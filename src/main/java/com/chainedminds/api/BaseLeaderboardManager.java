package com.chainedminds.api;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.dataClasses.account.LeaderboardData;
import com.chainedminds.utilities.BackendHelper;
import com.chainedminds.utilities.BaseManager;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseLeaderboardManager<Data extends BaseData, AccountData extends BaseAccountData> extends BaseManager<Data> {

    private static final String TAG = BaseLeaderboardManager.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_PLAY_TIME = "PlayTime";
    private static final String FIELD_RESULT = "Result";
    private static final String FIELD_DATE_TIME = "DateTime";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_GAME_NAME = "GameName";
    private static final String FIELD_SCORE = "Score";
    private static final String FIELD_GAMER_TAG = "GamerTag";

    private static final List<LeaderboardData> DAILY_LEADERBOARD = new ArrayList<>();
    private static final List<LeaderboardData> WEEKLY_LEADERBOARD = new ArrayList<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("ResetLeaderboard")
                .setTime(0, 0, 0)
                .setInterval(1, 0, 0, 0)
                .setTimingListener(task -> {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        DAILY_LEADERBOARD.clear();

                        Calendar calendar = Calendar.getInstance();

                        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {

                            WEEKLY_LEADERBOARD.clear();
                        }
                    });
                }));

        /*TaskManager.addTask(TaskManager.Task.build()
                .setName("FetchLeaderboard")
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> {

                    fetchLeaderboard(LeaderboardType.DAILY);
                    fetchLeaderboard(LeaderboardType.WEEKLY);

                    super.setInitialization(true);
                })
                .startAsyncAndSchedule());*/
    }

    private static void fetchLeaderboard(LeaderboardType leaderboardType) {

        String appName = BaseConfig.APP_NAME_CAFE_GAME;

        Calendar calendar = Calendar.getInstance();

        if (leaderboardType == LeaderboardType.DAILY) {

            calendar.setTimeInMillis(BackendHelper.getTodayMills());
        }

        if (leaderboardType == LeaderboardType.WEEKLY) {

            calendar.setTimeInMillis(BackendHelper.getFirstDayOfTheWeekMills(BaseConfig.LANGUAGE_FA));
        }

        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());

        String selectStatement = "SELECT " + FIELD_USER_ID + ", SUM(" + FIELD_SCORE + ") AS " + FIELD_SCORE +
                " FROM " + BaseConfig.TABLE_GAME_SESSIONS + " WHERE " + FIELD_DATE_TIME + " > ?" + " AND "
                + FIELD_APP_NAME + " = ? GROUP BY " + FIELD_USER_ID + " ORDER BY " + FIELD_SCORE + " DESC";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, timestamp.toString());
        parameters.put(2, appName);

        BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, new TwoStepQueryCallback() {

            private final List<LeaderboardData> leaderboardData = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    int score = resultSet.getInt(FIELD_SCORE);

                    LeaderboardData leaderboardData = new LeaderboardData();
                    leaderboardData.id = userID;
                    leaderboardData.score = score;

                    this.leaderboardData.add(leaderboardData);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    BaseResources.getInstance().accountManager.getUsernameMap(TAG, new Utilities.GrantAccess<Map<Integer, String>>() {
                        @Override
                        public void giveAccess(Map<Integer, String> gamerTags) {

                            for (LeaderboardData leaderboardData : leaderboardData) {

                                leaderboardData.gamerTag = gamerTags.get(leaderboardData.id);
                            }
                        }
                    });

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        if (leaderboardType == LeaderboardType.DAILY) {

                            DAILY_LEADERBOARD.clear();

                            DAILY_LEADERBOARD.addAll(leaderboardData);
                        }

                        if (leaderboardType == LeaderboardType.WEEKLY) {

                            WEEKLY_LEADERBOARD.clear();

                            WEEKLY_LEADERBOARD.addAll(leaderboardData);
                        }
                    });

                    System.out.println(
                            "---------------------------------------------------\n" +
                                    "--> Fetched : " + TAG + " : " + leaderboardType.name() + "\n" +
                                    "---------------------------------------------------\n");

                    Utilities.sleep(5000);

                } else {

                    System.out.println(
                            "---------------------------------------------------\n" +
                                    "--> Fetch failed : " + TAG + " : " + leaderboardType.name() + "\n" +
                                    "---------------------------------------------------\n" +
                                    "--> Retrying in 5 seconds ...\n" +
                                    "---------------------------------------------------\n");

                    Utilities.sleep(5000);

                    fetchLeaderboard(leaderboardType);
                }
            }
        });
    }

    private static void sort(List<LeaderboardData> leaderboard) {

        leaderboard.sort((player1, player2) -> {

            if (player1.score == player2.score) {

                if (player1.gamerTag != null && player2.gamerTag != null) {

                    return player1.gamerTag.compareToIgnoreCase(player2.gamerTag);

                } else {

                    return 0;
                }

            } else {

                if (player1.score > player2.score) {

                    return -1;

                } else {

                    return 1;
                }
            }
        });
    }

    public Data getTopPlayersDaily(Data data) {

        if (data.client.gameName == null) {

            return super.handleMissingData(data);
        }

        String appName = data.client.appName;
        String gameName = data.client.gameName;

        data.response = BaseCodes.RESPONSE_NOK;

        data.topPlayers = new ArrayList<>();

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " + FIELD_SCORE +
                " FROM " + BaseConfig.TABLE_LEADERBOARD_DAILY + " WHERE " + FIELD_APP_NAME + " = ? AND " +
                FIELD_GAME_NAME + " = ? ORDER BY " + FIELD_SCORE + " DESC";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, appName);
        parameters.put(2, gameName);

        BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

            while (resultSet.next()) {

                int userID = resultSet.getInt(FIELD_USER_ID);
                int score = resultSet.getInt(FIELD_SCORE);

                AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                account.id = userID;
                account.score = score;

                data.topPlayers.add(account);
            }

            data.response = BaseCodes.RESPONSE_OK;
        });

        BaseResources.getInstance().accountManager.getUsernameMap(TAG, new Utilities.GrantAccess<Map<Integer, String>>() {
            @Override
            public void giveAccess(Map<Integer, String> gamerTags) {

                for (Object account : data.topPlayers) {

                    ((BaseAccountData) account).gamerTag = gamerTags.get(((BaseAccountData) account).id);
                }
            }
        });

        return data;
    }

    public Data getWeeklyTopPlayers(Data receivedData) {

        String appName = receivedData.client.appName;

        Data data = (Data) BaseClasses.construct(BaseClasses.getInstance().dataClass);

        data.response = BaseCodes.RESPONSE_NOK;

        data.topPlayers = new ArrayList<>();

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            if (super.checkInitialization()) {

                return super.handleNotInitialized(data);
            }

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (LeaderboardData leaderboardData : WEEKLY_LEADERBOARD) {

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = leaderboardData.id;
                    account.score = leaderboardData.score;
                    account.gamerTag = leaderboardData.gamerTag;
                    account.name = leaderboardData.gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });

        } else {

            Calendar firstDayOfTheWeekCalendar = Calendar.getInstance();
            firstDayOfTheWeekCalendar.setTimeInMillis(BackendHelper.getFirstDayOfTheWeekMills(BaseConfig.LANGUAGE_FA));

            Timestamp firstDayOfTheWeekTimestamp = new Timestamp(firstDayOfTheWeekCalendar.getTimeInMillis());

            String selectStatement = "SELECT " + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +
                    ", SUM(" + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_SCORE + ") AS " + FIELD_SCORE +
                    ", " + BaseConfig.TABLE_NAME_ACCOUNTS_USERS + "." + FIELD_GAMER_TAG + " FROM " + BaseConfig.TABLE_GAME_SESSIONS +
                    " INNER JOIN " + BaseConfig.TABLE_ACCOUNTS_USERS + " ON " + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." +
                    FIELD_USER_ID + " = " + BaseConfig.TABLE_ACCOUNTS_USERS + "." + FIELD_USER_ID + " WHERE " +
                    FIELD_DATE_TIME + " > ?" + " AND " + FIELD_APP_NAME + " = ?" + " GROUP BY " +
                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID + " ORDER BY " + FIELD_SCORE + " DESC";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, firstDayOfTheWeekTimestamp.toString());
            parameters.put(2, appName);

            BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String gamerTag = resultSet.getString(FIELD_GAMER_TAG);
                    int score = resultSet.getInt(FIELD_SCORE);

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = userID;
                    account.score = score;
                    account.name = gamerTag;
                    account.gamerTag = gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });
        }

        return data;
    }

    public Data getDailyTopPlayers(Data receivedData) {

        String appName = receivedData.client.appName;

        Data data = (Data) BaseClasses.construct(BaseClasses.getInstance().dataClass);

        data.response = BaseCodes.RESPONSE_NOK;

        data.topPlayers = new ArrayList<>();

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            if (super.checkInitialization()) {

                return super.handleNotInitialized(data);
            }

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                for (LeaderboardData leaderboardData : DAILY_LEADERBOARD) {

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = leaderboardData.id;
                    account.score = leaderboardData.score;
                    account.gamerTag = leaderboardData.gamerTag;
                    account.name = leaderboardData.gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });

        } else {

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTimeInMillis(BackendHelper.getTodayMills());

            Timestamp todayTimestamp = new Timestamp(todayCalendar.getTimeInMillis());

            String selectStatement = "SELECT " +

                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +

                    ", SUM(" + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_SCORE + ") AS " + FIELD_SCORE +

                    ", " + BaseConfig.TABLE_NAME_ACCOUNTS_USERS + "." + FIELD_GAMER_TAG +

                    " FROM " + BaseConfig.TABLE_GAME_SESSIONS +

                    " INNER JOIN " + BaseConfig.TABLE_ACCOUNTS_USERS + " ON " +

                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID + " = " + BaseConfig.TABLE_ACCOUNTS_USERS + "." + FIELD_USER_ID +

                    " WHERE " +

                    FIELD_DATE_TIME + " > ?" + " AND " + FIELD_APP_NAME + " = ?" +

                    " GROUP BY " + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +

                    " ORDER BY " + FIELD_SCORE + " DESC";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, todayTimestamp.toString());
            parameters.put(2, appName);

            BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String gamerTag = resultSet.getString(FIELD_GAMER_TAG);
                    int score = resultSet.getInt(FIELD_SCORE);

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = userID;
                    account.score = score;
                    account.name = gamerTag;
                    account.gamerTag = gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });
        }

        return data;
    }

    public Data getWeeklyTopPlayersLimited(Data receivedData) {

        String appName = receivedData.client.appName;

        int playerID = 0;

        if (receivedData.account != null) {

            playerID = receivedData.account.id;
        }

        Data data = (Data) BaseClasses.construct(BaseClasses.getInstance().dataClass);

        data.response = BaseCodes.RESPONSE_NOK;

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            if (super.checkInitialization()) {

                return super.handleNotInitialized(data);
            }

            int finalUserID = playerID;

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                data.topPlayers = getWeeklyTopTen();

                if (finalUserID != 0) {

                    int position = 0;

                    boolean isInLeaderBoard = false;

                    for (LeaderboardData leaderboardData : WEEKLY_LEADERBOARD) {

                        position++;

                        if (leaderboardData.id == finalUserID) {

                            isInLeaderBoard = true;
                            break;
                        }
                    }

                    if (isInLeaderBoard) {

                        if (position > 9 && position < 20) {

                            for (int index = 10; index < Math.min(20, WEEKLY_LEADERBOARD.size()); index++) {

                                LeaderboardData leaderboardData = WEEKLY_LEADERBOARD.get(index);

                                AccountData account = (AccountData) BaseClasses
                                        .construct(BaseClasses.getInstance().accountClass);
                                account.id = leaderboardData.id;
                                account.score = leaderboardData.score;
                                account.gamerTag = leaderboardData.gamerTag;
                                account.name = leaderboardData.gamerTag;
                                account.position = index;

                                data.topPlayers.add(account);
                            }

                        } else if (position >= 20) {

                            for (int index = -5; index < 5; index++) {

                                if (position + index < WEEKLY_LEADERBOARD.size()) {

                                    LeaderboardData leaderboardData = WEEKLY_LEADERBOARD.get(position + index);

                                    AccountData account = (AccountData) BaseClasses
                                            .construct(BaseClasses.getInstance().accountClass);
                                    account.id = leaderboardData.id;
                                    account.score = leaderboardData.score;
                                    account.gamerTag = leaderboardData.gamerTag;
                                    account.name = leaderboardData.gamerTag;
                                    account.position = position + index + 1;

                                    data.topPlayers.add(account);
                                }
                            }
                        }
                    }
                }
            });

            data.response = BaseCodes.RESPONSE_OK;

        } else {

            Calendar firstDayOfTheWeekCalendar = Calendar.getInstance();
            firstDayOfTheWeekCalendar.setTimeInMillis(BackendHelper.getFirstDayOfTheWeekMills(BaseConfig.LANGUAGE_FA));

            Timestamp firstDayOfTheWeekTimestamp = new Timestamp(firstDayOfTheWeekCalendar.getTimeInMillis());

            String selectStatement = "SELECT " +
                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +
                    ", SUM(" + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_SCORE + ") AS " + FIELD_SCORE +
                    ", " + BaseConfig.TABLE_NAME_ACCOUNTS_USERS + "." + FIELD_GAMER_TAG +
                    " FROM " + BaseConfig.TABLE_GAME_SESSIONS +
                    " INNER JOIN " + BaseConfig.TABLE_ACCOUNTS_USERS + " ON " +
                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID + " = " + BaseConfig.TABLE_ACCOUNTS_USERS + "." + FIELD_USER_ID +
                    " WHERE " +
                    FIELD_DATE_TIME + " > ?" + " AND " + FIELD_APP_NAME + " = ?" +
                    " GROUP BY " + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +
                    " ORDER BY " + FIELD_SCORE + " DESC";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, firstDayOfTheWeekTimestamp.toString());
            parameters.put(2, appName);

            BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String gamerTag = resultSet.getString(FIELD_GAMER_TAG);
                    int score = resultSet.getInt(FIELD_SCORE);

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = userID;
                    account.score = score;
                    account.name = gamerTag;
                    account.gamerTag = gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });
        }
        return data;
    }

    public Data getDailyTopPlayersLimited(Data receivedData) {

        String appName = receivedData.client.appName;

        int playerID = 0;

        if (receivedData.account != null) {

            playerID = receivedData.account.id;
        }

        Data data = (Data) BaseClasses.construct(BaseClasses.getInstance().dataClass);

        data.response = BaseCodes.RESPONSE_NOK;

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            if (super.checkInitialization()) {

                return super.handleNotInitialized(data);
            }

            int finalUserID = playerID;

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                data.topPlayers = getDailyTopTen();

                if (finalUserID != 0) {

                    int position = 0;

                    boolean isInLeaderBoard = false;

                    for (LeaderboardData leaderboardData : DAILY_LEADERBOARD) {

                        position++;

                        if (leaderboardData.id == finalUserID) {

                            isInLeaderBoard = true;

                            break;
                        }
                    }

                    if (isInLeaderBoard) {

                        if (position > 9 && position < 20) {

                            for (int index = 10; index < Math.min(20, DAILY_LEADERBOARD.size()); index++) {

                                LeaderboardData leaderboardData = DAILY_LEADERBOARD.get(index);

                                AccountData account = (AccountData) BaseClasses
                                        .construct(BaseClasses.getInstance().accountClass);
                                account.id = leaderboardData.id;
                                account.score = leaderboardData.score;
                                account.gamerTag = leaderboardData.gamerTag;
                                account.name = leaderboardData.gamerTag;
                                account.position = index;

                                data.topPlayers.add(account);
                            }

                        } else if (position >= 20) {

                            for (int index = -5; index < 5; index++) {

                                if (position + index < DAILY_LEADERBOARD.size()) {

                                    LeaderboardData leaderboardData = DAILY_LEADERBOARD.get(position + index);

                                    AccountData account = (AccountData) BaseClasses
                                            .construct(BaseClasses.getInstance().accountClass);
                                    account.id = leaderboardData.id;
                                    account.score = leaderboardData.score;
                                    account.gamerTag = leaderboardData.gamerTag;
                                    account.name = leaderboardData.gamerTag;
                                    account.position = position + index + 1;

                                    data.topPlayers.add(account);
                                }
                            }
                        }
                    }
                }
            });

            data.response = BaseCodes.RESPONSE_OK;

        } else {

            Calendar todayCalendar = Calendar.getInstance();
            todayCalendar.setTimeInMillis(BackendHelper.getTodayMills());

            Timestamp todayTimestamp = new Timestamp(todayCalendar.getTimeInMillis());

            String selectStatement = "SELECT " +

                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +

                    ", SUM(" + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_SCORE + ") AS " + FIELD_SCORE +

                    ", " + BaseConfig.TABLE_NAME_ACCOUNTS_USERS + "." + FIELD_GAMER_TAG +

                    " FROM " + BaseConfig.TABLE_GAME_SESSIONS +

                    " INNER JOIN " + BaseConfig.TABLE_ACCOUNTS_USERS + " ON " +

                    BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID + " = " + BaseConfig.TABLE_ACCOUNTS_USERS + "." + FIELD_USER_ID +

                    " WHERE " +

                    FIELD_DATE_TIME + " > ?" + " AND " + FIELD_APP_NAME + " = ?" +

                    " GROUP BY " + BaseConfig.TABLE_NAME_GAME_SESSIONS + "." + FIELD_USER_ID +

                    " ORDER BY " + FIELD_SCORE + " DESC";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, todayTimestamp.toString());
            parameters.put(2, appName);

            BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String gamerTag = resultSet.getString(FIELD_GAMER_TAG);
                    int score = resultSet.getInt(FIELD_SCORE);

                    AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                    account.id = userID;
                    account.score = score;
                    account.name = gamerTag;
                    account.gamerTag = gamerTag;

                    data.topPlayers.add(account);
                }

                data.response = BaseCodes.RESPONSE_OK;
            });

        }
        return data;
    }

    public List<AccountData> getDailyTopTen() {

        List<AccountData> topTen = new ArrayList<>();

        if (super.checkInitialization()) {

            return topTen;
        }

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (LeaderboardData leaderboardData : DAILY_LEADERBOARD) {

                AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                account.id = leaderboardData.id;
                account.score = leaderboardData.score;
                account.gamerTag = leaderboardData.gamerTag;
                account.name = leaderboardData.gamerTag;

                topTen.add(account);

                if (topTen.size() == 10) {

                    break;
                }
            }
        });

        return topTen;
    }

    private List<AccountData> getWeeklyTopTen() {

        List<AccountData> topTen = new ArrayList<>();

        if (super.checkInitialization()) {

            return topTen;
        }

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (LeaderboardData leaderboardData : WEEKLY_LEADERBOARD) {

                AccountData account = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                account.id = leaderboardData.id;
                account.score = leaderboardData.score;
                account.gamerTag = leaderboardData.gamerTag;
                account.name = leaderboardData.gamerTag;

                topTen.add(account);

                if (topTen.size() == 10) {

                    break;
                }
            }
        });

        return topTen;
    }

    public boolean addScore(int userID, String appName, int score) {

        if (super.checkInitialization()) {

            return false;
        }

        if (!BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            return true;
        }

        AtomicBoolean wasSuccessful = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.writeLock(), () -> {

            {
                boolean recordExists = false;

                for (LeaderboardData leaderboardData : DAILY_LEADERBOARD) {

                    if (leaderboardData.id == userID) {

                        leaderboardData.score += score;

                        recordExists = true;

                        break;
                    }
                }

                if (!recordExists) {

                    LeaderboardData leaderboardRecord = new LeaderboardData();
                    leaderboardRecord.id = userID;
                    leaderboardRecord.score = score;
                    leaderboardRecord.gamerTag = BaseResources.getInstance().accountManager.getGamerTag(userID);

                    DAILY_LEADERBOARD.add(leaderboardRecord);
                }
            }

            {
                boolean recordExists = false;

                for (LeaderboardData leaderboardData : WEEKLY_LEADERBOARD) {

                    if (leaderboardData.id == userID) {

                        leaderboardData.score += score;

                        recordExists = true;

                        break;
                    }
                }

                if (!recordExists) {

                    LeaderboardData leaderboardRecord = new LeaderboardData();
                    leaderboardRecord.id = userID;
                    leaderboardRecord.score = score;
                    leaderboardRecord.gamerTag = BaseResources.getInstance().accountManager.getGamerTag(userID);

                    WEEKLY_LEADERBOARD.add(leaderboardRecord);
                }
            }

            sort(DAILY_LEADERBOARD);

            sort(WEEKLY_LEADERBOARD);

            wasSuccessful.set(true);
        });

        return wasSuccessful.get();
    }

    public enum LeaderboardType {

        DAILY, WEEKLY
    }
}