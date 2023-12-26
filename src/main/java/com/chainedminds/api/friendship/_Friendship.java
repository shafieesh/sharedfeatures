package com.chainedminds.api.friendship;

import com.chainedminds._Classes;
import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.api.ActivityManager;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._FriendData;
import com.chainedminds.utilities._NotificationManager;
import com.chainedminds.utilities.Messages;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Friendship<Data extends _Data<?>, FriendData extends _FriendData> {

    private static final String TAG = _Friendship.class.getSimpleName();

    private static final int MAXIMUM_FRIENDS = 100;

    public static final short STATE_STRANGER = 0;
    public static final short STATE_WAITING = 1;
    public static final short STATE_PENDING = 2;
    public static final short STATE_FRIENDSHIP = 3;
    public static final short STATE_I_BLOCKED = 4;
    public static final short STATE_I_AM_BLOCKED = 5;

    private static final short ONLINE_STATUS_OFFLINE = 0;
    private static final short ONLINE_STATUS_ONLINE = 1;
    private static final short ONLINE_STATUS_AWAY = 2;
    private static final short ONLINE_STATUS_BUSY = 3;

    private static final short CAN_ADD_FRIENDSHIP = 0;
    private static final short YOU_HAVE_TOO_MANY_FRIENDS = 1;
    private static final short TARGET_HAS_TOO_MANY_FRIENDS = 2;

    private static final String FIELD_OLDER_USER_ID = "OlderUserID";
    private static final String FIELD_NEWER_USER_ID = "NewerUserID";
    private static final String FIELD_STATE = "State";

    private static final Set<Friendship> RELATIONS = new HashSet<>();
    //private static final List<BatchData> JOBS = new ArrayList<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("FriendsListManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> fetch())
                .runNow()
                .schedule());
    }

    private void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_FRIENDS_LIST;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Set<Friendship> relations = new HashSet<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Friendship friendship = new Friendship();

                    friendship.olderUserID = resultSet.getInt(FIELD_OLDER_USER_ID);
                    friendship.newerUserID = resultSet.getInt(FIELD_NEWER_USER_ID);

                    int packedState = resultSet.getInt(FIELD_STATE);

                    friendship.olderUserState = (short) (packedState >> 16);
                    friendship.newerUserState = (short) packedState;

                    relations.add(friendship);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        RELATIONS.clear();

                        RELATIONS.addAll(relations);
                    });
                }
            }
        });
    }

    public Data addFriend(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.friend == null || data.friend.id == 0) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        int friendID = data.friend.id;
        String appName = data.client.appName;
        String language = data.client.language;

        int olderID = Math.min(userID, friendID);
        int newerID = Math.max(userID, friendID);
        boolean isUserOlder = userID == olderID;

        if (userID == friendID) {

            return data;
        }

        if (getState(userID, friendID) == STATE_I_AM_BLOCKED) {

            return data;
        }

        switch (canAddFriendship(userID, friendID)) {

            case YOU_HAVE_TOO_MANY_FRIENDS:

                data.message = Messages.get("GENERAL", Messages.General.
                        YOU_HAVE_TOO_MANY_FRIENDS, language);
                return data;

            case TARGET_HAS_TOO_MANY_FRIENDS:

                data.message = Messages.get("GENERAL", Messages.General.
                        TARGET_HAS_TOO_MANY_FRIENDS, language);
                return data;
        }

        short olderState = isUserOlder ? STATE_WAITING : STATE_PENDING;
        short newerState = !isUserOlder ? STATE_WAITING : STATE_PENDING;
        int relationshipState = ((int) olderState << 16 | newerState);

        String statement = "REPLACE INTO " + _Config.TABLE_FRIENDS_LIST + " (" + FIELD_OLDER_USER_ID + ", " +
                FIELD_NEWER_USER_ID + ", " + FIELD_STATE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, olderID);
        parameters.put(2, newerID);
        parameters.put(3, relationshipState);

        boolean wasSuccessful = _DatabaseOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            AtomicBoolean recordExists = new AtomicBoolean(false);

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (Friendship friendship : RELATIONS) {

                    if (friendship.olderUserID == olderID && friendship.newerUserID == newerID) {

                        friendship.olderUserState = olderState;
                        friendship.newerUserState = newerState;

                        recordExists.set(true);

                        break;
                    }
                }
            });

            if (!recordExists.get()) {

                Friendship friendship = new Friendship();

                friendship.olderUserID = olderID;
                friendship.newerUserID = newerID;
                friendship.olderUserState = olderState;
                friendship.newerUserState = newerState;

                addFriendship(friendship);
            }

            data.response = _Codes.RESPONSE_OK;

            {
                int receiverID = friendID;
                int senderID = userID;

                String requestingUserName = _Resources.getInstance().accountManager.getUsername(senderID);
                String receiverLanguage = _Resources.getInstance()
                        .accountPropertyManager.getLanguage(receiverID, appName);

                String tag = Messages.Notification.Friendship.TAG_NEW_FRIEND_REQUEST;

                String title = Messages.get("NOTIFICATION", Messages.Notification.
                        Friendship.NEW_FRIEND_REQUEST, receiverLanguage, requestingUserName);

                String message = Messages.get("NOTIFICATION", Messages.Notification.
                        TOUCH_HERE_TO_OPEN, receiverLanguage);

                _NotificationManager.sendNotification(receiverID, appName, tag, title, message);
            }
        }

        return data;
    }

    private static void addFriendship(Friendship friendship) {

        Utilities.lock(TAG, LOCK.writeLock(), () -> RELATIONS.add(friendship));
    }

    public Data acceptFriend(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.friend == null || data.friend.id == 0) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        int friendID = data.friend.id;
        String appName = data.client.appName;

        int olderID = Math.min(userID, friendID);
        int newerID = Math.max(userID, friendID);

        if (userID == friendID) {

            return data;
        }

        short olderState = STATE_FRIENDSHIP;
        short newerState = STATE_FRIENDSHIP;
        int relationshipState = ((int) olderState << 16 | newerState);

        String statement = "REPLACE INTO " + _Config.TABLE_FRIENDS_LIST + " (" + FIELD_OLDER_USER_ID + ", " +
                FIELD_NEWER_USER_ID + ", " + FIELD_STATE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, olderID);
        parameters.put(2, newerID);
        parameters.put(3, relationshipState);

        boolean wasSuccessful = _DatabaseOld.update(TAG, statement, parameters);

        if (wasSuccessful) {

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (Friendship friendship : RELATIONS) {

                    if (friendship.olderUserID == olderID && friendship.newerUserID == newerID) {

                        friendship.olderUserState = olderState;
                        friendship.newerUserState = newerState;

                        break;
                    }
                }
            });

            data.response = _Codes.RESPONSE_OK;

            {
                int receiverID = friendID;
                int acceptingUserID = userID;

                String requestingUserName = _Resources.getInstance().accountManager.getUsername(acceptingUserID);
                String receiverLanguage = _Resources.getInstance()
                        .accountPropertyManager.getLanguage(receiverID, appName);

                String tag = Messages.Notification.Friendship.TAG_ACCEPTED_YOUR_FRIENDSHIP;

                String title = Messages.get("NOTIFICATION", Messages.Notification.
                        Friendship.ACCEPTED_YOUR_FRIENDSHIP, receiverLanguage, requestingUserName);

                String message = Messages.get("NOTIFICATION", Messages.Notification.
                        TOUCH_HERE_TO_OPEN, receiverLanguage);

                _NotificationManager.sendNotification(receiverID, appName, tag, title, message);
            }
        }

        return data;
    }

    public Data removeFriend(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.friend == null || data.friend.id == 0) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        int friendID = data.friend.id;

        int olderID = Math.min(userID, friendID);
        int newerID = Math.max(userID, friendID);

        if (userID == friendID) {

            return data;
        }

        String statement = "DELETE FROM " + _Config.TABLE_FRIENDS_LIST + " WHERE " +
                FIELD_OLDER_USER_ID + " = ? AND " + FIELD_NEWER_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, olderID);
        parameters.put(2, newerID);

        boolean wasSuccessful = _DatabaseOld.update(TAG, statement, parameters);

        if (wasSuccessful) {

            AtomicReference<Friendship> removingItem = new AtomicReference<>();

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                for (Friendship friendship : RELATIONS) {

                    if (friendship.olderUserID == olderID && friendship.newerUserID == newerID) {

                        removingItem.set(friendship);
                    }
                }

                RELATIONS.remove(removingItem.get());
            });

            data.response = _Codes.RESPONSE_OK;
        }

        return data;
    }

    public Data block(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.friend == null || data.friend.id == 0) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        int friendID = data.friend.id;

        int olderID = Math.min(userID, friendID);
        int newerID = Math.max(userID, friendID);
        boolean isUserOlder = userID == olderID;

        short olderState = isUserOlder ? STATE_I_BLOCKED : STATE_I_AM_BLOCKED;
        short newerState = !isUserOlder ? STATE_I_BLOCKED : STATE_I_AM_BLOCKED;
        int relationshipState = ((int) olderState << 16 | newerState);

        if (userID == friendID) {

            return data;
        }

        String statement = "REPLACE INTO " + _Config.TABLE_FRIENDS_LIST + " (" +
                FIELD_OLDER_USER_ID + ", " + FIELD_NEWER_USER_ID + ", " + FIELD_STATE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, olderID);
        parameters.put(2, newerID);
        parameters.put(3, relationshipState);

        boolean wasSuccessful = _DatabaseOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            AtomicBoolean recordExists = new AtomicBoolean(false);

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (Friendship friendship : RELATIONS) {

                    if (friendship.olderUserID == olderID && friendship.newerUserID == newerID) {

                        friendship.olderUserState = olderState;
                        friendship.newerUserState = newerState;

                        recordExists.set(true);
                    }
                }
            });

            if (!recordExists.get()) {

                Friendship friendship = new Friendship();

                friendship.olderUserID = userID;
                friendship.newerUserID = friendID;
                friendship.olderUserState = olderState;
                friendship.newerUserState = newerState;

                addFriendship(friendship);
            }

            data.response = _Codes.RESPONSE_OK;
        }

        return data;
    }

    public Data getFriendsList(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.id == 0) {

            return data;
        }

        int userID = data.account.id;
        String appName = data.client.appName;
        String language = data.client.language;

        data.friends = new ArrayList<>(getActivitySortedFriendsList(userID, appName, language));

        data.response = _Codes.RESPONSE_OK;

        return data;
    }

    private static <FriendData extends _FriendData> List<FriendData> getActivitySortedFriendsList(int userID, String appName, String language) {

        List<FriendData> friendsList = new ArrayList<>();

        List<FriendData> pendingRelations = new ArrayList<>();
        List<FriendData> waitingRelations = new ArrayList<>();
        List<FriendData> onlineFriends = new ArrayList<>();
        List<FriendData> awayFriends = new ArrayList<>();
        List<FriendData> offlineFriends = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Friendship friendship : RELATIONS) {

                if (friendship.olderUserID == userID || friendship.newerUserID == userID) {

                    int friendId = friendship.olderUserID == userID ?
                            friendship.newerUserID : friendship.olderUserID;

                    short friendshipState = friendship.olderUserID == userID ?
                            friendship.olderUserState : friendship.newerUserState;

                    if (friendshipState == STATE_PENDING) {

                        FriendData friend = (FriendData) _Classes.construct(_Classes.getInstance().friendClass);

                        friend.status = STATE_PENDING;
                        friend.id = friendId;
                        friend.onlineStatus = getOnlineStatus(friendId);
                        friend.activity = ActivityManager.getLastActivity(friendId, appName, language);

                        pendingRelations.add(friend);
                    }
                    if (friendshipState == STATE_WAITING) {

                        FriendData friend = (FriendData) _Classes.construct(_Classes.getInstance().friendClass);

                        friend.status = STATE_WAITING;
                        friend.id = friendId;
                        friend.onlineStatus = getOnlineStatus(friendId);
                        friend.activity = ActivityManager.getLastActivity(friendId, appName, language);

                        waitingRelations.add(friend);
                    }

                    if (friendshipState == STATE_FRIENDSHIP) {

                        FriendData friend = (FriendData) _Classes.construct(_Classes.getInstance().friendClass);

                        friend.status = STATE_FRIENDSHIP;
                        friend.id = friendId;
                        friend.onlineStatus = getOnlineStatus(friendId);
                        friend.activity = ActivityManager.getLastActivity(friendId, appName, language);

                        if (friend.onlineStatus == ONLINE_STATUS_ONLINE) {

                            onlineFriends.add(friend);
                        }

                        if (friend.onlineStatus == ONLINE_STATUS_BUSY || friend.onlineStatus == ONLINE_STATUS_AWAY) {

                            awayFriends.add(friend);
                        }

                        if (friend.onlineStatus == ONLINE_STATUS_OFFLINE) {

                            offlineFriends.add(friend);
                        }
                    }
                }
            }
        });

        _Resources.getInstance().accountManager.getUsernameMap(TAG, (Utilities.GrantAccess<Map<Integer, String>>) usernames -> {

            for (FriendData friend : pendingRelations) {

                friend.username = usernames.get(friend.id);
            }
            for (FriendData friend : waitingRelations) {

                friend.username = usernames.get(friend.id);
            }
            for (FriendData friend : onlineFriends) {

                friend.username = usernames.get(friend.id);
            }
            for (FriendData friend : awayFriends) {

                friend.username = usernames.get(friend.id);
            }
            for (FriendData friend : offlineFriends) {

                friend.username = usernames.get(friend.id);
            }
        });

        Comparator<FriendData> comparator = (friend1, friend2) -> {

            if (friend1 != null && friend1.username != null && friend2 != null && friend2.username != null) {

                return friend1.username.compareToIgnoreCase(friend2.username);
            }

            return 0;
        };

        pendingRelations.sort(comparator);
        waitingRelations.sort(comparator);
        onlineFriends.sort(comparator);
        awayFriends.sort(comparator);
        offlineFriends.sort(comparator);

        friendsList.addAll(pendingRelations);
        friendsList.addAll(waitingRelations);
        friendsList.addAll(onlineFriends);
        friendsList.addAll(awayFriends);
        friendsList.addAll(offlineFriends);

        return friendsList;
    }

    public static int getOnlineStatus(int userID) {

        long lastAccessTime = _Resources.getInstance().requestManager.getLastAccessTime(userID);

        long currentTime = System.currentTimeMillis();

        long diff = currentTime - lastAccessTime;

        if (diff < _Config.TWO_MINUTES) {

            return ONLINE_STATUS_ONLINE;
        }

        if (diff < _Config.FIVE_MINUTES) {

            return ONLINE_STATUS_AWAY;
        }

        return ONLINE_STATUS_OFFLINE;
    }

    public static int getState(int userID, int friendID) {

        AtomicInteger packedState = new AtomicInteger(STATE_STRANGER);

        int olderUserID = Math.min(userID, friendID);
        int newerUserID = Math.max(userID, friendID);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Friendship friendship : RELATIONS) {

                if (friendship.olderUserID == olderUserID && friendship.newerUserID == newerUserID) {

                    if (userID == olderUserID) {

                        packedState.set(friendship.olderUserState);

                    } else {

                        packedState.set(friendship.newerUserState);
                    }

                    break;
                }
            }
        });

        return packedState.get();
    }

    private static int canAddFriendship(int userID, int friendID) {

        AtomicInteger state = new AtomicInteger(CAN_ADD_FRIENDSHIP);

        int olderID = Math.min(userID, friendID);
        int newerID = Math.max(userID, friendID);
        boolean isUserOlder = userID == olderID;
        boolean isFriendOlder = friendID == olderID;

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            int userRelations = 0;
            int friendRelations = 0;

            for (Friendship friendship : RELATIONS) {

                if ((isUserOlder && friendship.olderUserID == olderID) ||
                        (!isUserOlder && friendship.newerUserID == newerID)) {

                    short userState = isUserOlder ? friendship.olderUserState : friendship.newerUserState;

                    if (userState == STATE_PENDING || userState == STATE_WAITING || userState == STATE_FRIENDSHIP) {

                        userRelations++;

                        if (userRelations >= MAXIMUM_FRIENDS) {

                            state.set(YOU_HAVE_TOO_MANY_FRIENDS);

                            break;
                        }
                    }
                }

                if ((isFriendOlder && friendship.olderUserID == olderID) ||
                        (!isFriendOlder && friendship.newerUserID == newerID)) {

                    short friendState = isFriendOlder ? friendship.olderUserState : friendship.newerUserState;

                    if (friendState == STATE_PENDING || friendState == STATE_WAITING || friendState == STATE_FRIENDSHIP) {

                        friendRelations++;

                        if (friendRelations >= MAXIMUM_FRIENDS) {

                            state.set(TARGET_HAS_TOO_MANY_FRIENDS);

                            break;
                        }
                    }
                }
            }
        });

        return state.get();
    }

    public static void notifyPlayerIsOnline(int userID, String appName) {

        List<Integer> onlineFriendIDs = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Friendship friendship : RELATIONS) {

                if (friendship.olderUserID == userID || friendship.newerUserID == userID) {

                    int friendId = friendship.olderUserID == userID ?
                            friendship.newerUserID : friendship.olderUserID;

                    short friendshipState = friendship.olderUserID == userID ?
                            friendship.newerUserState : friendship.olderUserState;

                    if (friendshipState == STATE_FRIENDSHIP) {

                        if (getOnlineStatus(friendId) == ONLINE_STATUS_ONLINE) {

                            onlineFriendIDs.add(friendId);
                        }
                    }
                }
            }
        });

        if (onlineFriendIDs.size() > 0) {

            String receiverLanguage = _Resources.getInstance()
                    .accountPropertyManager.getLanguage(userID, appName);

            if (onlineFriendIDs.size() > 1) {

                String playersCount = Utilities.localizeNumbers(onlineFriendIDs.size() + "", receiverLanguage);

                String message = Messages.get("FRIENDSHIP", Messages.Friendship.
                        X_PLAYERS_ARE_ONLINE, receiverLanguage, playersCount);

                _NotificationManager.sendMessage(userID, appName, message);

            } else {

                String gamerTag = _Resources.getInstance()
                        .accountManager.getUsername(onlineFriendIDs.get(0));

                String message = Messages.get("FRIENDSHIP", Messages.Friendship.
                        GAMERTAG_IS_ONLINE, receiverLanguage, gamerTag);

                _NotificationManager.sendMessage(userID, appName, message);
            }
        }

        String gamerTag = _Resources.getInstance().accountManager.getUsername(userID);

        for (int friendID : onlineFriendIDs) {

            String receiverLanguage = _Resources.getInstance()
                    .accountPropertyManager.getLanguage(friendID, appName);

            String message = Messages.get("FRIENDSHIP", Messages.Friendship.
                    GAMERTAG_IS_NOW_ONLINE, receiverLanguage, gamerTag);

            _NotificationManager.sendMessage(friendID, appName, message);
        }
    }

    public static class Friendship {

        public int userID;
        public int friendID;
        public int state;

        public int olderUserID;
        public int newerUserID;
        public short olderUserState;
        public short newerUserState;

        /*@Override
        public int hashCode() {

            return Objects.hash(olderUserID, newerUserID);
        }

        @Override
        public boolean equals(Object anotherObject) {

            if (!(anotherObject instanceof Friendship)) {

                return false;
            }

            if (anotherObject == this) {

                return true;
            }

            Friendship anotherFriendship = (Friendship) anotherObject;

            return this.olderUserID == anotherFriendship.olderUserID &&
                    this.newerUserID == anotherFriendship.newerUserID;
        }*/
    }
}