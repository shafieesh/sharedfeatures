package com.chainedminds.api.store;

import com.chainedminds._Classes;
import com.chainedminds._Codes;
import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.utilities.database._DatabaseOld;
import com.chainedminds.utilities.database.TwoStepQueryCallback;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class _IABSubscriptionPurchase<IABTransactionData extends _IABTransactionData> {

    private static final String TAG = _IABSubscriptionPurchase.class.getSimpleName();

    private static final String CAFEBAZAAR_API_URL = "https://pardakht.cafebazaar.ir/devapi/v2/";
    private static final String JHOOBIN_API_URL = "https://seller.jhoobin.com/ws/androidpublisher/v2/applications/";
    private static final String MYKET_API_URL = "https://developer.myket.ir/api/applications/";

    private static final String CAFEBAZAAR_PRODUCT = "androidpublisher#inappPurchase";
    private static final String CAFEBAZAAR_SUBSCRIPTION = "androidpublisher#subscriptionPurchase";

    private static final String MYKET_PRODUCT = "androidpublisher#productPurchase";
    private static final String MYKET_SUBSCRIPTION = "androidpublisher#productPurchase";

    private static final String MYKET_ACCESS_TOKEN = "3bea2378-2df0-455c-b573-b24d1aec0315";
    private static final String JHOOBIN_ACCESS_TOKEN = "a3819cde-613c-36b4-b0c8-7b8be75e8539";
    private static final String VADA_ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhd" +
            "WQiOiIwMzEwMGRjOWFkMjIxOTQ5NTk3ODVkMDJjODkzN2IyM2Q0NmRkMmJjIiwiZXhwIjoxODM5NzYxOTM" +
            "4LCJleHQiOiIwMzEwMGRjOWFkMjIxOTQ5NTk3ODVkMDJjODkzN2IyM2Q0NmRkMmJjIiwiaWF0IjoxNTI0N" +
            "DAxOTM4LCJpc3MiOiJodHRwczovL3d3dy5hcHByb28uaXIiLCJqdGkiOiJiZmM4OWY0M2MzMGMxOTlmZjF" +
            "kYjI5ZDI2OWVjZjc4NGVhMjNhYTNkIiwic3BlIjoiZGV2ZWxvcGVyLWFwaSJ9.K5YmpaGOKl7187OPAiTe" +
            "07p5hVMqvdihFsEoAS4y9traauBf9wcLrrtFCkTTwQR0BLn8_SkFopua2gc1d6sWP1IxZIB_HyLWslazbG" +
            "9Kq8lBuykPuvOxqpN6PX-UP7YxrpTuYZ-7RsvnqDYYfdZnu0NONtehlnCdcMWJdOvg-IycQcaR3qpINU1n" +
            "hGZCVGvfRpip8s7RPkQ6i2hNwFCsQbDNaRaMPh1bPQUaY6jURiPid1y4G0OxxFsMRJfYqdr-iuXiK6F066" +
            "82mF3wwTbrHPCBWdpi6LlFwvz6bvZXEw47YRiVzVM8_qdcevYD9qIvJ-C0pJt3o3ZZhwo_FjS_vA";

    public static final String FIELD_ID = "ID";
    public static final String FIELD_USER_ID = "UserID";
    public static final String FIELD_APP_NAME = "AppName";
    public static final String FIELD_MARKET = "Market";
    public static final String FIELD_SKU = "SKU";
    public static final String FIELD_TOKEN = "Token";
    public static final String FIELD_PAYLOAD = "Payload";
    public static final String FIELD_STATE = "State";
    public static final String FIELD_PURCHASE_DATE = "PurchaseDate";
    public static final String FIELD_EXPIRATION_DATE = "ExpirationDate";

    public void checkSubscriptions() {

        List<IABTransactionData> subscriptionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
                " WHERE " + FIELD_STATE + " != " + _IABPayment.PURCHASE_STATE_REJECTED +
                " AND " + FIELD_EXPIRATION_DATE + " > DATE_SUB(NOW(), INTERVAL 7 DAY)";

        _DatabaseOld.query(TAG, selectStatement, resultSet -> {

            while (resultSet.next()) {

                IABTransactionData iabTransaction = readTransaction(resultSet);

                subscriptionsList.add(iabTransaction);
            }
        });

        for (_IABTransactionData iabTransaction : subscriptionsList) {

            Boolean verified = _Resources.get().iabPayment.verifyIABTransaction(iabTransaction);

            if (verified != null && verified) {

                updateExpirationDate(iabTransaction.id, iabTransaction.expirationDate);

                if (iabTransaction.expirationDate > System.currentTimeMillis()) {

                    //TODO
                    //BaseResources.getInstance().accountManager.setPremiumPass(iabTransaction.userID, true);
                }
            }
        }
    }

    public int addIABTransaction(IABTransactionData transaction) {

        AtomicInteger transactionID = new AtomicInteger(_Codes.NOT_FOUND);

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        String token = transaction.token;
        Timestamp expirationDate = new Timestamp(transaction.expirationDate);

        String statement = "INSERT " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET +
                ", " + FIELD_SKU + ", " + FIELD_TOKEN + ", " + FIELD_EXPIRATION_DATE + ")" +
                " VALUES (?, ?, ?, ?, ?, ?) ";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, market);
        parameters.put(4, sku);
        parameters.put(5, token);
        parameters.put(6, expirationDate.getTime() == 0 ? null : expirationDate);

        _DatabaseOld.insert(TAG, statement, parameters, (wasSuccessful, generatedID, error) -> transactionID.set(generatedID));

        return transactionID.get();
    }

    public IABTransactionData getTransactionByID(int transactionID) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
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

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
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

    public Set<Integer> getActiveSubscribers() {

        AtomicBoolean queryResultWasOK = new AtomicBoolean();
        Set<Integer> activeSubscribersList = new HashSet<>();

        String selectStatement = "SELECT DISTINCT " + FIELD_USER_ID + " FROM " +
                _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS + " WHERE " + FIELD_STATE +
                " = " + _IABPayment.PURCHASE_STATE_APPLIED + " AND " +
                FIELD_EXPIRATION_DATE + " > NOW()";

        _DatabaseOld.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private final Set<Integer> fetchingUserIDs = new HashSet<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    fetchingUserIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful, Exception error) {

                queryResultWasOK.set(wasSuccessful);

                if (wasSuccessful) {

                    activeSubscribersList.addAll(fetchingUserIDs);
                }
            }
        });

        if (queryResultWasOK.get()) {

            return activeSubscribersList;

        } else {

            return null;
        }
    }

    public IABTransactionData getIABTransaction(String market, String token) {

        AtomicReference<IABTransactionData> iabTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
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

    public List<IABTransactionData> getPendingTransactions() {

        List<IABTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
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

    public boolean updateTransactionState(Connection connection, int id, int state, long purchaseDate, long expirationDate) {

        System.out.println("UPDATING " + id + " : " + new Timestamp(purchaseDate) + " - " + new Timestamp(expirationDate));

        String updateStatement = "UPDATE " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
                " SET " + FIELD_STATE + " = ?, " + FIELD_PURCHASE_DATE + " = ?, " +
                FIELD_EXPIRATION_DATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, purchaseDate == 0 ? null : new Timestamp(purchaseDate));
        parameters.put(3, expirationDate == 0 ? null : new Timestamp(expirationDate));
        parameters.put(4, id);

        if (connection != null) {

            return _DatabaseOld.update(connection, TAG, updateStatement, parameters);

        } else {

            return _DatabaseOld.update(TAG, updateStatement, parameters);
        }
    }

    public boolean updateExpirationDate(int id, long expirationDate) {

        String updateStatement = "UPDATE " + _Config.TABLE_PURCHASES_IAB_SUBSCRIPTIONS +
                " SET " + FIELD_EXPIRATION_DATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, expirationDate == 0 ? null : new Timestamp(expirationDate));
        parameters.put(2, id);

        return _DatabaseOld.update(TAG, updateStatement, parameters);
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
        Timestamp expirationDate = resultSet.getTimestamp(FIELD_EXPIRATION_DATE);

        if (purchaseDate != null) {

            iabTransaction.purchaseDate = purchaseDate.getTime();
        }

        if (expirationDate != null) {

            iabTransaction.expirationDate = expirationDate.getTime();
        }

        return iabTransaction;
    }
}