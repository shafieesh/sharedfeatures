package com.chainedminds.api.accounting;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.test.Resources;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.database.QueryCallback;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Authentication {

    private static final Cache<String, Integer> LOGIN_ATTEMPTS_CACHE =
            new Cache<>(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER);

    public static void fetch() {

        Username.fetch();
        Email.fetch();
    }

    public static boolean isBruteForcing(String address) {

        LOGIN_ATTEMPTS_CACHE.putIfAbsent(address, new Cache.Record<>(0)
                .accessLifetime(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER)
                .expirationTime(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER));

        int attemptsTimes = LOGIN_ATTEMPTS_CACHE.get(address);

        LOGIN_ATTEMPTS_CACHE.setValue(address, attemptsTimes + 1);

        return attemptsTimes > _Config.BRUTE_FORCE_ALLOWED_ATTEMPTS;
    }

    public static class Email {

        protected static void fetch() {

        }
    }

    public static class Username {

        private static final String TAG = Username.class.getSimpleName();

        protected static final String FIELD_USER_ID = "UserID";
        protected static final String FIELD_USERNAME = "Username";
        protected static final String FIELD_PASSWORD = "Password";

        protected static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

        protected static final Map<Integer, AuthData> MAPPING_USER_ID = new HashMap<>();
        protected static final Map<String, AuthData> MAPPING_USERNAME = new HashMap<>();

        protected static void fetch() {

            String selectStatement = "SELECT " + FIELD_USER_ID + ", " +
                    FIELD_USERNAME + " FROM " + _Config.TABLE_AUTH_USERNAME;

            _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

                private final Map<Integer, AuthData> mappingUserIDs = new HashMap<>();
                private final Map<String, AuthData> mappingUsernames = new HashMap<>();

                @Override
                public void onFetchingData(ResultSet resultSet) throws Exception {

                    while (resultSet.next()) {

                        AuthData authData = new AuthData();
                        authData.userID = resultSet.getInt(FIELD_USER_ID);
                        authData.username = resultSet.getString(FIELD_USERNAME);
                        authData.password = resultSet.getString(FIELD_PASSWORD);

                        mappingUserIDs.put(authData.userID, authData);
                        mappingUsernames.put(authData.username.toLowerCase(), authData);
                    }
                }

                @Override
                public void onFinishedTask(boolean wasSuccessful, Exception error) {

                    if (wasSuccessful) {

                        Utilities.lock(TAG, LOCK.writeLock(), () -> {

                            MAPPING_USER_ID.clear();
                            MAPPING_USER_ID.putAll(mappingUserIDs);

                            MAPPING_USERNAME.clear();
                            MAPPING_USERNAME.putAll(mappingUsernames);
                        });
                    }
                }
            });
        }

        //------------------------

        public static _Data authenticate(_Data data) {

            data.response = _Codes.RESPONSE_NOK;

            if (data.account == null || data.account.username == null || data.account.password == null ||
                    data.client.appName == null || data.client.platform == null || data.client.version == null) {

                data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

                return data;
            }

            String username = data.account.username;
            String password = data.account.password;
            String appName = data.client.appName;
            String platform = data.client.platform;
            String language = data.client.language;
            String address = data.client.address;
            String version = data.client.version;

            if (isBruteForcing(address)) {

                data.response = _Codes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;
                data.message = Messages.get("GENERAL", Messages.General.TOO_MANY_ATTEMPTS, language);
                return data;
            }

            username = Utilities.replaceLocalizedNumbers(username);
            password = Utilities.replaceLocalizedNumbers(password);

            int userID = getUserID(username);

            if (userID != _Config.NOT_FOUND) {

                Boolean passwordValidated = validatePassword(userID, password);

                if (passwordValidated != null) {

                    if (passwordValidated) {

                        Boolean isActive = Resources.getInstance().account.getIsActive(userID);

                        if (isActive != null && !isActive) {

                            data.message = Messages.get("GENERAL", Messages.General.ACCOUNT_DEACTIVATED, language);
                            return data;
                        }

                        String credential = BackendHelper.generateCredential();

                        boolean wasSuccessful = _Resources.get().accountSession
                                .addCredential(userID, credential, appName, platform, version, language);

                        if (wasSuccessful) {

                            data.account.id = userID;
                            data.account.credential = credential;

                            data.response = _Codes.RESPONSE_OK;
                        }

                    } else {

                        data.response = _Codes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

                        data.message = Messages.get("GENERAL",
                                Messages.General.INVALID_USERNAME_OR_PASSWORD, language);
                    }
                }

            } else {

                data.response = _Codes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

                data.message = Messages.get("GENERAL",
                        Messages.General.INVALID_USERNAME_OR_PASSWORD, language);
            }

            return data;
        }

        public static _Data register(_Data data) {

            data.response = _Codes.RESPONSE_NOK;

            if (data.account == null || data.account.username == null || data.account.password == null) {

                data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

                return data;
            }

            String username = Utilities.replaceLocalizedNumbers(data.account.username);
            String password = Utilities.replaceLocalizedNumbers(data.account.password);
            String language = data.client.language;

            if (username.length() < 4) {

                data.message = Messages.get("GENERAL",
                        Messages.General.USERNAME_IS_TOO_SHORT, data.client.language);
                return data;
            }

            if (password.length() < 6) {

                data.message = Messages.get("GENERAL",
                        Messages.General.PASSWORD_IS_TOO_SHORT, data.client.language);
                return data;
            }

            if (getUserID(username) == _Codes.NOT_FOUND) {

                int userID = Resources.getInstance().account.registerAccount(username);

                if (userID != -1) {

                    String insertStatement = "INSERT " + _Config.TABLE_AUTH_USERNAME +
                            " (" + FIELD_USER_ID + ", " + FIELD_USERNAME + ", " + FIELD_PASSWORD + ") VALUES (?, ?, ?)";

                    Map<Integer, Object> parameters = new HashMap<>();
                    parameters.put(1, userID);
                    parameters.put(2, username);
                    parameters.put(3, password);

                    _DatabaseOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

                        if (wasSuccessful) {

                            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                                AuthData authData = new AuthData();
                                authData.userID = userID;
                                authData.username = username;
                                authData.password = password;

                                MAPPING_USER_ID.put(userID, authData);
                                MAPPING_USERNAME.put(username.toLowerCase(), authData);
                            });

                            data.account.id = userID;
                            data.response = _Codes.RESPONSE_OK;
                        }
                    });
                }

            } else {

                data.response = _Codes.RESPONSE_IS_REGISTERED_BEFORE;

                data.message = Messages.get("GENERAL",
                        Messages.General.USERNAME_HAS_REGISTERED_BEFORE, language);
            }

            return data;
        }

        public static _Data setPassword(_Data data) {

            data.response = _Codes.RESPONSE_NOK;

            if (data.account == null || data.account.password == null) {

                data.message = Messages.get("GENERAL",
                        Messages.General.MISSING_DATA, data.client.language);

                return data;
            }

            if (data.account.password.length() < 6) {

                data.message = Messages.get("GENERAL",
                        Messages.General.PASSWORD_IS_TOO_SHORT, data.client.language);

                return data;
            }

            int userID = data.account.id;
            String password = data.account.password;

            boolean wasSuccessful = setPassword(userID, password);

            if (wasSuccessful) {

                data.response = _Codes.RESPONSE_OK;
            }

            return data;
        }

        //------------------------

        public static AuthData get(int userID) {

            AtomicReference<AuthData> data = new AtomicReference<>();

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                if (MAPPING_USER_ID.containsKey(userID)) {

                    data.set(MAPPING_USER_ID.get(userID));
                }
            });

            return data.get();
        }

        public static int getUserID(String username) {

            AtomicInteger userID = new AtomicInteger(_Config.NOT_FOUND);

            if (username == null) {

                return userID.get();
            }

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                String lowercaseUsername = username.toLowerCase();

                if (MAPPING_USERNAME.containsKey(lowercaseUsername)) {

                    userID.set(MAPPING_USERNAME.get(lowercaseUsername).userID);
                }
            });

            return userID.get();
        }

        public static String getUsername(int userID) {

            AtomicReference<String> username = new AtomicReference<>();

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                if (MAPPING_USER_ID.containsKey(userID)) {

                    username.set(MAPPING_USER_ID.get(userID).username);
                }
            });

            return username.get();
        }

        public static String getPassword(int userID) {

            AtomicReference<String> password = new AtomicReference<>();

            Utilities.lock(TAG, LOCK.readLock(), () -> {

                if (MAPPING_USER_ID.containsKey(userID)) {

                    password.set(MAPPING_USER_ID.get(userID).password);
                }
            });

            return password.get();
        }

        public static void getUsernameMap(String tag, Utilities.GrantAccess<Map<String, AuthData>> job) {

            Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USERNAME));
        }

        public static void getUserIDMap(String tag, Utilities.GrantAccess<Map<Integer, AuthData>> job) {

            Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USER_ID));
        }

        public static void getUsernameList(String tag, Utilities.GrantAccess<Set<String>> job) {

            Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USERNAME.keySet()));
        }

        //------------------------

        public static boolean setPassword(int userID, String newPassword) {

            Connection connection = _ConnectionOld.get(_ConnectionOld.MANUAL_COMMIT);

            String oldPassword = getPassword(userID);

            boolean wasSuccessful = setProperty(connection, userID, FIELD_PASSWORD, newPassword);

            wasSuccessful &= _Log.log(connection, userID, "Password", oldPassword + " -> " + newPassword);

            if (wasSuccessful) {

                _ConnectionOld.commit(connection);

            } else {

                _ConnectionOld.rollback(connection);
            }

            _ConnectionOld.close(connection);

            return wasSuccessful;
        }

        protected static Boolean validatePassword(int userID, String password) {

            String storedPassword = getPassword(userID);

            if (storedPassword != null) {

                return storedPassword.equals(password);
            }

            return null;
        }

        //------------------------

        protected static <T> T getProperty(int userID, String field, Class<T> T) {

            return getProperty(null, userID, field, T);
        }

        protected static <T> T getProperty(Connection connection, int userID, String field, Class<T> T) {

            AtomicReference<T> value = new AtomicReference<>();

            String statement = "SELECT " + field + " FROM " + _Config.TABLE_ACCOUNTS +
                    " WHERE " + FIELD_USER_ID + " = ?";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, userID);

            QueryCallback queryCallback = resultSet -> {

                if (resultSet.next()) {

                    value.set(resultSet.getObject(field, T));
                }
            };

            if (connection != null) {

                _DatabaseOld.query(connection, TAG, statement, parameters, queryCallback);

            } else {

                _DatabaseOld.query(TAG, statement, parameters, queryCallback);
            }

            if (value.get() == null) {

                if (T == Integer.class) {

                    value.set((T) Integer.valueOf(_Config.NOT_FOUND));
                }
            }

            return value.get();
        }

        protected static boolean setProperty(int userID, String fieldName, Object value) {

            return setProperty(null, userID, fieldName, value);
        }

        protected static boolean setProperty(Connection connection, int userID, String fieldName, Object value) {

            String statement = "UPDATE " + _Config.TABLE_ACCOUNTS + " SET " +
                    fieldName + " = ? WHERE " + FIELD_USER_ID + " = ?";

            Map<Integer, Object> parameters = new HashMap<>();

            parameters.put(1, value);
            parameters.put(2, userID);

            if (connection != null) {

                return _DatabaseOld.update(connection, TAG, statement, parameters);

            } else {

                return _DatabaseOld.update(TAG, statement, parameters);
            }
        }

        //------------------------

        public static class AuthData {

            public int userID;
            public String username;
            public String password;
        }
    }
}
