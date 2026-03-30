package com.chainedminds.api.accounting;

import com.chainedminds._Config;
import com.chainedminds.models._PermissionData;
import com.chainedminds.utilities.Utilities;
import com.chainedminds.utilities.database.TwoStepQueryCallback;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class _Permissions {

    private static final String TAG = _Permissions.class.getSimpleName();

    protected static final String FIELD_PERMISSION = "Permission";
    protected static final String FIELD_POSITION = "Position";
    protected static final String FIELD_CATEGORY = "Category";
    protected static final String FIELD_TITLE = "Title";
    protected static final String FIELD_DESCRIPTION = "Description";

    protected static final List<_PermissionData> PERMISSIONS = new ArrayList<>();

    protected static final ReadWriteLock LOCK = new ReentrantReadWriteLock();

    protected static void fetch() {

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PERMISSIONS + " ORDER BY " + FIELD_POSITION;

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final List<_PermissionData> permissions = new ArrayList<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    _PermissionData permission = new _PermissionData();
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

    public static List<_PermissionData> getAll() {

        List<_PermissionData> permissions = new ArrayList<>();

        Utilities.lock(TAG, LOCK.readLock(), () -> permissions.addAll(PERMISSIONS));

        return permissions;
    }
}
