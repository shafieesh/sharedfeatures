package com.chainedminds.api.account;

import com.chainedminds._Config;
import com.chainedminds._R;
import com.chainedminds.utilities.Messages;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities._NotificationManager;
import com.chainedminds.utilities.database.QueryCallback;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Friendship {

    private static final String TAG = _Friendship.class.getSimpleName();

    public static final int MAXIMUM_FRIENDS = 100;

    public static final short STATE_STRANGER = 0;
    public static final short STATE_WAITING = 1;
    public static final short STATE_PENDING = 2;
    public static final short STATE_FRIENDSHIP = 3;
    public static final short STATE_I_BLOCKED = 4;
    public static final short STATE_I_AM_BLOCKED = 5;

    public static final short ONLINE_STATUS_OFFLINE = 0;
    public static final short ONLINE_STATUS_ONLINE = 1;
    public static final short ONLINE_STATUS_AWAY = 2;
    public static final short ONLINE_STATUS_BUSY = 3;

    public static final short CAN_ADD_FRIENDSHIP = 0;
    public static final short YOU_HAVE_TOO_MANY_FRIENDS = 1;
    public static final short TARGET_HAS_TOO_MANY_FRIENDS = 2;

    public static final String FIELD_OLDER_USER_ID = "OlderUserID";
    public static final String FIELD_NEWER_USER_ID = "NewerUserID";
    public static final String FIELD_STATE = "State";

    public static final Map<String, Friendship> FRIENDSHIPS = new HashMap<>();

    public static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void fetch() {

        String statement = "SELECT * FROM " + _Config.TABLE_FRIENDSHIPS;

        _R.get().database.query(TAG, statement, new QueryCallback() {

            private final Map<String, Friendship> friendships = new HashMap<>();

            @Override
            public void fetch(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Friendship friendship = new Friendship();
                    friendship.lowerID = resultSet.getInt(FIELD_OLDER_USER_ID);
                    friendship.upperID = resultSet.getInt(FIELD_NEWER_USER_ID);

                    int packedState = resultSet.getInt(FIELD_STATE);

                    friendship.lowerState = (short) (packedState >> 16);
                    friendship.upperState = (short) packedState;

                    String key = friendship.lowerID + "-" + friendship.upperID;

                    friendships.put(key, friendship);
                }
            }

            @Override
            public void finalize(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        FRIENDSHIPS.clear();
                        FRIENDSHIPS.putAll(friendships);
                    });
                }
            }
        });
    }

    public static List<Friendship> getAll(int userID) {

        List<Friendship> friendships = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Friendship friendship : FRIENDSHIPS.values()) {

                if (friendship.lowerID == userID || friendship.upperID == userID) {

                    friendships.add(friendship);
                }
            }
        });

        return friendships;
    }

    public static Friendship get(int userID, int friendID) {

        int lowerID = Math.min(userID, friendID);
        int upperID = Math.max(userID, friendID);
        String key = lowerID + "-" + upperID;

        AtomicReference<Friendship> friendship = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> friendship.set(FRIENDSHIPS.get(key)));

        return friendship.get();
    }

    public static int getOnlineStatus(int userID) {

        long lastAccessTime = _R.get().requestHandler.getLastAccessTime(userID);

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

        Friendship friendship = get(userID, friendID);

        if (friendship != null) {

            if (userID == friendship.lowerID) {

                return friendship.lowerState;

            } else {

                return friendship.upperState;
            }
        }

        return STATE_STRANGER;
    }

    public static int canAddFriendship(int userID, int friendID) {

        AtomicInteger state = new AtomicInteger(CAN_ADD_FRIENDSHIP);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            int userRelations = 0;
            int friendRelations = 0;

            for (Friendship friendship : FRIENDSHIPS.values()) {

                if (friendship.lowerID == userID || friendship.upperID == userID) {

                    boolean isUserLower = userID == friendship.lowerID;
                    short userState = isUserLower ? friendship.lowerState : friendship.upperState;

                    if (userState == STATE_PENDING || userState == STATE_WAITING || userState == STATE_FRIENDSHIP) {

                        userRelations++;

                        if (userRelations >= MAXIMUM_FRIENDS) {

                            state.set(YOU_HAVE_TOO_MANY_FRIENDS);
                            break;
                        }
                    }
                }

                if (friendship.lowerID == friendID || friendship.upperID == friendID) {

                    boolean isFriendLower = userID == friendship.lowerID;
                    short friendState = isFriendLower ? friendship.lowerState : friendship.upperState;

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

            for (Friendship friendship : getAll(userID)) {

                int friendID = friendship.lowerID == userID ?
                        friendship.upperID : friendship.lowerID;

                short friendshipState = friendship.lowerID == friendID ?
                        friendship.lowerState : friendship.upperState;

                if (friendshipState == STATE_FRIENDSHIP) {

                    if (getOnlineStatus(friendID) == ONLINE_STATUS_ONLINE) {

                        onlineFriendIDs.add(friendID);
                    }
                }
            }
        });

        if (!onlineFriendIDs.isEmpty()) {

            String receiverLanguage = _R.get().accountSession.getLanguage(userID, appName);

            if (onlineFriendIDs.size() > 1) {

                String playersCount = Utilities.localizeNumbers(onlineFriendIDs.size() + "", receiverLanguage);
                String message = Messages.get("X_PLAYERS_ARE_ONLINE", receiverLanguage, playersCount);

                _NotificationManager.sendMessage(userID, appName, message);

            } else {

                String name = _R.get().account.getName(onlineFriendIDs.getFirst());
                String message = Messages.get("GAMERTAG_IS_ONLINE", receiverLanguage, name);

                _NotificationManager.sendMessage(userID, appName, message);
            }
        }

        String name = _R.get().account.getName(userID);

        for (int friendID : onlineFriendIDs) {

            String receiverLanguage = _R.get().accountSession.getLanguage(friendID, appName);
            String message = Messages.get("GAMERTAG_IS_NOW_ONLINE", receiverLanguage, name);

            _NotificationManager.sendMessage(friendID, appName, message);
        }
    }

    public static class Friendship {

        public int lowerID;
        public int upperID;
        public short lowerState;
        public short upperState;
    }
}