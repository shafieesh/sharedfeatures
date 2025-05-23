package com.chainedminds.api.accounting;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.api.ActivityListener;
import com.chainedminds.models._Data;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.network.netty.NettyServer;
import com.chainedminds.utilities.Messages;
import com.chainedminds.utilities.Task;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.QueryCallback;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;

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
    protected static final String FIELD_IS_ACTIVE = "IsActive";
    protected static final String FIELD_NAME = "Name";
    protected static final String FIELD_PHONE_NUMBER = "PhoneNumber";
    protected static final String FIELD_EMAIL = "Email";
    protected static final String FIELD_REGISTRATION_TIME = "RegistrationTime";
    protected static final String FIELD_LAST_UPDATE = "LastUpdate";

    protected static final Map<Integer, String> MAPPING_USER_ID = new HashMap<>();

    protected final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void start() {

        Task.add(Task.Data.build()
                .setName("FetchUsers")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 5, 0)
                .setTimingListener(task -> {
                    fetch();
                    _Authentication.fetch();
                })
                .runNow()
                .schedule());

        addTasks();
    }

    protected void addTasks() {

        //OVERRIDES IN UPPER CLASSES
    }

    protected void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<Integer, String> mappingUserIDs = new HashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);
                    String username = resultSet.getString(FIELD_NAME);

                    mappingUserIDs.put(userID, username);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        MAPPING_USER_ID.clear();
                        MAPPING_USER_ID.putAll(mappingUserIDs);
                    });
                }
            }
        });
    }

    //------------------------------------------------------------------------------------

    protected boolean validateUserID(int userID) {

        AtomicBoolean userIDExists = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> userIDExists.set(MAPPING_USER_ID.containsKey(userID)));

        return userIDExists.get();
    }

    //------------------------

    protected Boolean getIsActive(int userID) {

        return getProperty(userID, FIELD_IS_ACTIVE, Boolean.class);
    }

    public String getName(int userID) {

        AtomicReference<String> name = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> name.set(MAPPING_USER_ID.get(userID)));
        
        return name.get();
    }

    //------------------------

    public List<_AccountData> searchNames(String name) {

        List<_AccountData> foundAccounts = new ArrayList<>();

        if (name == null) {

            return foundAccounts;
        }

        String query = name.toLowerCase();

        getUserIDMap(TAG, entries -> entries.forEach((loopingUserID, loopingName) -> {

            if (loopingName.toLowerCase().contains(query)) {

                _AccountData account = new _AccountData();
                account.id = loopingUserID;
                account.name = loopingName;

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

    protected int registerAccount(String name) {

        AtomicInteger userID = new AtomicInteger(_Config.NOT_FOUND);

        String insertStatement = "INSERT " + _Config.TABLE_ACCOUNTS + " (" + FIELD_NAME + ") VALUES (?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, name);

        _DatabaseOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                userID.set(generatedID);

                Utilities.lock(TAG, LOCK.writeLock(), () -> {

                    MAPPING_USER_ID.put(generatedID, name);
                });
            }
        });

        return userID.get();
    }

    public void getUserIDMap(String tag, Utilities.GrantAccess<Map<Integer, String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(MAPPING_USER_ID));
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
                fieldName + " = ?, " + FIELD_LAST_UPDATE + " = ? WHERE " + FIELD_USER_ID + " = ?";

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

    public Data authenticateCredential(Data data) {

        data.response = _Codes.RESPONSE_NOK;

        if (data.account == null || data.account.credential == null || data.subRequest == null) {

            data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String credential = data.account.credential;

        //---------CHECK IF USER ID IS VALID-------

        if (!_Config.DEBUG_MODE) {

            if (!validateUserID(userID)) {

                data.message = Messages.get("GENERAL", Messages.General.CREDENTIAL_EXPIRED, data.client.language);
                data.response = _Codes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }

            //---------CHECK IF CREDENTIAL IS VALID-------

            Boolean credentialIsValid = _Resources.get().accountSession.validateCredential(userID, credential);

            if (credentialIsValid == null) {

                data.response = _Codes.RESPONSE_NOK;
                return data;
            }

            if (!credentialIsValid) {

                data.message = Messages.get("GENERAL", Messages.General.CREDENTIAL_EXPIRED, data.client.language);
                data.response = _Codes.RESPONSE_CREDENTIAL_EXPIRED;
                return data;
            }
        }

        //------------------------------------------------
        //------------UPDATE USER'S ACCOUNT---------------

        if (!_Resources.get().accountSession.CACHED_USERS_INFO.contains(credential)) {

            String version = data.client.version;
            String language = data.client.language;
            String firebaseID = data.client.firebaseID;
            String ipAddress = data.client.address;
            String country = data.client.country;

            NettyServer.execute(() -> _Resources.get().accountSession.updateAccount(
                    credential, version, language, firebaseID, ipAddress, country));
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