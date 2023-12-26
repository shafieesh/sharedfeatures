package com.chainedminds.api.accounting;

import com.chainedminds._Config;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class _Profile {

    private static final String TAG = _Profile.class.getSimpleName();

    public static final String FIELD_USER_ID = "UserID";
    public static final String FIELD_AGE = "Age";
    public static final String FIELD_SEX = "Sex";

    public final boolean setProperty(int userID, String fieldName, Object value) {

        String statement = "UPDATE " + _Config.TABLE_ACCOUNTS_PROFILES + " SET " +
                fieldName + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, value);
        parameters.put(2, userID);

        return _DatabaseOld.update(TAG, statement, parameters);
    }

    public final boolean setProperty(Connection connection, int userID, String fieldName, Object value) {

        String statement = "UPDATE " + _Config.TABLE_ACCOUNTS_PROFILES + " SET " +
                fieldName + " = ? WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, value);
        parameters.put(2, userID);

        return _DatabaseOld.update(connection, TAG, statement, parameters);
    }
}