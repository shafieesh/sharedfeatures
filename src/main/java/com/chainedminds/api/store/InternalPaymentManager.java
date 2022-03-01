package com.chainedminds.api.store;

import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.api.accounting.BaseAccountPermissionsManager;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.payment.BaseTransactionData;
import com.chainedminds.utilities.BackendHelper;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalPaymentManager {

    private static final String TAG = InternalPaymentManager.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_TRANSACTION_ID = "TransactionID";
    private static final String FIELD_TRANSACTION_TYPE = "TransactionType";
    private static final String FIELD_TRANSACTION_VALUE = "TransactionValue";
    private static final String FIELD_TRANSACTION_TIME = "TransactionTime";

    public static boolean logTransaction(Connection connection, int userID, String type, float value) {

        String updateStatement = "INSERT " + BaseConfig.TABLE_TRANSACTIONS_INTERNAL + " (" + FIELD_USER_ID +
                ", " + FIELD_TRANSACTION_TYPE + ", " + FIELD_TRANSACTION_VALUE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, type);
        parameters.put(3, value);

        return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);
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

    public static BaseData getMyTransactions(BaseData data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;

        List<BaseTransactionData> transactionsList = new ArrayList<>();

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        String statement = "SELECT * FROM " + BaseConfig.TABLE_TRANSACTIONS_INTERNAL + " WHERE " + FIELD_USER_ID +
                " = ? ORDER BY " + FIELD_TRANSACTION_TIME + " DESC";

        BaseDatabaseHelperOld.query(TAG, statement, parameters,(resultSet) -> {

            while (resultSet.next()) {

                BaseTransactionData transaction = new BaseTransactionData();

                transaction.id = resultSet.getInt(FIELD_TRANSACTION_ID);
                transaction.userID = resultSet.getInt(FIELD_USER_ID);
                transaction.name = resultSet.getString(FIELD_TRANSACTION_TYPE);
                transaction.price = resultSet.getFloat(FIELD_TRANSACTION_VALUE);
                transaction.purchaseDate = resultSet.getTimestamp(FIELD_TRANSACTION_TIME).getTime();

                transactionsList.add(transaction);
            }
        });

        data.account.transactions = transactionsList;

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }

    //FIXME
    public static BaseData checkSubscription(BaseData data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        String appName = data.client.appName;

        data.account.subscriptions = BaseAccountPermissionsManager.getPermissions(userID, appName);

        data.response = BaseCodes.RESPONSE_OK;

        return data;
    }
}



