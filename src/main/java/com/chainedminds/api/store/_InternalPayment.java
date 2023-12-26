package com.chainedminds.api.store;

import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds.api.accounting._AccountPermissions;
import com.chainedminds.models._Data;
import com.chainedminds.models.payment._TransactionData;
import com.chainedminds.utilities.BackendHelper;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class _InternalPayment {

    private static final String TAG = _InternalPayment.class.getSimpleName();

    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_TRANSACTION_ID = "TransactionID";
    private static final String FIELD_TRANSACTION_TYPE = "TransactionType";
    private static final String FIELD_TRANSACTION_VALUE = "TransactionValue";
    private static final String FIELD_TRANSACTION_TIME = "TransactionTime";

    public static boolean logTransaction(Connection connection, int userID, String type, float value) {

        String updateStatement = "INSERT " + _Config.TABLE_TRANSACTIONS_INTERNAL + " (" + FIELD_USER_ID +
                ", " + FIELD_TRANSACTION_TYPE + ", " + FIELD_TRANSACTION_VALUE + ") VALUES (?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, type);
        parameters.put(3, value);

        return _DatabaseOld.update(connection, TAG, updateStatement, parameters);
    }

    public static int getSubscriptionPeriod(String market) {

        int days = 0;

        String paymentOption = BackendHelper.getPaymentType(market);

        if (paymentOption.equals(_Config.PAYMENT_OPTION_IAB)) {

            days = 30;
        }

        if (paymentOption.equals(_Config.PAYMENT_OPTION_IPG)) {

            days = 30;
        }

        if (paymentOption.equals(_Config.PAYMENT_OPTION_VAS)) {

            days = 60;
        }

        return days;
    }

    public static _Data getMyTransactions(_Data data) {

        data.response = _Codes.RESPONSE_NOK;

        int userID = data.account.id;

        List<_TransactionData> transactionsList = new ArrayList<>();

        Map<Integer, Object> parameters = new HashMap<>();
        parameters.put(1, userID);

        String statement = "SELECT * FROM " + _Config.TABLE_TRANSACTIONS_INTERNAL + " WHERE " + FIELD_USER_ID +
                " = ? ORDER BY " + FIELD_TRANSACTION_TIME + " DESC";

        _DatabaseOld.query(TAG, statement, parameters,(resultSet) -> {

            while (resultSet.next()) {

                _TransactionData transaction = new _TransactionData();

                transaction.id = resultSet.getInt(FIELD_TRANSACTION_ID);
                transaction.userID = resultSet.getInt(FIELD_USER_ID);
                transaction.name = resultSet.getString(FIELD_TRANSACTION_TYPE);
                transaction.price = resultSet.getFloat(FIELD_TRANSACTION_VALUE);
                transaction.purchaseDate = resultSet.getTimestamp(FIELD_TRANSACTION_TIME).getTime();

                transactionsList.add(transaction);
            }
        });

        data.account.transactions = transactionsList;

        data.response = _Codes.RESPONSE_OK;

        return data;
    }

    //FIXME
    public static _Data checkSubscription(_Data data) {

        data.response = _Codes.RESPONSE_NOK;

        int userID = data.account.id;
        String appName = data.client.appName;

        data.account.subscriptions = _AccountPermissions.getPermissions(userID, appName);

        data.response = _Codes.RESPONSE_OK;

        return data;
    }
}



