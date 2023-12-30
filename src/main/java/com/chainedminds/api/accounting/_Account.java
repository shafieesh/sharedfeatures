package com.chainedminds.api.accounting;

import com.chainedminds._Classes;
import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.api.ActivityListener;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.QueryCallback;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Account<Data extends _Data<?>> {

    private static final String TAG = _Account.class.getSimpleName();

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_USERNAME = "Username";
    protected static final String FIELD_PASSWORD = "Password";
    protected static final String FIELD_IS_ACTIVE = "IsActive";
    protected static final String FIELD_NAME = "Name";
    protected static final String FIELD_PHONE_NUMBER = "PhoneNumber";
    protected static final String FIELD_EMAIL = "Email";
    protected static final String FIELD_REGISTRATION_TIME = "RegistrationTime";
    protected static final String FIELD_LAST_UPDATE = "LastUpdate";

    private static final Cache<String, Integer> LOGIN_ATTEMPTS_CACHE =
            new Cache<>(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER);

    protected final Map<Integer, String> MAPPING_USERNAME = new HashMap<>();
    protected final Set<String> INDEX_USERNAME = new HashSet<>();

    protected final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        Task.add(Task.Data.build()
                .setName("FetchUsers")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 5, 0)
                .setTimingListener(task -> fetch())
                .runNow()
                .schedule());

        addTasks();
    }

    protected void addTasks() {

        //OVERRIDES IN UPPER CLASSES
    }

    protected void fetch() {

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " +
                FIELD_USERNAME + " FROM " + _Config.TABLE_ACCOUNTS;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<Integer, String> usernames = new HashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String username = resultSet.getString(FIELD_USERNAME);

                    usernames.put(userID, username);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        MAPPING_USERNAME.clear();
                        MAPPING_USERNAME.putAll(usernames);

                        INDEX_USERNAME.clear();

                        MAPPING_USERNAME.values().forEach(username -> {

                            if (username != null) {

                                INDEX_USERNAME.add(username.toLowerCase());
                            }
                        });
                    });
                }
            }
        });
    }

    //------------------------------------------------------------------------------------

    public int findUserID(String username) {

        AtomicInteger userID = new AtomicInteger(_Config.NOT_FOUND);

        if (username == null) {

            return userID.get();
        }

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            String lowercaseUsername = username.toLowerCase();

            if (INDEX_USERNAME.contains(lowercaseUsername)) {

                MAPPING_USERNAME.forEach((key, value) -> {

                    if (value != null && Objects.equals(lowercaseUsername, value.toLowerCase())) {

                        userID.set(key);
                    }
                });
            }
        });

        return userID.get();
    }

    protected boolean validateUserID(int userID) {

        AtomicBoolean userIDExists = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> userIDExists.set(MAPPING_USERNAME.containsKey(userID)));

        return userIDExists.get();
    }

    //------------------------

    public String getUsername(int userID) {

        AtomicReference<String> username = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> username.set(MAPPING_USERNAME.get(userID)));

        return username.get();
    }

    public void getUsernameMap(String tag, Utilities.GrantAccess<Map<Integer, String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USERNAME));
    }

    public void getUsernameList(String tag, Utilities.GrantAccess<Set<String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(INDEX_USERNAME));
    }

    protected boolean checkIfUsernameRegistered(String username) {

        AtomicBoolean usernameRegistered = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            String lowerCasedUsername = username.toLowerCase();

            usernameRegistered.set(INDEX_USERNAME.contains(lowerCasedUsername));
        });

        return usernameRegistered.get();
    }

    //------------------------

    protected String getPassword(int userID) {

        String storedPassword = getProperty(userID, FIELD_PASSWORD, String.class);

        if (storedPassword != null) {

            storedPassword = Utilities.replaceLocalizedNumbers(storedPassword);
        }

        return storedPassword;
    }

    protected String getPassword(Connection connection, int userID) {

        String storedPassword = getProperty(connection, userID, FIELD_PASSWORD, String.class);

        if (storedPassword != null) {

            storedPassword = Utilities.replaceLocalizedNumbers(storedPassword);
        }

        return storedPassword;
    }

    public boolean setPassword(int userID, String newPassword) {

        Connection connection = _ConnectionOld.get(_ConnectionOld.MANUAL_COMMIT);

        String oldPassword = getPassword(connection, userID);

        boolean wasSuccessful = setProperty(connection, userID, FIELD_PASSWORD, newPassword);

        wasSuccessful &= _Logs.log(connection, userID, "Password", oldPassword + " -> " + newPassword);

        if (wasSuccessful) {

            _ConnectionOld.commit(connection);

        } else {

            _ConnectionOld.rollback(connection);
        }

        _ConnectionOld.close(connection);

        return wasSuccessful;
    }

    protected Boolean validatePassword(int userID, String password) {

        String storedPassword = getPassword(userID);

        if (storedPassword != null) {

            return storedPassword.equals(password);
        }

        return null;
    }

    //------------------------

    protected Boolean getIsActive(int userID) {

        return getProperty(userID, FIELD_IS_ACTIVE, Boolean.class);
    }
    //------------------------

    public <AccountData extends _AccountData> AccountData getAccount(int userID) {

        AtomicReference<AccountData> account = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        _DatabaseOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                AccountData foundAccount = (AccountData) _Classes
                        .construct(_Classes.getInstance().accountClass);

                foundAccount.id = userID;
                foundAccount.username = resultSet.getString(FIELD_USERNAME);
                foundAccount.password = resultSet.getString(FIELD_PASSWORD);
                foundAccount.isActive = resultSet.getBoolean(FIELD_IS_ACTIVE);
                foundAccount.name = resultSet.getString(FIELD_NAME);
                foundAccount.phoneNumber = resultSet.getLong(FIELD_PHONE_NUMBER);
                foundAccount.email = resultSet.getString(FIELD_EMAIL);
                foundAccount.registrationTime = resultSet.getTimestamp(FIELD_REGISTRATION_TIME).getTime();
                foundAccount.lastUpdate = resultSet.getTimestamp(FIELD_LAST_UPDATE).getTime();

                account.set(foundAccount);
            }
        });

        return account.get();
    }

    protected int registerAccount(String username, String password, String name) {

        AtomicInteger userID = new AtomicInteger(_Config.NOT_FOUND);

        String insertStatement = "INSERT " + _Config.TABLE_ACCOUNTS +
                " (" + FIELD_USERNAME + ", " + FIELD_PASSWORD + ", " + FIELD_NAME + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, username);
        parameters.put(2, password);
        parameters.put(3, name);

        _DatabaseOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                userID.set(generatedID);

                Utilities.lock(TAG, LOCK.writeLock(), () -> {

                    MAPPING_USERNAME.put(userID.get(), username);
                    INDEX_USERNAME.add(username.toLowerCase());
                });
            }
        });

        return userID.get();
    }

    public List<_AccountData> searchUsernames(@NotNull String username) {

        username = Objects.requireNonNull(username, "Username should not be null");

        List<_AccountData> foundAccounts = new ArrayList<>();

        String query = username.toLowerCase();

        getUsernameMap(TAG, usernames -> usernames.forEach((loopingUserID, loopingUsername) -> {

            if (loopingUsername.toLowerCase().contains(query)) {

                _AccountData account = new _AccountData();
                account.id = loopingUserID;
                account.username = loopingUsername;

                foundAccounts.add(account);
            }
        }));

        Comparator<_AccountData> comparator = Comparator.comparingInt(account -> account.username.length());

        foundAccounts.sort(comparator);

        if (foundAccounts.size() > 100) {

            return foundAccounts.subList(0, 99);

        } else {

            return foundAccounts;
        }
    }

    public List<_AccountData> getAccountsCompact(String appName) {

        List<_AccountData> accounts = new ArrayList<>();

        if (appName != null) {

            Set<Integer> userIDs = _Resources.getInstance().accountSession.getUserIDs(appName);

            getUsernameMap(TAG, usernames -> userIDs.forEach(userID -> {

                _AccountData account = new _AccountData();
                account.id = userID;
                account.username = usernames.get(userID);

                accounts.add(account);
            }));
        }

        Comparator<_AccountData> comparator = Comparator.comparing(account -> account.username);

        accounts.sort(comparator);

        return accounts;
    }

    public List<_AccountData> getAccountsCompact() {

        List<_AccountData> accounts = new ArrayList<>();

        getUsernameMap(TAG, usernames -> usernames.forEach((loopingUserID, loopingUsername) -> {

            _AccountData account = new _AccountData();
            account.id = loopingUserID;
            account.username = loopingUsername;

            accounts.add(account);
        }));

        Comparator<_AccountData> comparator = Comparator.comparing(account -> account.username);

        accounts.sort(comparator);

        return accounts;
    }

    public _AccountData getAccount(int userID, String appName, int targetUserID) {

        AtomicReference<_AccountData> accountHolder = new AtomicReference<>();

        if (_AccountPermissions.hasPermission(userID, appName, "USER_MANAGEMENT")) {

            _AccountData account = getAccount(targetUserID);

            account.password = null;
            account.permissions = _AccountPermissions.getPermissions(targetUserID, appName);

            accountHolder.set(account);
        }

        return accountHolder.get();
    }

    //------------------------

    public boolean isBruteForcing(String address) {

        LOGIN_ATTEMPTS_CACHE.putIfAbsent(address, new Cache.Record<>(0)
                .accessLifetime(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER)
                .expirationTime(_Config.BRUTE_FORCE_REMOVE_BLOCKAGE_AFTER));

        int attemptsTimes = LOGIN_ATTEMPTS_CACHE.get(address);

        LOGIN_ATTEMPTS_CACHE.setValue(address, attemptsTimes + 1);

        return attemptsTimes > _Config.BRUTE_FORCE_ALLOWED_ATTEMPTS;
    }

    //------------------------------------------------------------------------------------

    protected final <T> T getProperty(int userID, String field, Class<T> T) {

        return getProperty(null, userID, field, T);
    }

    protected final <T> T getProperty(Connection connection, int userID, String field, Class<T> T) {

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

    protected final boolean setProperty(int userID, String fieldName, Object value) {

        return setProperty(null, userID, fieldName, value);
    }

    protected final boolean setProperty(Connection connection, int userID, String fieldName, Object value) {

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

    //------------------------------------------------------------------------------------

    public Data authenticateUsername(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.username == null || data.account.password == null ||
                data.client.appName == null || data.client.platform == null) {

            data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String username = data.account.username;
        String password = data.account.password;
        String appName = data.client.appName;
        String platform = data.client.platform;
        String language = data.client.language;
        String address = data.client.address;
        int appVersion = data.client.appVersion;

        if (isBruteForcing(address)) {

            //BaseNotificationManager.reportBruteForce(address, username, password);

            data.response = _Codes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

            data.message = Messages.get("GENERAL",
                    Messages.General.TOO_MANY_ATTEMPTS, language);

            return data;
        }

        username = Utilities.replaceLocalizedNumbers(username);
        password = Utilities.replaceLocalizedNumbers(password);

        int userID = findUserID(username);

        if (userID != _Config.NOT_FOUND) {

            Boolean isActive = getIsActive(userID);

            if (isActive != null && !isActive) {

                data.message = Messages.get("GENERAL",
                        Messages.General.ACCOUNT_DEACTIVATED, language);

                return data;
            }

            Boolean passwordValidated = validatePassword(userID, password);

            if (passwordValidated != null) {

                if (passwordValidated) {

                    String credential = BackendHelper.generateCredential();

                    boolean wasSuccessful = _Resources.getInstance().accountSession
                            .addCredential(userID, credential, appName, platform, appVersion, language);

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

    public Data authenticateCredential(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.credential == null) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String credential = data.account.credential;

        //---------CHECK IF USER ID IS VALID-------

        if (!_Config.DEBUG_MODE) {

            if (!validateUserID(userID)) {

                data.response = _Codes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }

            //---------CHECK IF CREDENTIAL IS VALID-------

            Boolean credentialIsValid = _Resources.getInstance().accountSession.validateCredential(userID, credential);

            if (credentialIsValid == null) {

                data.response = _Codes.RESPONSE_NOK;
                return data;
            }

            if (!credentialIsValid) {

                data.response = _Codes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }
        }

        //------------------------------------------------
        //------------UPDATE USER'S ACCOUNT---------------

        if (!_Resources.getInstance().accountSession.CACHED_USERS_INFO.contains(credential)) {

            NettyServer.execute(() -> _Resources.getInstance().accountSession.updateAccount(data));
        }

        ActivityListener.setBecameOnline(userID);

        //------------REQUESTS------------------------

        return onSubRequestReceived(data);
    }

    public Data onSubRequestReceived(Data data) {

        int userID = data.account.id;
        int subRequest = data.subRequest;

        if (subRequest == 0) {

            data.response = _Codes.RESPONSE_OK;
            data.message = null;
        }

        return data;
    }

    public Data registerAccount(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.username == null || data.account.password == null) {

            data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String username = data.account.username;
        String password = data.account.password;
        String name = data.account.name;
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

        username = Utilities.replaceLocalizedNumbers(username);
        password = Utilities.replaceLocalizedNumbers(password);

        if (!checkIfUsernameRegistered(username)) {

            /*if (BaseBlackListManager.isBlocked(ipAddress, BaseBlackListManager.TYPE_IP_ADDRESS,
                    BaseBlackListManager.REASON_ADMIN_KICK) == BaseBlackListManager.STATE_KICKED ||
                    BaseBlackListManager.isBlocked(uuid, BaseBlackListManager.TYPE_UUID,
                            BaseBlackListManager.REASON_ADMIN_KICK) == BaseBlackListManager.STATE_KICKED) {

                data.response = BaseCodes.RESPONSE_NOK;

                data.message = Messages.get("GENERAL",
                        Messages.General.YOU_ARE_BLOCKED_FOR_SOME_TIMES, language);

                return data;
            }*/

            int userID = registerAccount(username, password, name);

            if (userID != -1) {

                data.account.id = userID;
                data.response = _Codes.RESPONSE_OK;
            }

        } else {

            data.response = _Codes.RESPONSE_IS_REGISTERED_BEFORE;

            data.message = Messages.get("GENERAL",
                    Messages.General.USERNAME_HAS_REGISTERED_BEFORE, language);
        }

        return data;
    }

    protected Data setPhoneNumber(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || (data.account.phoneNumber + "").length() == 0) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        long phoneNumber = data.account.phoneNumber;

        boolean wasSuccessful = setProperty(userID, FIELD_PHONE_NUMBER, phoneNumber);

        if (wasSuccessful) {

            data.response = _Codes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setEmail(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.email == null || data.account.email.isEmpty()) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String email = data.account.email;

        boolean wasSuccessful = setProperty(userID, FIELD_EMAIL, email);

        if (wasSuccessful) {

            data.response = _Codes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setName(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.name == null) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String name = data.account.name;

        boolean wasSuccessful = setProperty(userID, FIELD_NAME, name);

        if (wasSuccessful) {

            data.response = _Codes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setPassword(Data data) {

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

    //------------------------------------------------------------------------------------

    /*public Data getEntrancePermission(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.client == null || data.client.packageName == null) {

            data.message = Messages.get("GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String appName = data.client.appName;
        String language = data.client.language;
        String market = data.client.market;
        ClientData clientData = data.client;

        boolean isOriginal = false;

        for (String packageName : BaseConfig.PACKAGE_NAMES) {

            if (packageName.equals(clientData.packageName) ||
                    (packageName + ".debug").equals(clientData.packageName)) {

                isOriginal = true;

                data.response = BaseCodes.RESPONSE_OK;

                data.coinChargeSettings = getCoinChargeSettings();
                data.gameSettings = new GameSettings();
                data.gameSettings.availableGames = new String[2];
                data.gamerTagChangePrice = CHANGE_GAMERTAG_PRICE_COIN;

                if ("FA".equals(language) && !market.equals(BaseConfig.MARKET_IRANAPPS)) {

                    data.gameSettings.availableGames[0] = "GhanGhan";
                    data.gameSettings.availableGames[1] = "Palette";
                }

                String adAvailability = DynamicConfig.getMap("Ads", appName + "-" + market);

                data.ad = new AdData();
                data.ad.availability = "enable".equals(adAvailability);

                if (data.client.uuid == null && data.client.apiVersion >= 45) {

                    data.client.uuid = Hash.md5(System.currentTimeMillis());
                    data.response = BaseCodes.RESPONSE_OK_CHANGE_UUID;
                }

                break;
            }
        }

        if (!isOriginal) {

            data.response = BaseCodes.RESPONSE_NOK_INVALID_APPLICATION_ID;

            data.message = Messages.get("GENERAL", Messages
                    .General.INVALID_APPLICATION_ID, data.client.language);
        }

        return data;
    }*/
}