package com.chainedminds.api.store;

import com.chainedminds._Classes;
import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class _IABProductPurchase<IABTransactionData extends _IABTransactionData> {

    private static final String TAG = _IABProductPurchase.class.getSimpleName();

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

        AtomicInteger transactionID = new AtomicInteger(_Codes.NOT_FOUND);

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        String token = transaction.token;

        String statement = "INSERT " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET +
                ", " + FIELD_SKU + ", " + FIELD_TOKEN + ") VALUES (?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, market);
        parameters.put(4, sku);
        parameters.put(5, token);

        _DatabaseOld.insert(TAG, statement, parameters,
                (wasSuccessful, generatedID, error) -> transactionID.set(generatedID));

        return transactionID.get();
    }

    public IABTransactionData getTransactionByID(int transactionID) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, transactionID);

        _DatabaseOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                iabTransactionHolder.set(iabTransaction);
            }
        });

        return iabTransactionHolder.get();
    }

    public List<IABTransactionData> getTransactionsByUserID(int userID) {

        List<IABTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        _DatabaseOld.query(TAG, selectStatement, parameters, resultSet -> {

            while (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                transactionsList.add(iabTransaction);
            }
        });

        return transactionsList;
    }

    public IABTransactionData getIABTransaction(String market, String token) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_MARKET + " = ? AND " + FIELD_TOKEN + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, market);
        parameters.put(2, token);

        _DatabaseOld.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                iabTransactionHolder.set(iabTransaction);
            }
        });

        return iabTransactionHolder.get();
    }

    public List<_IABTransactionData> getPendingTransactions() {

        List<_IABTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " WHERE " + FIELD_STATE + " = " + _IABPayment.PURCHASE_STATE_PENDING +
                " OR " + FIELD_STATE + " = " + _IABPayment.PURCHASE_STATE_VERIFIED;

        _DatabaseOld.query(TAG, selectStatement, resultSet -> {

            while (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                transactionsList.add(iabTransaction);
            }
        });

        return transactionsList;
    }

    public boolean updateTransactionState(Connection connection, int id, int state) {

        String updateStatement = "UPDATE " + _Config.TABLE_PURCHASES_IAB_PRODUCTS +
                " SET " + FIELD_STATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, id);

        if (connection != null) {

            return _DatabaseOld.update(connection, TAG, updateStatement, parameters);

        } else {

            return _DatabaseOld.update(TAG, updateStatement, parameters);
        }
    }

    private IABTransactionData readTransaction(ResultSet resultSet) throws Exception {

        IABTransactionData iabTransaction = (IABTransactionData) _Classes
                .construct(_Classes.getInstance().iabTransactionClass);

        iabTransaction.category = _Product.CATEGORY_SUBSCRIPTION;

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