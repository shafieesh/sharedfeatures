package com.chainedminds.api.accounting;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.database.DBResult;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class BaseAccountPropertyManager<Data extends BaseData> {

    private static final String TAG = BaseAccountPropertyManager.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_PLATFORM = "Platform";
    private static final String FIELD_CREDENTIAL = "Credential";
    private static final String FIELD_PLAY_TIME = "PlayTime";
    private static final String FIELD_RESULT = "Result";
    private static final String FIELD_DATE_TIME = "DateTime";
    private static final String FIELD_GAME_NAME = "GameName";
    private static final String FIELD_SCORE = "Score";
    private static final String FIELD_LEAGUE_ID = "LeagueID";
    private static final String FIELD_FIREBASE_ID = "FirebaseID";
    private static final String FIELD_IP_ADDRESS = "IPAddress";
    private static final String FIELD_LANGUAGE = "Language";
    private static final String FIELD_UUID = "UUID";
    private static final String FIELD_LAST_UPDATE = "LastUpdate";
    private static final String FIELD_BLOCKED = "Blocked";

    private static final CacheManager<Integer, String> CREDENTIALS_CACHE = new CacheManager<>();
    private static final CacheManager<Integer, String> LANGUAGE_CACHE = new CacheManager<>();

    public static final Map<String, Map<Integer, Long>> USER_ACTIVITY = new HashMap<>();

    public void fetch() {

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " + FIELD_APP_NAME +
                ", " + FIELD_LAST_UPDATE + " FROM " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES;

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String appName = resultSet.getString(FIELD_APP_NAME);
                    long epoch = resultSet.getTimestamp(FIELD_LAST_UPDATE).getTime();

                    USER_ACTIVITY.putIfAbsent(appName, new HashMap<>());

                    USER_ACTIVITY.get(appName).put(userID, epoch);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

            }
        });
    }

    public String getLanguage(int userID, String appName) {

        return LANGUAGE_CACHE.get(userID, () -> {

            String language = getPropertyOld(userID, appName, FIELD_LANGUAGE, String.class);

            if (language == null) {

                language = BaseConfig.LANGUAGE_FA;
            }

            return language;
        });
    }

    public boolean getCredential(int userID, String appName, String platform, AtomicReference<String> credential) {

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

            credential.set(CREDENTIALS_CACHE.get(userID, () -> {

                DBResult<String> result = getProperty(userID, appName, platform, FIELD_CREDENTIAL, String.class);

                if (result.isSuccessful()) {

                    return result.value;
                }

                return null;
            }));

            return true;

        } else {

            DBResult<String> result = getProperty(userID, appName, platform, FIELD_CREDENTIAL, String.class);

            credential.set(result.value);
            return result.isSuccessful();
        }
    }

    public Boolean getBlocked(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_BLOCKED, Boolean.class);
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

        if (uuids != null && uuids.size() > 0) {

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
                    FIELD_BLOCKED + " = FALSE AND " + FIELD_UUID +
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

        if (firebaseIDs != null && firebaseIDs.size() > 0) {

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
                    FIELD_BLOCKED + " = FALSE AND " + FIELD_FIREBASE_ID +
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

        if (ipAddresses != null && ipAddresses.size() > 0) {

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
                    FIELD_BLOCKED + " = FALSE AND " + FIELD_IP_ADDRESS +
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

    public boolean setCredential(int userID, String credential) {

        String statement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " SET " + FIELD_CREDENTIAL + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, credential);
        parameters.put(2, userID);

        return BaseDatabaseHelperOld.update(TAG, statement, parameters, (wasSuccessful, error) -> {

            if (wasSuccessful) {

                CREDENTIALS_CACHE.put(userID, null);
            }
        });
    }

    public boolean setCredential(int userID, String appName, String platform, String credential) {

        boolean wasSuccessful = setProperty(userID, appName, platform, FIELD_CREDENTIAL, credential);

        if (wasSuccessful) {

            if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

                CREDENTIALS_CACHE.put(userID, credential);
            }
        }

        return wasSuccessful;
    }

    public boolean setCredential(Connection connection, int userID,
                                 String appName, String platform, String credential) {

        boolean wasSuccessful = setProperty(connection, userID, appName, platform, FIELD_CREDENTIAL, credential);

        if (wasSuccessful) {

            if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName)) {

                CREDENTIALS_CACHE.put(userID, credential);
            }
        }

        return wasSuccessful;
    }

    public boolean setBlocked(int userID, boolean block) {

        String statement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " SET " + FIELD_BLOCKED + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, block);
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
                ") VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")";

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
                ") VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")";

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
                ") VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, value);

        return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);
    }

    //------------------------------------------------------------------------------------

    /*public Data getPublicProfile0(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int profileID = data.account.id;

        boolean upToDateClient = data.player != null;

        if (upToDateClient) {

            profileID = data.player.id;

            if (profileID == 0) {

                String gamerTag = data.player.gamerTag;

                profileID = BaseResources.getInstance().accountManager.findUserID(gamerTag);
            }
        }

        int subRequest = data.subRequest;

        BaseFileManager fileManager = BaseResources.getInstance().fileManager;

        if (subRequest == BaseCodes.REQUEST_BASIC_DATA) {

            data = getPublicProfile(profileID, data);
        }

        if (subRequest == BaseCodes.REQUEST_FILE_INFO) {

            *//*FileClass file = FileManager.getFileInfo();

            if (file != null) {

                data.file = file;

                data.response = BaseCodes.RESPONSE_OK;
            }*//*
        }

        if (subRequest == BaseCodes.REQUEST_FILE_BYTES) {

            String fileName = profileID + ".jpg";
            int filePath = BaseFileManager.SECTION_PROFILES;

            if (!fileManager.exists(filePath, fileName)) {

                fileName = "1.jpg";
            }

            byte[] fileBytes = fileManager.loadFile(filePath, fileName);

            String encodedString = Base64Helper.encode(fileBytes);

            if (encodedString != null) {

                data.file = BaseClasses.construct(BaseClasses.getInstance().fileClass);
                data.file.base64 = encodedString;

                data.response = BaseCodes.RESPONSE_OK;
            }
        }

        if (subRequest == BaseCodes.REQUEST_FILE_HASH) {

            String fileName = profileID + ".jpg";
            int filePath = BaseFileManager.SECTION_PROFILES;

            if (!fileManager.exists(filePath, fileName)) {

                fileName = "1.jpg";
            }

            BaseFileData file = fileManager.getHash(filePath, fileName);

            if (file != null) {

                data.file = file;

                data.response = BaseCodes.RESPONSE_OK;
            }
        }

        return data;
    }*/

    /*public Data getFullProfile0(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        int profileID = data.player.id;

        int lowerSubRequest = data.lowerSubRequest;

        if (lowerSubRequest == BaseCodes.REQUEST_BASIC_DATA) {

            data = getFullProfile(profileID, data);
        }

        if (lowerSubRequest == BaseCodes.REQUEST_FILE_INFO) {

            *//*FileClass file = FileManager.getFileInfo();

            if (file != null) {

                data.file = file;

                data.response = BaseCodes.RESPONSE_OK;
            }*//*
        }

        BaseFileManager fileManager = BaseResources.getInstance().fileManager;

        if (lowerSubRequest == BaseCodes.REQUEST_FILE_BYTES) {

            String fileName = profileID + ".jpg";
            int filePath = BaseFileManager.SECTION_PROFILES;

            if (!fileManager.exists(filePath, fileName)) {

                fileName = "1.jpg";
            }

            byte[] fileBytes = fileManager.loadFile(filePath, fileName);

            String encodedString = Base64Helper.encode(fileBytes);

            if (encodedString != null) {

                data.file = BaseClasses.construct(BaseClasses.getInstance().fileClass);
                data.file.base64 = encodedString;
            }
        }

        if (lowerSubRequest == BaseCodes.REQUEST_FILE_HASH) {

            String fileName = profileID + ".jpg";
            int filePath = BaseFileManager.SECTION_PROFILES;

            if (!fileManager.exists(filePath, fileName)) {

                fileName = "1.jpg";
            }

            BaseFileData file = fileManager.getHash(filePath, fileName);

            if (file != null) {

                data.file = file;

                data.response = BaseCodes.RESPONSE_OK;
            }
        }

        return data;
    }*/

    public Data getFullProfile(int profileID, Data data) {

        return data;
    }

    public Data getPublicProfile(int profileID, Data data) {

        return data;
    }

    public Data updateProfile(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        int lowerSubRequest = data.lowerSubRequest;

        if (lowerSubRequest == BaseCodes.REQUEST_BASIC_DATA) {

            //data.profile = getPublicProfile(userID);
        }

        if (lowerSubRequest == BaseCodes.REQUEST_FILE_BYTES) {

            if (data.file != null) {

                byte[] fileBytes = Base64Helper.decode(data.file.base64);

                boolean wasSuccessful = BaseResources.getInstance().fileManager
                        .saveFile(BaseFileManager.SECTION_PROFILES, userID + ".jpg", fileBytes);

                if (wasSuccessful) {

                    data.response = BaseCodes.RESPONSE_OK;
                }

                data.file = null;
            }
        }

        return data;
    }

    protected <Account extends BaseAccountData> Account getAccountProperties(int userID, String appName) {

        return getAccountProperties(userID, appName, null);
    }

    protected <AccountData extends BaseAccountData> AccountData getAccountProperties(
            int userID, String appName, AccountData basicAccount) {

        AtomicReference<AccountData> account = new AtomicReference<>(basicAccount);

        String statement = "SELECT * FROM " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                " WHERE " + FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                AccountData accountData = basicAccount;

                if (accountData == null) {

                    accountData = (AccountData) BaseClasses.construct(BaseClasses.getInstance().accountClass);
                }

                accountData.id = resultSet.getInt(FIELD_USER_ID);
                accountData.score = resultSet.getInt(FIELD_SCORE);

                account.set(accountData);
            }
        });

        return account.get();
    }

    public int getScore(int userID, String appName, String platform) {

        DBResult<Integer> result = getProperty(userID, appName, platform, FIELD_SCORE, Integer.class);

        if (result.isSuccessful()) {

            return result.value;
        }

        return BaseCodes.NOT_FOUND;
    }

    public void addScore(Integer userID, String appName, String platform, String gameName, int score, int playTime, boolean wonGame) {

        /*String statement = "INSERT " + BaseConfig.TABLE_GAME_SESSIONS + " (" + FIELD_USER_ID +
                ", " + FIELD_APP_NAME + ", " + FIELD_GAME_NAME + ", " + FIELD_PLAY_TIME +
                ", " + FIELD_SCORE + ", " + FIELD_RESULT + ")" + " VALUES (?, ?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, gameName);
        parameters.put(4, playTime);
        parameters.put(5, score);
        parameters.put(6, wonGame);

        if (userID == 1) {

            parameters.put(5, 0);
        }*/

        //boolean wasSuccessful = DatabaseHelper.insert(connection, TAG, statement, parameters);

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
                        addScore(userID, appName, platform,  gameName, score, playTime, wonGame));
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
    }

    public boolean isCafeChatDataTransferred(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_USER_ID, Integer.class) == userID;
    }
}