package com.chainedminds.api.accounting;

import com.chainedminds.BaseConfig;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AccountPermissionsManager {

    public static final String PREMIUM = "PREMIUM";

    public static final String ADMIN = "ADMIN";
    public static final String MODERATOR = "MODERATOR";
    private static final String TAG = AccountPermissionsManager.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_PERMISSION = "Permission";
    private static final String FIELD_START_TIME = "StartTime";
    private static final String FIELD_FINISH_TIME = "FinishTime";
    private static final String FIELD_TITLE = "Title";
    private static final String FIELD_DESCRIPTION = "Description";

    private static final List<Permission> PERMISSIONS = new ArrayList<>();
    private static final List<Permission> USER_PERMISSIONS = new ArrayList<>();
    private static final Set<Integer> USER_ID_IDX = new HashSet<>();

    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    public static void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("AccountPermissionsManager")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .startAndSchedule()
                .setTimingListener(task -> {
                    fetchPermissions();
                    fetchUserPermissions();
                }));
    }

    private static void fetchPermissions() {

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PERMISSIONS;

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<Permission> permissions = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    Permission permission = new Permission();
                    permission.appName = resultSet.getString(FIELD_APP_NAME);
                    permission.permission = resultSet.getString(FIELD_PERMISSION);
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

    private static void fetchUserPermissions() {

        String selectStatement = "SELECT * FROM " +
                BaseConfig.TABLE_ACCOUNTS_PERMISSIONS +
                " WHERE " + FIELD_START_TIME + " < NOW() AND " + FIELD_FINISH_TIME + " > NOW()";

        BaseDatabaseHelperOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<Permission> permissions = new ArrayList<>();
            private final Set<Integer> userIDs = new HashSet<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    int userID = resultSet.getInt(FIELD_USER_ID);

                    userIDs.add(userID);

                    Permission permission = new Permission();

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
                        USER_ID_IDX.clear();

                        USER_PERMISSIONS.addAll(permissions);
                        USER_ID_IDX.addAll(userIDs);
                    });
                }
            }
        });
    }

    public static List<Permission> getPermissions() {

        List<Permission> permissions = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            permissions.addAll(PERMISSIONS);
        });

        return permissions;
    }

    public static Set<String> getPermissions(int userID) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Permission permission : USER_PERMISSIONS) {

                if (userID == permission.userID) {

                    permissions.add(permission.permission);
                }
            }
        });

        return permissions;
    }

    public static String getCurrentPermission(int userID) {

        AtomicReference<String> userPermission = new AtomicReference<>("EMPTY");

        String selectStatement = "SELECT " + FIELD_PERMISSION + " FROM " + BaseConfig.TABLE_ACCOUNTS_PERMISSIONS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        BaseDatabaseHelperOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                userPermission.set(resultSet.getString(FIELD_PERMISSION));
            }
        });

        return userPermission.get();
    }

    public static Set<String> getPermissions(int userID, String appName) {

        Set<String> permissions = new HashSet<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Permission permission : USER_PERMISSIONS) {

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

            for (Permission permission : USER_PERMISSIONS) {

                if (userID == permission.userID && ("*".equals(appName) || appName.equals(permission.appName))) {

                    permissions.add(permission.permission);
                }
            }
        });

        return permissions;
    }

    public static boolean setPermission(Connection connection, int userID, String appName, String permission, long finishTime) {

        String insertStatement = "REPLACE " + BaseConfig.TABLE_ACCOUNTS_PERMISSIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_PERMISSION +
                ", " + FIELD_FINISH_TIME + ") VALUES (?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, permission);
        parameters.put(4, new Timestamp(finishTime));

        return BaseDatabaseHelperOld.insert(connection, TAG, insertStatement, parameters, (wasSuccessful, generatedID, error) -> {

            Permission permissionData = new Permission();

            permissionData.userID = userID;
            permissionData.appName = appName;
            permissionData.permission = permission;

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                USER_ID_IDX.add(userID);
                USER_PERMISSIONS.add(permissionData);
            });
        });
    }

    @Deprecated
    public static boolean hasPermissions(int userID) {

        AtomicBoolean hasPermissions = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> hasPermissions.set(USER_ID_IDX.contains(userID)));

        return hasPermissions.get();
    }

    public static boolean hasPermission(int userID, String permissionName) {

        AtomicBoolean hasPermission = new AtomicBoolean(false);

        Utilities.lock(TAG, LOCK.readLock(), () -> {

            for (Permission permission : USER_PERMISSIONS) {

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

            for (Permission permission : USER_PERMISSIONS) {

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

            for (Permission permission : USER_PERMISSIONS) {

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

            for (Permission permission : USER_PERMISSIONS) {

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

            for (Permission permission : USER_PERMISSIONS) {

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

    static void addCafeChatPermission(int userID) {

        Set<String> permissions = getRealPermissions(userID, "CafeGame");

        for (String permission : permissions) {

            Permission permissionData = new Permission();
            permissionData.appName = "CafeChat";
            permissionData.userID = userID;
            permissionData.permission = permission;

            USER_PERMISSIONS.add(permissionData);
        }
    }

    public static boolean removePermission(Connection connection, int userID, String appName, String permission) {

        String deleteStatement = "DELETE FROM " + BaseConfig.TABLE_ACCOUNTS_PERMISSIONS + " WHERE " +
                FIELD_USER_ID + " = ? AND " + FIELD_APP_NAME + " = ? AND " + FIELD_PERMISSION + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, permission);

        return BaseDatabaseHelperOld.update(connection, TAG, deleteStatement, parameters, (wasSuccessful, error) -> {

            Utilities.lock(TAG, LOCK.writeLock(), () -> {

                for (int index = USER_PERMISSIONS.size()-1; index >= 0 ; index--) {

                    Permission loopingPermission = USER_PERMISSIONS.get(index);

                    if (loopingPermission.userID == userID && loopingPermission.appName.equals(appName) &&
                            loopingPermission.permission.equals(permission)) {

                        USER_PERMISSIONS.remove(index);
                    }
                }
            });
        });
    }

    private static class Permission {

        int userID;
        String appName;
        String permission;
        String title;
        String description;
    }
}
