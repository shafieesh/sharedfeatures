package com.chainedminds.api.store;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.dataClasses.payment.BaseIABTransactionData;
import com.chainedminds.utilities.database.DatabaseHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BaseIABProductPurchasesManager<IABTransactionData extends BaseIABTransactionData> {

    private static final String TAG = BaseIABProductPurchasesManager.class.getSimpleName();

    private static final String FIELD_ID = "ID";
    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_MARKET = "Market";
    private static final String FIELD_SKU = "SKU";
    private static final String FIELD_TOKEN = "Token";
    private static final String FIELD_PAYLOAD = "Payload";
    private static final String FIELD_STATE = "State";
    private static final String FIELD_PURCHASE_DATE = "PurchaseDate";

    public int addIABTransaction(IABTransactionData transaction) {

        AtomicInteger transactionID = new AtomicInteger(BaseCodes.NOT_FOUND);

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        String token = transaction.token;

        String statement = "INSERT " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET +
                ", " + FIELD_SKU + ", " + FIELD_TOKEN + ") VALUES (?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, market);
        parameters.put(4, sku);
        parameters.put(5, token);

        DatabaseHelper.insert(TAG, statement, parameters,
                (wasSuccessful, generatedID, error) -> transactionID.set(generatedID));

        return transactionID.get();
    }

    public IABTransactionData getTransactionByID(int transactionID) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, transactionID);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                iabTransactionHolder.set(iabTransaction);
            }
        });

        return iabTransactionHolder.get();
    }

    public List<IABTransactionData> getTransactionsByUserID(int userID) {

        List<IABTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            while (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                transactionsList.add(iabTransaction);
            }
        });

        return transactionsList;
    }

    public IABTransactionData getIABTransaction(String market, String token) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_MARKET + " = ? AND " + FIELD_TOKEN + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, market);
        parameters.put(2, token);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                iabTransactionHolder.set(iabTransaction);
            }
        });

        return iabTransactionHolder.get();
    }

    public List<IABTransactionData> getPendingTransactions() {

        List<IABTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_STATE + " = " + BaseIABPaymentManager.PURCHASE_STATE_PENDING +
                " OR " + FIELD_STATE + " = " + BaseIABPaymentManager.PURCHASE_STATE_VERIFIED;

        DatabaseHelper.query(TAG, selectStatement, resultSet -> {

            while (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                transactionsList.add(iabTransaction);
            }
        });

        return transactionsList;
    }

    public boolean updateTransactionState(Connection connection, int id, int state) {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_PURCHASES_IAB_PRODUCTS +
                " SET " + FIELD_STATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, id);

        if (connection != null) {

            return DatabaseHelper.update(connection, TAG, updateStatement, parameters);

        } else {

            return DatabaseHelper.update(TAG, updateStatement, parameters);
        }
    }

    private IABTransactionData readTransaction(ResultSet resultSet) throws Exception {

        IABTransactionData iabTransaction = (IABTransactionData) BaseClasses
                .construct(BaseClasses.getInstance().iabTransactionClass);

        iabTransaction.category = BaseProductsManager.CATEGORY_SUBSCRIPTION;

        iabTransaction.id = resultSet.getInt(FIELD_ID);
        iabTransaction.userID = resultSet.getInt(FIELD_USER_ID);
        iabTransaction.appName = resultSet.getString(FIELD_APP_NAME);
        iabTransaction.market = resultSet.getString(FIELD_MARKET);
        iabTransaction.sku = resultSet.getString(FIELD_SKU);
        iabTransaction.token = resultSet.getString(FIELD_TOKEN);
        iabTransaction.state = resultSet.getInt(FIELD_STATE);

        Timestamp purchaseDate = resultSet.getTimestamp(FIELD_PURCHASE_DATE);

        if (purchaseDate != null) {

            iabTransaction.purchaseDate = purchaseDate.getTime();
        }

        return iabTransaction;
    }
}