package com.chainedminds.api.accounting;

import com.chainedminds._Config;
import com.chainedminds.models._PermissionData;
import com.chainedminds.utilities.Task;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _AccountPermissions {

    private static final String TAG = _AccountPermissions.class.getSimpleName();

    protected static final String FIELD_USER_ID = "UserID";
    protected static final String FIELD_APP_NAME = "AppName";
    protected static final String FIELD_PERMISSION = "Permission";
    protected static final String FIELD_START_TIME = "StartTime";
    protected static final String FIELD_FINISH_TIME = "FinishTime";
    protected static final String FIELD_POSITION = "Position";
    protected static final String FIELD_CATEGORY = "Category";
    protected static final String FIELD_TITLE = "Title";
    protected static final String FIELD_DESCRIPTION = "Description";

    protected static final List<_PermissionData> PERMISSIONS = new ArrayList<>();
    protected static final List<_PermissionData> USER_PERMISSIONS = new ArrayList<>();

    protected static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void start() {

        Task.add(Task.Data.build()
                .setName("AccountPermissionsManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .onEachCycle(() -> {
                    fetchPermissions();
                    fetchUserPermissions();
                })
                .runNow()
                .schedule());
    }

    protected static void fetchPermissions() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PERMISSIONS + " ORDER BY " + FIELD_POSITION;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<_PermissionData> permissions = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    _PermissionData permission = new _PermissionData();
                    permission.appName = resultSet.getString(FIELD_APP_NAME);
                    permission.permission = resultSet.getString(FIELD_PERMISSION);
                    permission.category = resultSet.getString(FIELD_CATEGORY);
                    permission.title = resultSet.getString(FIELD_TITLE);
                    permission.description = resultSet.getString(FIELD_DESCRIPTION);

                    permissions.add(permission);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        PERMISSIONS.clear();
                        PERMISSIONS.addAll(permissions);
                    });
                }
            }
        });
    }

    protected static void fetchUserPermissions() {

        String selectStatement = "SELECT * FROM " +
                _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " WHERE " + FIELD_START_TIME + " <= NOW() AND NOW() <= " + FIELD_FINISH_TIME;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<_PermissionData> permissions = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);

                    _PermissionData permission = new _PermissionData();

                    permission.userID = userID;
                    permission.appName = resultSet.getString(FIELD_APP_NAME);
                    permission.permission = resultSet.getString(FIELD_PERMISSION);

                    permissions.add(permission);
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                if (wasSuccessful) {

                    Utilities.lock(TAG, LOCK.writeLock(), () -> {

                        USER_PERMISSIONS.clear();
                        USER_PERMISSIONS.addAll(permissions);
                    });
                }
            }
        });
    }

    public static List<_PermissionData> getPermissions() {

        List<_PermissionData> permissions = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            permissions.addAll(PERMISSIONS);
        });

        return permissions;
    }

    public static Set<String> getPermissions(int userID) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID) {

                    permissions.add(permission.permission);
                }
            }
        });

        return permissions;
    }

    public static Set<String> getPermissions(int userID, String appName) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID && ("*".equals(appName) || appName.equals(permission.appName))) {

                    permissions.add(permission.permission);
                }
            }
        });

        return permissions;
    }

    public static Set<String> getRealPermissions(int userID, String appName) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID && ("*".equals(appName) || appName.equals(permission.appName))) {

                    permissions.add(permission.permission);
                }
            }
        });

        return permissions;
    }

    @Deprecated
    public static boolean hasPermissions(int userID) {

        AtomicBoolean hasPermissions = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID) {

                    hasPermissions.set(true);
                    break;
                }
            }
        });

        return hasPermissions.get();
    }

    public static boolean hasPermission(int userID, String permissionName) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID && permissionName.equals(permission.permission)) {

                    hasPermission.set(true);

                    break;
                }
            }
        });

        return hasPermission.get();
    }

    public static boolean hasPermission(int userID, String appName, String permissionName) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID &&
                        ("*".equals(appName) || appName.equals(permission.appName)) &&
                        permissionName.equals(permission.permission)) {

                    hasPermission.set(true);

                    break;
                }
            }
        });

        return hasPermission.get();
    }

    public static boolean hasRealPermission(int userID, String appName, String permissionName) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID &&
                        ("*".equals(appName) || appName.equals(permission.appName)) &&
                        permissionName.equals(permission.permission)) {

                    hasPermission.set(true);

                    break;
                }
            }
        });

        return hasPermission.get();
    }

    public static boolean hasPermission(int userID, String appName, String permissionName, String permissionName2) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if (userID == permission.userID &&
                        ("*".equals(appName) || appName.equals(permission.appName)) &&
                        (permissionName.equals(permission.permission) ||
                                permissionName2.equals(permission.permission))) {

                    hasPermission.set(true);

                    break;
                }
            }
        });

        return hasPermission.get();
    }

    public static boolean hasPermission(int userID, int userID2, String appName, String permissionName) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (_PermissionData permission : USER_PERMISSIONS) {

                if ((userID == permission.userID || userID2 == permission.userID) &&
                        ("*".equals(appName) || appName.equals(permission.appName)) &&
                        permissionName.equals(permission.permission)) {

                    hasPermission.set(true);

                    break;
                }
            }
        });

        return hasPermission.get();
    }

    public static boolean setPermission(Connection connection, int userID, String appName, String permission, long finishTime) {

        String insertStatement = "REPLACE " + _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PERMISSION +
                ", " + FIELD_FINISH_TIME + ") VALUES (?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, permission);
        parameters.put(4, new Timestamp(finishTime));

        return _DatabaseOld.insert(connection, TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                fetchUserPermissions();
            }
        });
    }

    public static boolean setPermission(Connection connection, int userID,
                                        String appName, String permission,
                                        long startTime, long finishTime) {

        String insertStatement = "REPLACE " + _Config.TABLE_ACCOUNTS_PERMISSIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PERMISSION +
                ", " + FIELD_START_TIME + ", " + FIELD_FINISH_TIME + ") VALUES (?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, permission);
        parameters.put(4, new Timestamp(startTime));
        parameters.put(5, new Timestamp(finishTime));

        return _DatabaseOld.insert(connection, TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                fetchUserPermissions();
            }
        });
    }

    public static boolean removePermission(Connection connection, int userID, String appName, String permission) {

        String deleteStatement = "DELETE FROM " + _Config.TABLE_ACCOUNTS_PERMISSIONS + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ? AND " + FIELD_PERMISSION + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, permission);

        return _DatabaseOld.update(connection, TAG, deleteStatement, parameters, (wasSuccessful, error) -> {

            if (wasSuccessful) {

                fetchUserPermissions();
            }
        });
    }
}
