package com.chainedminds.api.store;

import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.api.accounting.AccountPermissionsManager;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.utilities.BackendHelper;
import com.chainedminds.utilities.database.DatabaseHelper;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InternalPaymentManager {

    private static final String TAG = InternalPaymentManager.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_TRANSACTION_TYPE = "TransactionType";
    private static final String FIELD_TRANSACTION_VALUE = "TransactionValue";

    public static boolean logTransaction(Connection connection, int userID, String type, float value) {

        String updateStatement = "INSERT " + BaseConfig.TABLE_TRANSACTIONS_INTERNAL + " (" + FIELD_USER_ID +
                ", " + FIELD_TRANSACTION_TYPE + ", " + FIELD_TRANSACTION_VALUE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, type);
        parameters.put(3, value);

        return DatabaseHelper.update(connection, TAG, updateStatement, parameters);
    }

    public static int getSubscriptionPeriod(String market) {

        int days = 0;

        String paymentOption = BackendHelper.getPaymentType(market);

        if (paymentOption.equals(BaseConfig.PAYMENT_OPTION_IAB)) {

            days = 30;
        }

        if (paymentOption.equals(BaseConfig.PAYMENT_OPTION_IPG)) {

            days = 30;
        }

        if (paymentOption.equals(BaseConfig.PAYMENT_OPTION_VAS)) {

            days = 60;
        }

        return days;
    }

    //FIXME
    public static BaseData checkSubscription(BaseData data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        String appName = data.client.appName;

        data.account.subscriptions = new ArrayList<>(AccountPermissionsManager.getPermissions(userID, appName));

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }
}



