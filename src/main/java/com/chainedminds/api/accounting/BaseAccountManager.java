package com.chainedminds.api.accounting;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.api.ActivityListener;
import com.chainedminds.api.IPLocationFinder;
import com.chainedminds.models.BaseData;
import com.chainedminds.models.CoinChargeSettings;
import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.QueryCallback;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BaseAccountManager<Data extends BaseData> {

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_USERNAME = "Username";
    protected static final String FIELD_NAME = "Name";
    protected static final String FIELD_IP_ADDRESS = "IPAddress";
    protected static final String FIELD_PHONE_NUMBER = "PhoneNumber";
    protected static final String FIELD_EMAIL = "Email";
    protected static final String FIELD_COINS = "Coins";
    protected static final String FIELD_TICKETS = "Tickets";
    protected static final String FIELD_PASSWORD = "Password";
    protected static final String FIELD_REGISTRATION_TIME = "RegistrationTime";
    protected static final String FIELD_BIO = "Bio";
    protected static final String FIELD_PREMIUM_PASS = "PremiumPass";
    protected static final String FIELD_APP_NAME = "AppName";
    protected static final String FIELD_APP_VERSION = "AppVersion";
    protected static final String FIELD_PLATFORM = "Platform";
    protected static final String FIELD_MARKET = "Market";
    protected static final String FIELD_LANGUAGE = "Language";
    protected static final String FIELD_COUNTRY = "Country";
    protected static final String FIELD_LAST_UPDATE = "LastUpdate";
    protected static final String FIELD_FIREBASE_ID = "FirebaseID";
    protected static final String FIELD_SDK = "SDK";
    protected static final String FIELD_BRAND = "Brand";
    protected static final String FIELD_MANUFACTURER = "Manufacturer";
    protected static final String FIELD_MODEL = "Model";
    protected static final String FIELD_PRODUCT = "Product";
    protected static final String FIELD_UUID = "UUID";
    protected static final String FIELD_TITLE = "Title";
    protected static final String FIELD_DATE_TIME = "DateTime";
    protected static final String FIELD_SCORE = "Score";
    protected static final String FIELD_LAST_COIN_CHARGE_AMOUNT = "LastCoinChargeAmount";
    protected static final String FIELD_LAST_TICKET_CHARGE_AMOUNT = "LastTicketChargeAmount";

    protected static final Set<Integer> PREMIUM_USERS = new HashSet<>();
    protected static final Set<Integer> CACHED_USER_INFO = new HashSet<>();

    private static final String TAG = BaseAccountManager.class.getSimpleName();

    private static final CacheManager<String, Integer> LOGIN_ATTEMPTS_CACHE = new CacheManager<>(1000 * 60 * 15);
    private static final CacheManager<Integer, Long> CACHE_REGISTRATION_TIME = new CacheManager<>();

    //protected final Set<Long> INDEX_USER_ID = new HashSet<>();

    protected final Map<Integer, String> MAPPING_USERNAME = new HashMap<>();
    protected final Set<String> INDEX_USERNAME = new HashSet<>();

    //protected final Map<Integer, String> MAPPING_PHONE_NUMBER = new HashMap<>();
    //protected final Set<Long> INDEX_PHONE_NUMBER = new HashSet<>();

    protected final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start2() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("FetchUsers")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 30, 0)
                .setTimingListener(task -> {

                    fetch();
                })
                .startAndSchedule());
    }

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("HourlyCache")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> CACHED_USER_INFO.clear())
                .startAndSchedule());

        TaskManager.addTask(TaskManager.Task.build()
                .setName("FetchUsers")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 30, 0)
                .setTimingListener(task -> {

                    fetch();
                    fetchPremiumUsers();
                })
                .startAndSchedule());

        if (!BaseConfig.DEBUG_MODE) {

            TaskManager.addTask(TaskManager.Task.build()
                    .setName("RecheckSubscriptions")
                    .setTime(0, 0, 0)
                    .setInterval(1, 0, 0, 0)
                    .setTimingListener(task -> recheckSubscriptions()));

            TaskManager.addTask(TaskManager.Task.build()
                    .setName("ChargeCoins")
                    .setTime(0, 0, 0)
                    .setInterval(0, 0, 10, 0)
                    .setTimingListener(task -> chargeCoins())
                    .startAndSchedule());

            TaskManager.addTask(TaskManager.Task.build()
                    .setName("ChargePremiumCoins")
                    .setTime(0, 0, 0)
                    .setInterval(0, 0, 3, 0)
                    .setTimingListener(task -> chargePremiumCoins())
                    .startAndSchedule());
        }
    }

    protected void addTasks() {

        //OVERRIDES IN UPPER CLASSES
    }

    protected void fetch() {

        String selectStatement = "SELECT " + FIELD_USER_ID + ", " +
                FIELD_USERNAME + " FROM " + BaseConfig.TABLE_ACCOUNTS;

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

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

    public void getUsernameMap(String tag, Utilities.GrantAccess<Map<Integer, String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USERNAME));
    }

    public void getUsernameList(String tag, Utilities.GrantAccess<Set<String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(INDEX_USERNAME));
    }

    protected void recheckSubscriptions() {

        Set<Integer> premiumUsers = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> premiumUsers.addAll(PREMIUM_USERS));

        BaseResources.getInstance().iabPaymentManager.checkSubscriptionsFor(premiumUsers);
    }

    protected void fetchPremiumUsers() {

        String selectStatement = "SELECT " + FIELD_USER_ID + " FROM " +
                BaseConfig.TABLE_ACCOUNTS + " WHERE " + FIELD_PREMIUM_PASS + " = TRUE";

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<Integer> premiumUserIDs = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);

                    premiumUserIDs.add(userID);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        PREMIUM_USERS.clear();

                        PREMIUM_USERS.addAll(premiumUserIDs);
                    });
                }
            }
        });
    }

    protected void chargeCoins() {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS + " SET " + FIELD_COINS + " = " +
                FIELD_COINS + " + 20, " + FIELD_LAST_COIN_CHARGE_AMOUNT + " = " + FIELD_LAST_COIN_CHARGE_AMOUNT +
                " + 20 WHERE " + FIELD_PREMIUM_PASS + " = FALSE AND " + FIELD_COINS + " < 100";

        BaseDatabaseHelperOld.update(TAG, updateStatement);
    }

    protected void chargePremiumCoins() {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS + " SET " + FIELD_COINS + " = " +
                FIELD_COINS + " + 20, " + FIELD_LAST_COIN_CHARGE_AMOUNT + " = " + FIELD_LAST_COIN_CHARGE_AMOUNT +
                " + 20 WHERE " + FIELD_PREMIUM_PASS + " = TRUE AND " + FIELD_COINS + " < 100";

        BaseDatabaseHelperOld.update(TAG, updateStatement);
    }

    protected CoinChargeSettings getCoinChargeSettings() {

        CoinChargeSettings coinChargeSettings = new CoinChargeSettings();

        coinChargeSettings.normalInterval = 600000;
        coinChargeSettings.normalSpeed = 1;
        coinChargeSettings.premiumInterval = 200000;
        coinChargeSettings.premiumSpeed = 3;
        coinChargeSettings.coinChargeCap = 100;
        coinChargeSettings.ticketChargeTime = 10;

        return coinChargeSettings;
    }

    public boolean isPremium(int userID) {

        AtomicBoolean isPremium = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> isPremium.set(PREMIUM_USERS.contains(userID)));

        return isPremium.get();
    }

    public int findUserID(String username) {

        AtomicInteger userID = new AtomicInteger(BaseConfig.NOT_FOUND);

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

    public <AccountData extends BaseAccountData> AccountData getAccount(int userID) {

        AtomicReference<AccountData> account = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_ACCOUNTS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                AccountData foundAccount = (AccountData) BaseClasses
                        .construct(BaseClasses.getInstance().accountClass);

                foundAccount.id = userID;
                foundAccount.gamerTag = resultSet.getString(FIELD_USERNAME);
                foundAccount.username = resultSet.getString(FIELD_USERNAME);
                foundAccount.name = resultSet.getString(FIELD_NAME);
                foundAccount.password = resultSet.getString(FIELD_PASSWORD);
                foundAccount.phoneNumber = resultSet.getLong(FIELD_PHONE_NUMBER);
                foundAccount.email = resultSet.getString(FIELD_EMAIL);
                foundAccount.coins = resultSet.getInt(FIELD_COINS);
                foundAccount.tickets = resultSet.getInt(FIELD_TICKETS);
                foundAccount.bio = resultSet.getString(FIELD_BIO);
                foundAccount.lastCoinChargeAmount = resultSet.getInt(FIELD_LAST_COIN_CHARGE_AMOUNT);
                foundAccount.lastTicketChargeAmount = resultSet.getInt(FIELD_LAST_TICKET_CHARGE_AMOUNT);

                account.set(foundAccount);
            }
        });

        return account.get();
    }

    public String getUsername(int userID) {

        AtomicReference<String> username = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> username.set(MAPPING_USERNAME.get(userID)));

        return username.get();
    }

    public boolean setPremiumPass(Connection connection, int userID, boolean isActive) {

        return setProperty(connection, userID, FIELD_PREMIUM_PASS, isActive);
    }

    public boolean setPremiumPass(int userID, boolean isActive) {

        return setProperty(userID, FIELD_PREMIUM_PASS, isActive);
    }

    protected boolean validateUserID(int userID) {

        AtomicBoolean userIDExists = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> userIDExists.set(MAPPING_USERNAME.containsKey(userID)));

        return userIDExists.get();
    }

    protected boolean checkIfUsernameRegistered(String username) {

        AtomicBoolean usernameRegistered = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            String lowerCasedUsername = username.toLowerCase();

            usernameRegistered.set(INDEX_USERNAME.contains(lowerCasedUsername));
        });

        return usernameRegistered.get();
    }

    protected Boolean validateCredential(int userID, String appName, String platform, String credential) {

        if (credential == null) {

            return false;
        }

        AtomicReference<String> storedCredential = new AtomicReference<>();

        boolean wasSuccessful = BaseResources.getInstance().accountPropertyManager.getCredential(
                userID, appName, platform, storedCredential);

        if (wasSuccessful) {

            return credential.equals(storedCredential.get());
        }

        return null;
    }

    protected Boolean validatePassword(int userID, String password) {

        String storedPassword = getPassword(userID);

        if (storedPassword != null) {

            return storedPassword.equals(password);
        }

        return null;
    }

    //------------------------------------------------------------------------------------

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

        Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

        String oldPassword = getPassword(connection, userID);

        boolean wasSuccessful = setProperty(connection, userID, FIELD_PASSWORD, newPassword);

        wasSuccessful &= BaseLogs.log(connection, userID, "Password", oldPassword + " -> " + newPassword);

        if (wasSuccessful) {

            BaseConnectionManagerOld.commit(connection);

        } else {

            BaseConnectionManagerOld.rollback(connection);
        }

        BaseConnectionManagerOld.close(connection);

        return wasSuccessful;
    }

    public int getCoins(int userID) {

        return getProperty(userID, FIELD_COINS, Integer.class);
    }

    public int getCoins(Connection connection, int userID) {

        return getProperty(connection, userID, FIELD_COINS, Integer.class);
    }

    protected boolean setCoins(Connection connection, int userID, int coins) {

        return setProperty(connection, userID, FIELD_COINS, coins);
    }

    public boolean changeCoins(Connection connection, int userID, int amount) {

        synchronized (userID + "") {

            int storedCoins = getCoins(connection, userID);

            if (storedCoins != -1) {

                int newCoins = storedCoins + amount;

                boolean wasSuccessful;

                if (newCoins >= 0) {

                    wasSuccessful = setCoins(connection, userID, newCoins);

                } else {

                    wasSuccessful = setCoins(connection, userID, 0);
                }

                return wasSuccessful;

            } else {

                return false;
            }
        }
    }

    public boolean hasEnoughCoins(int userID, int coins) {

        int storedCoins = getCoins(userID);

        return storedCoins != -1 && storedCoins >= coins;
    }

    public Long getRegistrationTime(int userID) {

        return CACHE_REGISTRATION_TIME.get(userID, () -> {

            Timestamp registrationTime = getProperty(userID, FIELD_REGISTRATION_TIME, Timestamp.class);

            if (registrationTime != null) {

                return registrationTime.getTime();
            }

            return null;
        });
    }

    //------------------------------------------------------------------------------------

    protected final <T> T getProperty(int userID, String field, Class<T> T) {

        return getProperty(null, userID, field, T);
    }

    protected final <T> T getProperty(Connection connection, int userID, String field, Class<T> T) {

        AtomicReference<T> value = new AtomicReference<>();

        String statement = "SELECT " + field + " FROM " + BaseConfig.TABLE_ACCOUNTS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        QueryCallback queryCallback = resultSet -> {

            if (resultSet.next()) {

                value.set(resultSet.getObject(field, T));
            }
        };

        if (connection != null) {

            BaseDatabaseHelperOld.query(connection, TAG, statement, parameters, queryCallback);

        } else {

            BaseDatabaseHelperOld.query(TAG, statement, parameters, queryCallback);
        }

        if (value.get() == null) {

            if (T == Integer.class) {

                value.set((T) Integer.valueOf(BaseConfig.NOT_FOUND));
            }
        }

        return value.get();
    }

    protected final boolean setProperty(int userID, String fieldName, Object value) {

        return setProperty(null, userID, fieldName, value);
    }

    protected final boolean setProperty(Connection connection, int userID, String fieldName, Object value) {

        String statement = "UPDATE " + BaseConfig.TABLE_ACCOUNTS + " SET " +
                fieldName + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, value);
        parameters.put(2, userID);

        if (connection != null) {

            return BaseDatabaseHelperOld.update(connection, TAG, statement, parameters);

        } else {

            return BaseDatabaseHelperOld.update(TAG, statement, parameters);
        }
    }

    //------------------------------------------------------------------------------------

    public Data authenticateUsername(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

//        String uuid = data.client.uuid;
//
//        if (BaseBlackListManager.isBlocked(uuid, BaseBlackListManager.TYPE_UUID,
//                BaseBlackListManager.REASON_ADMIN_KICK) == BaseBlackListManager.STATE_KICKED) {
//
//            data.message = Messages.get("SYSTEM_GENERAL", Messages.General.YOU_ARE_BLOCKED_FOR_SOME_TIMES, data.client.language);
//
//            return data;
//        }

        if (data.account == null ||
                (data.account.username == null && data.account.gamerTag == null) ||
                data.account.password == null) {

            data.message = Messages.get("SYSTEM_GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }


        String username = data.account.username != null ? data.account.username : data.account.gamerTag;
        String password = data.account.password;
        String appName = data.client.appName;
        String platform = data.client.platform;
        String language = data.client.language;
        String address = data.client.address;
        int appVersion = data.client.appVersion;

        if (BaseConfig.APP_NAME_CAFE_CHAT.equals(appName)) {

            data.message = "برای ورود به حساب از 'کافه گیم' بجای 'کافه چت' استفاده کن. 'کافه گیم' رو میتونی از طریق گوگل پلی، کافه بازار و یا مایکت نصب کنی.";

            return data;
        }

        username = Utilities.replaceLocalizedNumbers(username);
        password = Utilities.replaceLocalizedNumbers(password);

        int userID = findUserID(username);

        if (isBruteForcing(address)) {

            //BaseNotificationManager.reportBruteForce(address, username, password);

            data.response = BaseCodes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.TOO_MANY_ATTEMPTS, language);

            return data;
        }

        if (userID != BaseConfig.NOT_FOUND) {

            Boolean isBlocked = BaseResources.getInstance().accountPropertyManager.getBlocked(userID, appName);

            if (isBlocked != null && isBlocked) {

                data.message = "این حساب مسدود شده است";

                return data;
            }

            Boolean passwordValidated = validatePassword(userID, password);

            if (BaseAccountPermissionsManager.hasPermission(userID, "*", "MANAGEMENT") ||
                    BaseAccountPermissionsManager.hasPermission(userID, "*", "ADMIN") ||
                    BaseAccountPermissionsManager.hasPermission(userID, "*", "MODERATOR")) {

                //BaseNotificationManager.reportLogin(username, password, passwordValidated, address);

                if (BaseConfig.APP_NAME_CAFE_CHAT.equals(appName)) {

                    data.message = "این حساب فقط از طریق 'کافه گیم' مجاز به ورود می باشد";

                    return data;
                }
            }

            if (passwordValidated != null) {

                if (passwordValidated) {

                    String credential = BackendHelper.generateCredential();

                    Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

                    boolean wasSuccessful = BaseResources.getInstance().accountPropertyManager.setCredential(connection, userID, appName, platform, credential);

                    if (wasSuccessful) {

                        resetChargedItems(connection, userID);

                        data.account.id = userID;
                        data.account.credential = credential;

                        data.response = BaseCodes.RESPONSE_OK;
                    }

                    if (wasSuccessful) {

                        BaseConnectionManagerOld.commit(connection);

                    } else {

                        BaseConnectionManagerOld.rollback(connection);
                    }

                    BaseConnectionManagerOld.close(connection);

                } else {

                    data.response = BaseCodes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

                    data.message = Messages.get("SYSTEM_GENERAL",
                            Messages.General.INVALID_USERNAME_OR_PASSWORD, language);
                }
            }

        } else {

            data.response = BaseCodes.RESPONSE_INVALID_USERNAME_OR_PASSWORD;

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.INVALID_USERNAME_OR_PASSWORD, language);
        }

        return data;
    }

    private void resetChargedItems(Connection connection, int userID) {

        setProperty(connection, userID, FIELD_LAST_COIN_CHARGE_AMOUNT, 0);
        setProperty(connection, userID, FIELD_LAST_TICKET_CHARGE_AMOUNT, 0);
    }

    public boolean isBruteForcing(String address) {

        int attemptsTimes = LOGIN_ATTEMPTS_CACHE.get(address, () -> 1);

        if (attemptsTimes > 20) {

            return true;

        } else {

            LOGIN_ATTEMPTS_CACHE.put(address, attemptsTimes + 1);

            return false;
        }
    }

    public Data authenticateCredential(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.account.credential == null) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String credential = data.account.credential;
        String appName = data.client.appName;
        String platform = data.client.platform;
        int appVersion = data.client.appVersion;
        String language = data.client.language;
        int subRequest = data.subRequest;

        //---------CHECK IF USER ID IS VALID-------

        if (!BaseConfig.DEBUG_MODE) {

            if (!validateUserID(userID)) {

                data.response = BaseCodes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }

            //---------CHECK IF CREDENTIAL IS VALID-------

            Boolean credentialIsValid = validateCredential(userID, appName, platform, credential);

            if (credentialIsValid == null) {

                data.response = BaseCodes.RESPONSE_NOK;
                return data;
            }

            if (!credentialIsValid) {

                data.response = BaseCodes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }
        }

        //------------------------------------------------
        //------------UPDATE USER'S ACCOUNT---------------

        if (BaseConfig.APP_NAME_CAFE_GAME.equals(appName) && appVersion < 37 &&
                !CACHED_USER_INFO.contains(userID)) {

            NettyServer.execute(() -> updateAccount(data));
        }

        ActivityListener.setBecameOnline(userID);

        //------------REQUESTS------------------------

        onSubRequestReceived(data);

        //--------------------------------------------

        return data;
    }

    public Data onSubRequestReceived(Data data) {

        int userID = data.account.id;
        int subRequest = data.subRequest;

        if (subRequest == 0) {

            data.response = BaseCodes.RESPONSE_OK;
            data.message = null;
        }

        return data;
    }

    public Data registerAccount(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null ||
                data.account.username == null ||
                data.account.username.length() < 3) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String language = data.client.language;
        String lowerCasedUsername = data.account.username.toLowerCase();

        /*if (lowerCasedUsername.contains("dev") ||
                lowerCasedUsername.contains("developer") ||
                lowerCasedUsername.contains("admin") ||
                lowerCasedUsername.contains("cafegame") ||
                lowerCasedUsername.contains("fandogh")) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.ILLEGAL_GAMER_TAG, language);

            return data;
        }*/

        String username = data.account.username;
        String password = data.account.password;
        String name = data.account.name;
        String appName = data.client.appName;
        String platform = data.client.platform;

        username = Utilities.replaceLocalizedNumbers(username);

        if (password != null) {

            password = Utilities.replaceLocalizedNumbers(password);
        }

        if (!checkIfUsernameRegistered(username)) {

            /*if (BaseBlackListManager.isBlocked(ipAddress, BaseBlackListManager.TYPE_IP_ADDRESS,
                    BaseBlackListManager.REASON_ADMIN_KICK) == BaseBlackListManager.STATE_KICKED ||
                    BaseBlackListManager.isBlocked(uuid, BaseBlackListManager.TYPE_UUID,
                            BaseBlackListManager.REASON_ADMIN_KICK) == BaseBlackListManager.STATE_KICKED) {

                data.response = BaseCodes.RESPONSE_NOK;

                data.message = Messages.get("SYSTEM_GENERAL",
                        Messages.General.YOU_ARE_BLOCKED_FOR_SOME_TIMES, language);

                return data;
            }*/

            int userID = registerAccount(username, password, name, appName, platform);

            if (userID != -1) {

                String credential = BackendHelper.generateCredential();

                if (BaseResources.getInstance().accountPropertyManager.setCredential(userID, appName, platform, credential)) {

                    data.account.id = userID;
                    data.account.credential = credential;

                    data.response = BaseCodes.RESPONSE_OK;
                }
            }

        } else {

            data.response = BaseCodes.RESPONSE_IS_REGISTERED_BEFORE;

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.USERNAME_HAS_REGISTERED_BEFORE, language);
        }

        return data;
    }

    protected int registerAccount(String username, String password, String name, String appName, String platform) {

        AtomicInteger userID = new AtomicInteger(BaseConfig.NOT_FOUND);

        String insertStatement = "INSERT " + BaseConfig.TABLE_ACCOUNTS +
                " (" + FIELD_USERNAME + ", " + FIELD_PASSWORD + ", " + FIELD_NAME + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, username);
        parameters.put(2, password);
        parameters.put(3, name);

        BaseDatabaseHelperOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                userID.set(generatedID);

                if (appName != null && platform != null) {

                    String insertStatement2 = "INSERT " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +
                            " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PLATFORM +
                            ") VALUES (?, ?, ?)";

                    parameters.put(1, generatedID);
                    parameters.put(2, appName);
                    parameters.put(3, platform);

                    wasSuccessful = BaseDatabaseHelperOld.insert(TAG, insertStatement2, parameters);
                }

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        MAPPING_USERNAME.put(userID.get(), username);
                        INDEX_USERNAME.add(username.toLowerCase());
                    });
                }
            }
        });

        return userID.get();
    }

    protected Data updateAccount(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        int appVersion = data.client.appVersion;
        String appName = data.client.appName;
        String platform = data.client.platform;
        String market = data.client.market;
        String language = data.client.language;
        String firebaseID = data.client.firebaseID;
        String ipAddress = data.client.address;
        int sdk = data.client.sdk;
        String brand = data.client.brand;
        String manufacturer = data.client.manufacturer;
        String model = data.client.model;
        String product = data.client.product;
        String uuid = data.client.uuid;
        String country = IPLocationFinder.getCountry(ipAddress);

        if (appVersion == 0) {

            appVersion = data.client.appVersion;
            appName = data.client.appName;
            platform = data.client.platform;
            market = data.client.market;
            language = data.client.language;
            firebaseID = data.client.firebaseID;
        }

        if (uuid == null) {

            uuid = Hash.md5(System.currentTimeMillis());
        }

        String statement = "INSERT " + BaseConfig.TABLE_ACCOUNTS_PROPERTIES +

                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_APP_VERSION +
                ", " + FIELD_PLATFORM + ", " + FIELD_MARKET + ", " + FIELD_LANGUAGE +
                ", " + FIELD_FIREBASE_ID + ", " + FIELD_IP_ADDRESS + ", " + FIELD_UUID +
                ", " + FIELD_COUNTRY + ", " + FIELD_LAST_UPDATE + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +

                "ON DUPLICATE KEY UPDATE " +

                FIELD_APP_VERSION + " = VALUES (" + FIELD_APP_VERSION + "), " +
                FIELD_PLATFORM + " = VALUES (" + FIELD_PLATFORM + "), " +
                FIELD_MARKET + " = VALUES (" + FIELD_MARKET + "), " +
                FIELD_LANGUAGE + " = VALUES (" + FIELD_LANGUAGE + "), " +
                FIELD_FIREBASE_ID + " = VALUES (" + FIELD_FIREBASE_ID + "), " +
                FIELD_IP_ADDRESS + " = VALUES (" + FIELD_IP_ADDRESS + "), " +
                FIELD_UUID + " = VALUES (" + FIELD_UUID + "), " +
                FIELD_COUNTRY + " = VALUES (" + FIELD_COUNTRY + "), " +
                FIELD_LAST_UPDATE + " = VALUES (" + FIELD_LAST_UPDATE + ")";

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

        statement = "INSERT INTO " + BaseConfig.TABLE_DEVICES_PROPERTIES +

                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PLATFORM +
                ", " + FIELD_MARKET + ", " + FIELD_LANGUAGE + ", " + FIELD_APP_VERSION +
                ", " + FIELD_SDK + ", " + FIELD_BRAND + ", " + FIELD_MANUFACTURER +
                ", " + FIELD_MODEL + ", " + FIELD_PRODUCT + ", " + FIELD_UUID +
                ", " + FIELD_LAST_UPDATE + ") " +

                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE " +

                FIELD_MARKET + " = VALUES (" + FIELD_MARKET + "), " +
                FIELD_LANGUAGE + " = VALUES (" + FIELD_LANGUAGE + "), " +
                FIELD_APP_VERSION + " = VALUES (" + FIELD_APP_VERSION + "), " +
                FIELD_SDK + " = VALUES (" + FIELD_SDK + "), " +
                FIELD_BRAND + " = VALUES (" + FIELD_BRAND + "), " +
                FIELD_MANUFACTURER + " = VALUES (" + FIELD_MANUFACTURER + "), " +
                FIELD_MODEL + " = VALUES (" + FIELD_MODEL + "), " +
                FIELD_PRODUCT + " = VALUES (" + FIELD_PRODUCT + "), " +
                FIELD_UUID + " = VALUES (" + FIELD_UUID + "), " +
                FIELD_LAST_UPDATE + " = VALUES (" + FIELD_LAST_UPDATE + ")";

        parameters.clear();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, platform);
        parameters.put(4, market);
        parameters.put(5, language);
        parameters.put(6, appVersion);
        parameters.put(7, sdk);
        parameters.put(8, brand);
        parameters.put(9, manufacturer);
        parameters.put(10, model);
        parameters.put(11, product);
        parameters.put(12, uuid);

        wasSuccessful &= BaseDatabaseHelperOld.insert(TAG, statement, parameters);

        if (wasSuccessful) {

            CACHED_USER_INFO.add(userID);

            data.response = BaseCodes.RESPONSE_OK;

            if (data.client.uuid == null) {

                data.client.uuid = uuid;

                data.response = BaseCodes.RESPONSE_OK_CHANGE_UUID;
            }
        }

        return data;
    }

    //------------------------------------------------------------------------------------

    protected Data setPhoneNumber(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || (data.account.phoneNumber + "").length() == 0) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        long phoneNumber = data.account.phoneNumber;

        boolean wasSuccessful = setProperty(userID, FIELD_PHONE_NUMBER, phoneNumber);

        if (wasSuccessful) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setEmail(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || (data.account.email + "").length() == 0) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String email = data.account.email;

        boolean wasSuccessful = setProperty(userID, FIELD_EMAIL, email);

        if (wasSuccessful) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setName(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.account.name == null) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String name = data.account.name;

        boolean wasSuccessful = setProperty(userID, FIELD_NAME, name);

        if (wasSuccessful) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setBio(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.account.bio == null) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String bio = data.account.bio;

        boolean wasSuccessful = setProperty(userID, FIELD_BIO, bio);

        if (wasSuccessful) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }

    protected Data setPassword(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || (data.account.password + "").length() == 0) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        if (data.account.password.length() < 6) {

            data.message = Messages.get("SYSTEM_GENERAL",
                    Messages.General.PASSWORD_IS_TOO_SHORT, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String password = data.account.password;

        boolean wasSuccessful = setPassword(userID, password);

        if (wasSuccessful) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }

    //------------------------------------------------------------------------------------

    /*public Data getEntrancePermission(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.client == null || data.client.packageName == null) {

            data.message = Messages.get("SYSTEM_GENERAL",
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

            data.message = Messages.get("SYSTEM_GENERAL", Messages
                    .General.INVALID_APPLICATION_ID, data.client.language);
        }

        return data;
    }*/

    public Data searchUsers(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null || data.account.username == null) {

            return data;
        }

        String query = data.account.username.toLowerCase();

        data.accounts = new ArrayList<>();

        getUsernameMap(TAG, usernames -> usernames.forEach((userID, username) -> {

            if (username.toLowerCase().contains(query)) {

                BaseAccountData account = new BaseAccountData();
                account.id = userID;
                account.username = username;

                data.accounts.add(account);
            }
        }));

        Comparator<BaseAccountData> comparator = Comparator.comparingInt(account -> account.username.length());

        data.accounts.sort(comparator);

        if (data.accounts.size() > 100) {

            data.accounts = data.accounts.subList(0, 99);
        }

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }

    public Data getUsers(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.account == null) {

            return data;
        }

        String appName = data.client.appName;

        Set<Integer> userIDs = BaseResources.getInstance().accountPropertyManager.getUserIDs(appName);

        data.accounts = new ArrayList<>();

        getUsernameMap(TAG, usernames -> userIDs.forEach(userID -> {

            BaseAccountData account = new BaseAccountData();
            account.id = userID;
            account.username = usernames.get(userID);

            data.accounts.add(account);
        }));

        Comparator<BaseAccountData> comparator = Comparator.comparing(account -> account.username);

        data.accounts.sort(comparator);

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }

    public BaseAccountData getUser(int userID, String appName, int targetUserID) {

        AtomicReference<BaseAccountData> accountHolder = new AtomicReference<>();

        if (BaseAccountPermissionsManager.hasPermission(userID, appName, "USER_MANAGEMENT")) {

            BaseAccountData account = getAccount(targetUserID);

            account.password = null;
            account.permissions = BaseAccountPermissionsManager.getPermissions(targetUserID, appName);

            accountHolder.set(account);
        }

        return accountHolder.get();
    }
}