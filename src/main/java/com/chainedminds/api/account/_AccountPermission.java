package com.chainedminds.api.account;

import com.chainedminds._Config;
import com.chainedminds.models.account._AccountPermissionData;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _AccountPermission {

    private static final String TAG = _AccountPermission.class.getSimpleName();

    public static final String FIELD_USER_ID = "UserID";
    public static final String FIELD_PERMISSION = "Permission";
    public static final String FIELD_START_TIME = "StartTime";
    public static final String FIELD_FINISH_TIME = "FinishTime";

    public static final Map<Integer, Map<String, _AccountPermissionData>> PERMISSIONS = new LinkedHashMap<>();

    public static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " WHERE " + FIELD_START_TIME + " <= NOW() AND NOW() <= " + FIELD_FINISH_TIME;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Map<Integer, Map<String, _AccountPermissionData>> permissions = new LinkedHashMap<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    _AccountPermissionData permission = new _AccountPermissionData();
                    permission.userID = resultSet.getInt(FIELD_USER_ID);
                    permission.permission = resultSet.getString(FIELD_PERMISSION);
                    permission.startTime = resultSet.getTimestamp(FIELD_START_TIME).getTime();
                    permission.finishTime = resultSet.getTimestamp(FIELD_FINISH_TIME).getTime();

                    permissions.putIfAbsent(permission.userID, new LinkedHashMap<>());
                    permissions.get(permission.userID).put(permission.permission, permission);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        PERMISSIONS.clear();
                        PERMISSIONS.putAll(permissions);
                    });
                }
            }
        });
    }

    public static Set<String> get(int userID) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            permissions.addAll(PERMISSIONS.getOrDefault(userID, new HashMap<>()).keySet());
        });

        return permissions;
    }

    public static boolean has(int userID, String permission) {

        Set<String> permissions = get(userID);

        return permissions.contains(permission);
    }

    public static boolean hasAny(int userID, String... permissions) {

        return hasAny(userID, Arrays.asList(permissions));
    }

    public static boolean hasAny(int userID, List<String> permissions) {

        Set<String> currentPermissions = get(userID);

        for (String loopingPermission : permissions) {

            if (currentPermissions.contains(loopingPermission)) {

                return true;
            }
        }

        return false;
    }

    public static boolean hasAll(int userID, String... permissions) {

        return hasAll(userID, Arrays.asList(permissions));
    }

    public static boolean hasAll(int userID, List<String> permissions) {

        if (permissions.isEmpty()) {

            return true;
        }

        Set<String> currentPermissions = get(userID);

        if (currentPermissions.isEmpty()) {

            return false;
        }

        for (String loopingPermission : permissions) {

            if (!currentPermissions.contains(loopingPermission)) {

                return false;
            }
        }

        return true;
    }

    public static boolean set(Connection connection, int userID, String permission, long finishTime) {

        return set(connection, userID, permission, null, finishTime);
    }

    public static boolean set(Connection connection, int userID, String permission, Long startTime, long finishTime) {

        _AccountPermissionData permissionData = new _AccountPermissionData();
        permissionData.userID = userID;
        permissionData.permission = permission;
        permissionData.startTime = startTime != null ? startTime : System.currentTimeMillis();
        permissionData.finishTime = finishTime;

        String insertStatement = "REPLACE " + _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_PERMISSION + ", " + FIELD_START_TIME + ", " +
                FIELD_FINISH_TIME + ") VALUES (?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, permissionData.userID);
        parameters.put(2, permissionData.permission);
        parameters.put(3, new Timestamp(permissionData.startTime));
        parameters.put(4, new Timestamp(permissionData.finishTime));

        return _DatabaseOld.insert(connection, TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                Utilities.lock(TAG, LOCK.writeLock(), () -> {

                    PERMISSIONS.putIfAbsent(userID, new LinkedHashMap<>());
                    PERMISSIONS.get(userID).put(permissionData.permission, permissionData);
                });
            }
        });
    }

    public static boolean remove(Connection connection, int userID, String permission) {

        String deleteStatement = "DELETE FROM " + _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " WHERE " + FIELD_USER_ID + " = ? AND " + FIELD_PERMISSION + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, permission);

        return _DatabaseOld.update(connection, TAG, deleteStatement, parameters, (wasSuccessful, error) -> {

            if (wasSuccessful) {

                Utilities.lock(TAG, LOCK.writeLock(), () -> PERMISSIONS.getOrDefault(userID, new HashMap<>()).remove(permission));
            }
        });
    }
}
