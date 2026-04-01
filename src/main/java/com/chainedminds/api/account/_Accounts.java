package com.chainedminds.api.account;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds.models.account._AccountData;
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

public class _Accounts {

    private static final String TAG = _Accounts.class.getSimpleName();

    public static final String FIELD_ID = "UserID";
    public static final String FIELD_NAME = "Name";
    public static final String FIELD_IS_ACTIVE = "IsActive";
    public static final String FIELD_LAST_UPDATE = "LastUpdate";

    public static final Map<Integer, String> USERS = new LinkedHashMap<>();

    public final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<Integer, String> mappingUserIDs = new LinkedHashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_ID);
                    String username = resultSet.getString(FIELD_NAME);

                    mappingUserIDs.put(userID, username);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        USERS.clear();
                        USERS.putAll(mappingUserIDs);
                    });
                }
            }
        });
    }

    //------------------------------------------------------------------------------------

    public boolean has(int id) {

        AtomicBoolean has = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> has.set(USERS.containsKey(id)));

        return has.get();
    }

    //------------------------

    public Boolean isActive(int id) {

        return getProperty(id, FIELD_IS_ACTIVE, Boolean.class);
    }

    public String getName(int id) {

        AtomicReference<String> name = new AtomicReference<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> name.set(USERS.get(id)));

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

    public int registerAccount(String name) {

        AtomicInteger userID = new AtomicInteger(_Codes.NOT_FOUND);

        String insertStatement = "INSERT " + _Config.TABLE_ACCOUNTS + " (" + FIELD_NAME + ") VALUES (?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, name);

        _DatabaseOld.insert(TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                userID.set(generatedID);

                Utilities.lock(TAG, LOCK.writeLock(), () -> {

                    USERS.put(generatedID, name);
                });
            }
        });

        return userID.get();
    }

    public void getUserIDMap(String tag, Utilities.GrantAccess<Map<Integer, String>> job) {

        Utilities.lock(tag, LOCK.readLock(), () -> job.giveAccess(USERS));
    }

    public Map<Integer, String> getUsers() {

        Map<Integer, String> users = new LinkedHashMap<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> users.putAll(USERS));

        return users;
    }

    //------------------------------------------------------------------------------------

    public final <T> T getProperty(int id, String field, Class<T> T) {

        return getProperty(null, id, field, T);
    }

    public final <T> T getProperty(Connection connection, int id, String field, Class<T> T) {

        AtomicReference<T> value = new AtomicReference<>();

        String statement = "SELECT " + field + " FROM " + _Config.TABLE_ACCOUNTS +
                " WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, id);

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

                value.set((T) Integer.valueOf(_Codes.NOT_FOUND));
            }
        }

        return value.get();
    }

    public final boolean setProperty(int id, String fieldName, Object value) {

        return setProperty(null, id, fieldName, value);
    }

    public final boolean setProperty(Connection connection, int id, String fieldName, Object value) {

        String statement = "UPDATE " + _Config.TABLE_ACCOUNTS + " SET " +
                fieldName + " = ?, " + FIELD_LAST_UPDATE + " = NOW() WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, value);
        parameters.put(2, id);

        if (connection != null) {

            return _DatabaseOld.update(connection, TAG, statement, parameters);

        } else {

            return _DatabaseOld.update(TAG, statement, parameters);
        }
    }

    //------------------------------------------------------------------------------------
}