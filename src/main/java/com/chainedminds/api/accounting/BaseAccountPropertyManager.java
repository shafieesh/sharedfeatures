package com.chainedminds.api.accounting;

import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.api.IPLocationFinder;
import com.chainedminds.models.BaseData;
import com.chainedminds.utilities.CacheManager;
import com.chainedminds.utilities.Hash;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.DBResult;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BaseAccountPropertyManager {

    private static final String TAG = BaseAccountPropertyManager.class.getSimpleName();

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_APP_NAME = "AppName";
    protected static final String FIELD_PLATFORM = "Platform";
    protected static final String FIELD_IS_ACTIVE = "IsActive";
    protected static final String FIELD_CREDENTIAL = "Credential";
    protected static final String FIELD_APP_VERSION = "AppVersion";
    protected static final String FIELD_MARKET = "Market";
    protected static final String FIELD_LANGUAGE = "Language";
    protected static final String FIELD_UUID = "UUID";
    protected static final String FIELD_FIREBASE_ID = "FirebaseID";
    protected static final String FIELD_IP_ADDRESS = "IPAddress";
    protected static final String FIELD_COUNTRY = "Country";
    protected static final String FIELD_LAST_UPDATE = "LastUpdate";

    protected static final CacheManager<Integer, Map<String, String>> CREDENTIALS_CACHE = new CacheManager<>();
    protected static final CacheManager<Integer, Map<String, String>> LANGUAGE_CACHE = new CacheManager<>();

    protected final Set<Integer> CACHED_USERS_INFO = new HashSet<>();
    public static final Map<String, Map<Integer, Long>> USER_ACTIVITY = new HashMap<>();

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("Clear Cached Users Info")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 15, 0)
                .setTimingListener(task -> CACHED_USERS_INFO.clear())
                .schedule());
    }

    public void fetch() {

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " + FIELD_APP_NAME +
                ", " + FIELD_LAST_UPDATE + " FROM " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES;

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String appName = resultSet.getString(FIELD_APP_NAME);
                    long lastUpdate = resultSet.getTimestamp(FIELD_LAST_UPDATE).getTime();

                    USER_ACTIVITY.putIfAbsent(appName, new HashMap<>());
                    USER_ACTIVITY.get(appName).put(userID, lastUpdate);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

            }
        });
    }

    public String getLanguage(int userID, String appName) {

        Map<String, String> appNamePlatformCredential = LANGUAGE_CACHE.getOrPut(userID, HashMap::new);

        if (!appNamePlatformCredential.containsKey(appName)) {

            String language = getPropertyOld(userID, appName, FIELD_LANGUAGE, String.class);

            if (language == null) {

                language = BaseConfig.LANGUAGE_DEFAULT;
            }

            appNamePlatformCredential.put(appName, language);

            return language;

        } else {

            return appNamePlatformCredential.get(appName);
        }
    }

    public boolean getCredential(int userID, String appName, String platform, AtomicReference<String> credential) {

        Map<String, String> appNamePlatformCredential = CREDENTIALS_CACHE.getOrPut(userID, HashMap::new);

        String key = appName + "-" + platform;

        if (!appNamePlatformCredential.containsKey(key)) {

            DBResult<String> result = getProperty(userID, appName, platform, FIELD_CREDENTIAL, String.class);

            if (result.isSuccessful()) {

                appNamePlatformCredential.put(key, result.value);
                credential.set(result.value);
            }

            return result.isSuccessful();

        } else {

            credential.set(appNamePlatformCredential.get(key));
            return true;
        }
    }

    public Boolean getIsActive(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_IS_ACTIVE, Boolean.class);
    }

    public String getFirebaseID(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_FIREBASE_ID, String.class);
    }

    public String getIPAddress(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_IP_ADDRESS, String.class);
    }

    public Set<String> getIPAddresses(int userID) {

        DBResult<Set<String>> result = getProperties(userID, FIELD_IP_ADDRESS, new HashSet<>(), String.class);

        return result.value;
    }

    public String getUUID(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_UUID, String.class);
    }

    public Set<String> getUUIDs(int userID) {

        DBResult<Set<String>> result = getProperties(userID, FIELD_UUID, new HashSet<>(), String.class);

        return result.value;
    }

    public Set<String> getFirebaseIDs(int userID) {

        DBResult<Set<String>> result = getProperties(userID, FIELD_FIREBASE_ID, new HashSet<>(), String.class);

        return result.value;
    }

    public Set<Integer> getSimilarAccountsByUUID(int userID, Collection<String> uuids) {

        Set<Integer> userIDs = new HashSet<>();

        userIDs.add(userID);

        if (uuids != null && !uuids.isEmpty()) {

            Map<Integer, Object> parameters = new HashMap<>();

            List<String> questionMarks = new ArrayList<>();

            int counter = 1;

            for (String uuid : uuids) {

                parameters.put(counter, uuid);

                questionMarks.add("?");

                counter++;
            }

            String questionMarksArray = String.join(", ", questionMarks);

            String statement = "SELECT DISTINCT " + FIELD_USER_ID + " FROM " +
                    BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " WHERE " +
                    FIELD_IS_ACTIVE + " = FALSE AND " + FIELD_UUID +
                    " IN (" + questionMarksArray + ")";

            BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

                while (resultSet.next()) {

                    userIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            });
        }

        return userIDs;
    }

    public Set<Integer> getSimilarAccountsByFirebaseID(int userID, Collection<String> firebaseIDs) {

        Set<Integer> userIDs = new HashSet<>();

        userIDs.add(userID);

        if (firebaseIDs != null && !firebaseIDs.isEmpty()) {

            Map<Integer, Object> parameters = new HashMap<>();

            List<String> questionMarks = new ArrayList<>();

            int counter = 1;

            for (String firebaseID : firebaseIDs) {

                parameters.put(counter, firebaseID);

                questionMarks.add("?");

                counter++;
            }

            String questionMarksArray = String.join(", ", questionMarks);

            String statement = "SELECT DISTINCT " + FIELD_USER_ID + " FROM " +
                    BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " WHERE " +
                    FIELD_IS_ACTIVE + " = FALSE AND " + FIELD_FIREBASE_ID +
                    " IN (" + questionMarksArray + ")";

            BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

                while (resultSet.next()) {

                    userIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            });
        }

        return userIDs;
    }

    public Set<Integer> getSimilarAccountsByIPAddresses(int userID, Collection<String> ipAddresses) {

        Set<Integer> userIDs = new HashSet<>();

        userIDs.add(userID);

        if (ipAddresses != null && !ipAddresses.isEmpty()) {

            Map<Integer, Object> parameters = new HashMap<>();

            List<String> questionMarks = new ArrayList<>();

            int counter = 1;

            for (String ipAddress : ipAddresses) {

                parameters.put(counter, ipAddress);

                questionMarks.add("?");

                counter++;
            }

            String questionMarksArray = String.join(", ", questionMarks);

            String statement = "SELECT DISTINCT " + FIELD_USER_ID + " FROM " +
                    BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " WHERE " +
                    FIELD_IS_ACTIVE + " = FALSE AND " + FIELD_IP_ADDRESS +
                    " IN (" + questionMarksArray + ")";

            BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

                while (resultSet.next()) {

                    userIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            });
        }

        return userIDs;
    }

    public boolean removeFirebaseID(int userID, String firebaseID) {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " SET " +
                FIELD_FIREBASE_ID + " = NULL WHERE " + FIELD_USER_ID + " = ? AND " + FIELD_FIREBASE_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, firebaseID);

        return BaseDatabaseHelperOld.update(TAG, updateStatement, parameters);
    }

    public boolean setCredential(int userID, String appName, String platform, String credential) {

        boolean wasSuccessful = setProperty(userID, appName, platform, FIELD_CREDENTIAL, credential);

        if (wasSuccessful) {

            String key = appName + "-" + platform;

            Map<String, String> appNamePlatformCredential = CREDENTIALS_CACHE.getOrPut(userID, HashMap::new);

            appNamePlatformCredential.put(key, credential);
        }

        return wasSuccessful;
    }

    public boolean setCredential(Connection connection, int userID,
                                 String appName, String platform, String credential) {

        boolean wasSuccessful = setProperty(connection, userID, appName, platform, FIELD_CREDENTIAL, credential);

        if (wasSuccessful) {

            String key = appName + "-" + platform;

            Map<String, String> appNamePlatformCredential = CREDENTIALS_CACHE.getOrPut(userID, HashMap::new);

            appNamePlatformCredential.put(key, credential);
        }

        return wasSuccessful;
    }

    public boolean setIsActive(int userID, boolean isActive) {

        String statement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " SET " + FIELD_IS_ACTIVE + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, isActive);
        parameters.put(2, userID);

        return BaseDatabaseHelperOld.update(TAG, statement, parameters);
    }

    public Set<Integer> getUserIDs(String appName) {

        DBResult<Set<Integer>> result = getProperties(appName, FIELD_USER_ID, new HashSet<>(), Integer.class);

        return result.value;
    }

    //------------------------------------------------------------------------------------

    @Deprecated
    private <T> T getPropertyOld(int userID, String appName, String field, Class<T> T) {

        AtomicReference<T> value = new AtomicReference<>();

        String statement = "SELECT " + field + " FROM " +
                BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                value.set(resultSet.getObject(field, T));
            }
        });

        if (value.get() == null) {

            if (T == Integer.class) {

                value.set((T) Integer.valueOf(BaseConfig.NOT_FOUND));
            }
        }

        return value.get();
    }

    public <T> DBResult<T> getProperty(int userID, String appName, String platform, String field, Class<T> T) {

        DBResult<T> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " +
                BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ? AND " + FIELD_PLATFORM + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

            private T value = null;

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                if (resultSet.next()) {

                    value = resultSet.getObject(field, T);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    result.value = value;

                } else {

                    result.error = error;
                }
            }
        });

        if (result.isSuccessful() && result.value == null) {

            if (T == Integer.class) {

                result.value = (T) (Integer) BaseConfig.NOT_FOUND;
            }
        }

        return result;
    }

    public <Type, Series extends Collection<Type>> DBResult<Series> getProperties(
            String appName, String field, Series values, Class<Type> type) {

        DBResult<Series> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " +
                BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " WHERE " + FIELD_APP_NAME + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, appName);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Type value = resultSet.getObject(field, type);

                    values.add(value);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    result.value = values;

                } else {

                    result.error = error;
                }
            }
        });

        return result;
    }

    public <Type, Series extends Collection<Type>> DBResult<Series> getProperties(
            int userID, String field, Series collection, Class<Type> type) {

        DBResult<Series> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " +
                BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Type value = resultSet.getObject(field, type);

                    collection.add(value);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    result.value = collection;

                } else {

                    result.error = error;
                }
            }
        });

        return result;
    }

    public boolean setProperty(int userID, String appName, String platform, String field, Object value) {

        String updateStatement = "INSERT INTO " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PLATFORM + ", " + field +
                ") VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")" + ", " + FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);
        parameters.put(4, value);

        return BaseDatabaseHelperOld.update(TAG, updateStatement, parameters);
    }

    public boolean setProperty(Connection connection, int userID, String appName, String platform, String field, Object value) {

        String updateStatement = "INSERT INTO " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PLATFORM + ", " + field +
                ") VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")" + ", " + FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);
        parameters.put(4, value);

        return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);
    }

    public boolean setProperty(Connection connection, int userID, String appName, String field, Object value) {

        String updateStatement = "INSERT INTO " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + field +
                ") VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")" + ", " + FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, value);

        return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);
    }

    //------------------------------------------------------------------------------------

    protected <Data extends BaseData<?>> void updateAccount(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        int appVersion = data.client.appVersion;
        String appName = data.client.appName;
        String platform = data.client.platform;
        String market = data.client.market;
        String language = data.client.language;
        String firebaseID = data.client.firebaseID;
        String ipAddress = data.client.address;
        String uuid = data.client.uuid;
        String country = IPLocationFinder.getCountry(ipAddress);

        if (uuid == null) {

            uuid = Hash.md5(System.currentTimeMillis());
        }

        String statement = "INSERT " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +

                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_APP_VERSION +
                ", " + FIELD_PLATFORM + ", " + FIELD_MARKET + ", " + FIELD_LANGUAGE +
                ", " + FIELD_FIREBASE_ID + ", " + FIELD_IP_ADDRESS + ", " + FIELD_UUID +
                ", " + FIELD_COUNTRY + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +

                "ON DUPLICATE KEY UPDATE " +

                FIELD_APP_VERSION + " = VALUES (" + FIELD_APP_VERSION + "), " +
                FIELD_PLATFORM + " = VALUES (" + FIELD_PLATFORM + "), " +
                FIELD_MARKET + " = VALUES (" + FIELD_MARKET + "), " +
                FIELD_LANGUAGE + " = VALUES (" + FIELD_LANGUAGE + "), " +
                FIELD_FIREBASE_ID + " = VALUES (" + FIELD_FIREBASE_ID + "), " +
                FIELD_IP_ADDRESS + " = VALUES (" + FIELD_IP_ADDRESS + "), " +
                FIELD_UUID + " = VALUES (" + FIELD_UUID + "), " +
                FIELD_COUNTRY + " = VALUES (" + FIELD_COUNTRY + "), " +
                FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, appVersion);
        parameters.put(4, platform);
        parameters.put(5, market);
        parameters.put(6, language);
        parameters.put(7, firebaseID);
        parameters.put(8, ipAddress);
        parameters.put(9, uuid);
        parameters.put(10, country);

        boolean wasSuccessful = BaseDatabaseHelperOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            CACHED_USERS_INFO.add(userID);

            data.response = BaseCodes.RESPONSE_OK;

            if (data.client.uuid == null) {

                data.client.uuid = uuid;

                data.response = BaseCodes.RESPONSE_OK_CHANGE_UUID;
            }
        }
    }

    public <Data extends BaseData<?>> Data getFullProfile(int profileID, Data data) {

        return data;
    }

    public <Data extends BaseData<?>> Data getPublicProfile(int profileID, Data data) {

        return data;
    }

    /*public int getScore(int userID, String appName, String platform) {

        DBResult<Integer> result = getProperty(userID, appName, platform, FIELD_SCORE, Integer.class);

        if (result.isSuccessful()) {

            return result.value;
        }

        return BaseCodes.NOT_FOUND;
    }

    public void addScore(Integer userID, String appName, String platform, String gameName, int score, int playTime, boolean wonGame) {

        synchronized (userID) {

            int currentScore = getScore(userID, appName, platform);

            boolean wasSuccessful = currentScore != BaseCodes.NOT_FOUND;

            int newScore = score + currentScore;

            Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

            wasSuccessful &= setScore(connection, userID, appName, platform, newScore);

            if (wasSuccessful) {

                //wasSuccessful = BaseResources.getInstance().leaderboardManager.addScore(userID, appName, score);
            }

            if (wasSuccessful) {

                BaseConnectionManagerOld.commit(connection);

            } else {

                BaseConnectionManagerOld.rollback(connection);

                Utilities.retryWithin(BaseConfig.ONE_MINUTE, () ->
                        addScore(userID, appName, platform, gameName, score, playTime, wonGame));
            }

            BaseConnectionManagerOld.close(connection);
        }
    }

    public void addScore(Integer userID, String appName, String gameName, int score) {

        String statement = "INSERT " + BaseConfig.TABLE_LEADERBOARD_DAILY + " (" + FIELD_USER_ID +
                ", " + FIELD_APP_NAME + ", " + FIELD_GAME_NAME + ", " + FIELD_SCORE + ")" + " VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " + FIELD_SCORE + " = " + FIELD_SCORE + " + VALUES(" + FIELD_SCORE + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, gameName);
        parameters.put(4, score);

        if (userID == 1) {

            parameters.put(4, 0);
        }

        synchronized (userID) {

            Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

            boolean wasSuccessful = BaseDatabaseHelperOld.insert(connection, TAG, statement, parameters);

            if (wasSuccessful) {

                BaseConnectionManagerOld.commit(connection);

            } else {

                BaseConnectionManagerOld.rollback(connection);

                Utilities.retryWithin(BaseConfig.ONE_MINUTE, () ->
                        addScore(userID, appName, gameName, score));
            }

            BaseConnectionManagerOld.close(connection);
        }
    }

    public void setRecord(Integer userID, String appName, String gameName, int score) {

        String statement = "INSERT " + BaseConfig.TABLE_LEADERBOARD_DAILY + " (" + FIELD_USER_ID +
                ", " + FIELD_APP_NAME + ", " + FIELD_GAME_NAME + ", " + FIELD_SCORE + ") VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " + FIELD_SCORE + " = IF(" + FIELD_SCORE + " < VALUES(" + FIELD_SCORE + ")," +
                " VALUES(" + FIELD_SCORE + "), " + FIELD_SCORE + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, gameName);
        parameters.put(4, score);

        if (userID == 1) {

            parameters.put(4, 0);
        }

        synchronized (userID) {

            Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

            boolean wasSuccessful = BaseDatabaseHelperOld.insert(connection, TAG, statement, parameters);

            if (wasSuccessful) {

                BaseConnectionManagerOld.commit(connection);

            } else {

                BaseConnectionManagerOld.rollback(connection);

                Utilities.retryWithin(BaseConfig.ONE_MINUTE, () ->
                        setRecord(userID, appName, gameName, score));
            }

            BaseConnectionManagerOld.close(connection);
        }
    }

    public void addLeagueScore(Integer userID, String appName, String gameName,
                               int score, int playTime, boolean wonGame, int leagueID) {

        if (userID == 1) {

            score = 0;
        }

        String statement = "INSERT " + BaseConfig.TABLE_LEAGUE_GAMES + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_GAME_NAME +
                ", " + FIELD_PLAY_TIME + ", " + FIELD_SCORE + ", " + FIELD_RESULT +
                ", " + FIELD_LEAGUE_ID + ", " + FIELD_DATE_TIME + ")" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, gameName);
        parameters.put(4, playTime);
        parameters.put(5, score);
        parameters.put(6, wonGame);
        parameters.put(7, leagueID);

        BaseDatabaseHelperOld.insert(TAG, statement, parameters);
    }

    protected boolean setScore(Connection connection, int userID, String appName, String platform, int score) {

        return setProperty(connection, userID, appName, platform, FIELD_SCORE, score);
    }*/
}