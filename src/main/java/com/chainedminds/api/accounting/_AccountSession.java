package com.chainedminds.api.accounting;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.api.IPLocationFinder;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.test.Resources;
import com.chainedminds.utilities.Cache;
import com.chainedminds.utilities.Task;
import com.chainedminds.utilities.database.DBResult;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class _AccountSession {

    private static final String TAG = _AccountSession.class.getSimpleName();

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_CREDENTIAL = "Credential";
    protected static final String FIELD_APP_NAME = "AppName";
    protected static final String FIELD_PLATFORM = "Platform";
    protected static final String FIELD_VERSION = "Version";
    protected static final String FIELD_LANGUAGE = "Language";
    protected static final String FIELD_FIREBASE_ID = "FirebaseID";
    protected static final String FIELD_IP_ADDRESS = "IPAddress";
    protected static final String FIELD_COUNTRY = "Country";
    protected static final String FIELD_LAST_UPDATE = "LastUpdate";

    protected static final Cache<String, Integer> CREDENTIALS_CACHE = new Cache<>();
    protected static final Cache<Integer, Map<String, String>> LANGUAGE_CACHE = new Cache<>();

    protected final Set<String> CACHED_USERS_INFO = new HashSet<>();
    public static final Map<String, Map<Integer, Long>> USER_ACTIVITY = new HashMap<>();

    public void start() {

        Task.add(Task.Data.build()
                .setName("Clear Cached Users Info")
                .setTime(0, 0, 0)
                .setInterval(0, 0, _Config.CACHED_USERS_INFO_REFRESH_RATE, 0)
                .setTimingListener(task -> CACHED_USERS_INFO.clear())
                .schedule());


        Task.add(Task.Data.build()
                .setName("Remove Stale Sessions")
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> removeStaleSessions())
                .schedule());
    }

    public void fetch() {

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " + FIELD_APP_NAME +
                ", " + FIELD_LAST_UPDATE + " FROM " + _Config.TABLE_ACCOUNTS_SESSIONS;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

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

    //------------------------

    public Set<Integer> getUserIDs(String appName) {

        DBResult<Set<Integer>> result = getProperties(appName, FIELD_USER_ID, new HashSet<>(), Integer.class);

        return result.value;
    }

    public List<_AccountData> getAccountsCompact(String appName) {

        List<_AccountData> accounts = new ArrayList<>();

        if (appName != null) {

            Set<Integer> userIDs = _Resources.getInstance().accountSession.getUserIDs(appName);

            for (int userID : userIDs) {

                _AccountData account = Resources.getInstance().account.getAccount(userID);

                accounts.add(account);
            }
        }

        Comparator<_AccountData> comparator = Comparator.comparing(account -> account.username);

        accounts.sort(comparator);

        return accounts;
    }

    //------------------------

    public boolean getUserIdByCredential(String credential, AtomicReference<Integer> userID) {

        Integer cachedUserID = CREDENTIALS_CACHE.get(credential);

        if (cachedUserID == null) {

            DBResult<Integer> result = getProperty(credential, FIELD_USER_ID, Integer.class);

            if (result.isSuccessful()) {

                CREDENTIALS_CACHE.put(credential, result.value);
                userID.set(result.value);
            }

            return result.isSuccessful();

        } else {

            userID.set(cachedUserID);
            return true;
        }
    }

    public boolean addCredential(int userID, String credential, String appName,
                                 String platform, String version, String language) {

        String updateStatement = "INSERT INTO " + _Config.TABLE_ACCOUNTS_SESSIONS + " (" +
                FIELD_USER_ID + ", " + FIELD_CREDENTIAL + ", " + FIELD_APP_NAME + ", " +
                FIELD_PLATFORM + ", " + FIELD_VERSION + ", " + FIELD_LANGUAGE +
                ") VALUES (?, ?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);
        parameters.put(2, credential);
        parameters.put(3, appName);
        parameters.put(4, platform);
        parameters.put(5, version);
        parameters.put(6, language);

        boolean wasSuccessful = _DatabaseOld.insert(TAG, updateStatement, parameters);

        if (wasSuccessful) {

            CREDENTIALS_CACHE.put(credential, userID);
        }

        return wasSuccessful;
    }

    protected Boolean validateCredential(int userID, @NotNull String credential) {

        AtomicReference<Integer> storedUserID = new AtomicReference<>();

        boolean wasSuccessful = getUserIdByCredential(credential, storedUserID);

        if (wasSuccessful) {

            if (storedUserID.get() != null) {

                return userID == storedUserID.get();

            } else {

                return false;
            }
        }

        return null;
    }

    //------------------------

    public String getLanguage(int userID, String appName) {

        Map<String, String> appNamePlatformCredential = LANGUAGE_CACHE.getOrPut(userID, HashMap::new);

        if (!appNamePlatformCredential.containsKey(appName)) {

            String language = getPropertyOld(userID, appName, FIELD_LANGUAGE, String.class);

            if (language == null) {

                language = _Config.LANGUAGE_DEFAULT;
            }

            appNamePlatformCredential.put(appName, language);

            return language;

        } else {

            return appNamePlatformCredential.get(appName);
        }
    }

    //------------------------

    public String getFirebaseID(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_FIREBASE_ID, String.class);
    }

    public Set<String> getFirebaseIDs(int userID) {

        DBResult<Set<String>> result = getProperties(userID, FIELD_FIREBASE_ID, new HashSet<>(), String.class);

        return result.value;
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

            String statement = "SELECT DISTINCT " + FIELD_USER_ID +
                    " FROM " + _Config.TABLE_ACCOUNTS_SESSIONS +
                    " WHERE " + FIELD_FIREBASE_ID + " IN (" + questionMarksArray + ")";

            _DatabaseOld.query(TAG, statement, parameters, resultSet -> {

                while (resultSet.next()) {

                    userIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            });
        }

        return userIDs;
    }

    public boolean removeFirebaseID(int userID, String firebaseID) {

        String updateStatement = "UPDATE " + _Config.TABLE_ACCOUNTS_SESSIONS + " SET " +
                FIELD_FIREBASE_ID + " = NULL WHERE " + FIELD_USER_ID + " = ? AND " + FIELD_FIREBASE_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, firebaseID);

        return _DatabaseOld.update(TAG, updateStatement, parameters);
    }

    //------------------------

    public String getIPAddress(int userID, String appName) {

        return getPropertyOld(userID, appName, FIELD_IP_ADDRESS, String.class);
    }

    public Set<String> getIPAddresses(int userID) {

        DBResult<Set<String>> result = getProperties(userID, FIELD_IP_ADDRESS, new HashSet<>(), String.class);

        return result.value;
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

            String statement = "SELECT DISTINCT " + FIELD_USER_ID +
                    " FROM " + _Config.TABLE_ACCOUNTS_SESSIONS +
                    " WHERE " + FIELD_IP_ADDRESS + " IN (" + questionMarksArray + ")";

            _DatabaseOld.query(TAG, statement, parameters, resultSet -> {

                while (resultSet.next()) {

                    userIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            });
        }

        return userIDs;
    }

    //------------------------------------------------------------------------------------

    protected List<SessionData> getActiveSessions(int userID) {

        List<SessionData> activeSessions = new ArrayList<>();

        String statement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS_SESSIONS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        _DatabaseOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

            final List<SessionData> sessions = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    SessionData session = new SessionData();
                    session.userID = resultSet.getInt(FIELD_USER_ID);
                    session.credential = resultSet.getString(FIELD_CREDENTIAL);
                    session.appName = resultSet.getString(FIELD_APP_NAME);
                    session.platform = resultSet.getString(FIELD_PLATFORM);
                    session.version = resultSet.getString(FIELD_VERSION);
                    session.language = resultSet.getString(FIELD_LANGUAGE);
                    session.firebaseID = resultSet.getString(FIELD_FIREBASE_ID);
                    session.ipAddress = resultSet.getString(FIELD_IP_ADDRESS);
                    session.country = resultSet.getString(FIELD_COUNTRY);
                    session.lastUpdate = resultSet.getTimestamp(FIELD_LAST_UPDATE).getTime();

                    sessions.add(session);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    activeSessions.addAll(sessions);
                }
            }
        });

        return activeSessions;
    }

    protected boolean terminateSession(int userID, String credential) {

        Boolean credentialIsValid = validateCredential(userID, credential);

        if (credentialIsValid == null) {

            return false;
        }

        if (credentialIsValid) {

            String statement = "DELETE FROM " + _Config.TABLE_ACCOUNTS_SESSIONS + " WHERE " +
                    FIELD_USER_ID + " = ? AND " + FIELD_CREDENTIAL + " = ?";

            Map<Integer, Object> parameters = new HashMap<>();
            parameters.put(1, userID);
            parameters.put(2, credential);

            boolean wasSuccessful = _DatabaseOld.update(TAG, statement, parameters);

            if (wasSuccessful) {

                CREDENTIALS_CACHE.remove(credential);

                return true;
            }
        }

        return false;
    }

    protected void removeStaleSessions() {

        List<String> staleCredentials = new ArrayList<>();

        String statement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS_SESSIONS +
                " WHERE " + FIELD_LAST_UPDATE + " < DATE_SUB(NOW(), INTERVAL 7 DAY)";

        _DatabaseOld.query(TAG, statement, new TwoStepQueryCallback() {

            final List<String> credentials = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    credentials.add(resultSet.getString(FIELD_CREDENTIAL));
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    staleCredentials.addAll(credentials);
                }
            }
        });

        if (!staleCredentials.isEmpty()) {

            String credentialsArray = String.join("', '", staleCredentials);

            statement = "DELETE FROM " + _Config.TABLE_ACCOUNTS_SESSIONS + " WHERE " +
                    FIELD_CREDENTIAL + " IN ('" + credentialsArray + "')";

            boolean wasSuccessful = _DatabaseOld.update(TAG, statement);

            if (wasSuccessful) {

                CREDENTIALS_CACHE.remove(staleCredentials);
            }
        }
    }

    //------------------------------------------------------------------------------------

    @Deprecated
    private <T> T getPropertyOld(int userID, String appName, String field, Class<T> T) {

        AtomicReference<T> value = new AtomicReference<>();

        String statement = "SELECT " + field + " FROM " +
                _Config.TABLE_ACCOUNTS_SESSIONS + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);

        _DatabaseOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                value.set(resultSet.getObject(field, T));
            }
        });

        if (value.get() == null) {

            if (T == Integer.class) {

                value.set((T) Integer.valueOf(_Config.NOT_FOUND));
            }
        }

        return value.get();
    }

    public <T> DBResult<T> getProperty(String credential, String field, Class<T> T) {

        DBResult<T> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " + _Config.TABLE_ACCOUNTS_SESSIONS +
                " WHERE " + FIELD_CREDENTIAL + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, credential);

        _DatabaseOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

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

        return result;
    }

    public <T> DBResult<T> getProperty(int userID, String appName, String platform, String field, Class<T> T) {

        DBResult<T> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " +
                _Config.TABLE_ACCOUNTS_SESSIONS + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ? AND " + FIELD_PLATFORM + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);

        _DatabaseOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

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

                result.value = (T) (Integer) _Config.NOT_FOUND;
            }
        }

        return result;
    }

    public <Type, Series extends Collection<Type>> DBResult<Series> getProperties(
            String appName, String field, Series values, Class<Type> type) {

        DBResult<Series> result = new DBResult<>();

        String statement = "SELECT " + field + " FROM " +
                _Config.TABLE_ACCOUNTS_SESSIONS +
                " WHERE " + FIELD_APP_NAME + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, appName);

        _DatabaseOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

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
                _Config.TABLE_ACCOUNTS_SESSIONS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        _DatabaseOld.query(TAG, statement, parameters, new TwoStepQueryCallback() {

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

        return setProperty(null, userID, appName, platform, field, value);
    }

    public boolean setProperty(Connection connection, int userID, String appName, String platform, String field, Object value) {

        String updateStatement = "INSERT INTO " + _Config.TABLE_ACCOUNTS_SESSIONS + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PLATFORM + ", " + field +
                ") VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " + field + " = VALUES (" + field + ")" +
                ", " + FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);
        parameters.put(4, value);

        if (connection == null) {

            return _DatabaseOld.update(TAG, updateStatement, parameters);

        } else {

            return _DatabaseOld.update(connection, TAG, updateStatement, parameters);
        }
    }

    public boolean setProperty(Connection connection, int userID, String appName, String field, Object value) {

        String updateStatement = "INSERT INTO " + _Config.TABLE_ACCOUNTS_SESSIONS + " (" +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + field +
                ") VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " + field +
                " = VALUES (" + field + ")" + ", " + FIELD_LAST_UPDATE + " = NOW()";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, value);

        return _DatabaseOld.update(connection, TAG, updateStatement, parameters);
    }

    //------------------------------------------------------------------------------------

    protected <Data extends _Data<?>> void updateAccount(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        String credential = data.account.credential;
        String version = data.client.version;
        String language = data.client.language;
        String firebaseID = data.client.firebaseID;
        String ipAddress = data.client.address;
        String country = data.client.country;

        if (country == null) {

            country = IPLocationFinder.getCountry(ipAddress);
        }

        String statement = "UPDATE " + _Config.TABLE_ACCOUNTS_SESSIONS + " SET " +
                FIELD_VERSION + " = COALESCE(?, " + FIELD_VERSION + "), " +
                FIELD_LANGUAGE + " = COALESCE(?, " + FIELD_LANGUAGE + "), " +
                FIELD_FIREBASE_ID + " = COALESCE(?, " + FIELD_FIREBASE_ID + "), " +
                FIELD_IP_ADDRESS + " = ?, " + FIELD_COUNTRY + " = ?, " +
                FIELD_LAST_UPDATE + " = NOW() WHERE " + FIELD_CREDENTIAL + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, (version == null || version.equals("0")) ? null : version);
        parameters.put(2, language);
        parameters.put(3, firebaseID);
        parameters.put(4, ipAddress);
        parameters.put(5, country);
        parameters.put(6, credential);

        boolean wasSuccessful = _DatabaseOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            CACHED_USERS_INFO.add(credential);

            data.response = _Codes.RESPONSE_OK;
        }
    }

    public <Data extends _Data<?>> Data getFullProfile(int profileID, Data data) {

        return data;
    }

    public <Data extends _Data<?>> Data getPublicProfile(int profileID, Data data) {

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

    //------------------------------------------------------------------------------------

    public static class SessionData {

        public int userID;
        public String credential;
        public String appName;
        public String platform;
        public String version;
        public String language;
        public String firebaseID;
        public String ipAddress;
        public String country;
        public long lastUpdate;
    }
}