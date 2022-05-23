package com.chainedminds.api.store;

import com.chainedminds.BaseConfig;
import com.chainedminds.models.payment.BaseIPGTransactionData;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class BaseIPGSubscriptionPurchasesManager<IPGTransactionData extends BaseIPGTransactionData> {

    private static final String TAG = BaseIPGSubscriptionPurchasesManager.class.getSimpleName();

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

    /*public void checkSubscriptions() {

        List<IPGTransactionData> subscriptionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " WHERE " + FIELD_STATE + " != " + BaseIPGPaymentManager.PURCHASE_STATE_REJECTED +
                " AND " + FIELD_EXPIRATION_DATE + " > DATE_SUB(NOW(), INTERVAL 7 DAY)";

        DatabaseHelper.query(TAG, selectStatement, resultSet -> {

            while (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                subscriptionsList.add(ipgTransaction);
            }
        });

        for (BaseIPGTransactionData ipgTransaction : subscriptionsList) {

            Boolean verified = BaseResources.getInstance().ipgPaymentManager.verifyIPGTransaction(ipgTransaction);

            if (verified != null && verified) {

                updateExpirationDate(ipgTransaction.id, ipgTransaction.expirationDate);

                if (ipgTransaction.expirationDate > System.currentTimeMillis()) {

                    BaseResources.getInstance().accountManager.setPremiumPass(ipgTransaction.userID, true);
                }
            }
        }
    }*/

    /*public int addIPGTransaction(IPGTransactionData transaction) {

        AtomicInteger transactionID = new AtomicInteger(BaseCodes.NOT_FOUND);

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        String token = transaction.token;
        String payload = transaction.payload;
        Timestamp expirationDate = new Timestamp(transaction.expirationDate);

        String statement = "INSERT " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET +
                ", " + FIELD_SKU + ", " + FIELD_TOKEN + ", " + FIELD_PAYLOAD +
                ", " + FIELD_EXPIRATION_DATE + ") VALUES (?, ?, ?, ?, ?, ?, ?) ";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, market);
        parameters.put(4, sku);
        parameters.put(5, token);
        parameters.put(6, payload);
        parameters.put(7, expirationDate.getTime() == 0 ? null : expirationDate);

        DatabaseHelper.insert(TAG, statement, parameters, transactionID::set);

        return transactionID.get();
    }*/

    /*public IPGTransactionData getTransactionByID(int transactionID) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, transactionID);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                ipgTransactionHolder.set(ipgTransaction);
            }
        });

        return ipgTransactionHolder.get();
    }*/

    /*public List<IPGTransactionData> getTransactionsByUserID(int userID) {

        List<IPGTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " WHERE " + FIELD_USER_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            while (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                transactionsList.add(ipgTransaction);
            }
        });

        return transactionsList;
    }*/

    /*public Set<Integer> getActiveSubscribers() {

        AtomicBoolean queryResultWasOK = new AtomicBoolean();
        Set<Integer> activeSubscribersList = new HashSet<>();

        String selectStatement = "SELECT DISTINCT " + FIELD_USER_ID + " FROM " +
                BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS + " WHERE " + FIELD_STATE +
                " = " + BaseIPGPaymentManager.PURCHASE_STATE_APPLIED + " AND " +
                FIELD_EXPIRATION_DATE + " > NOW()";

        DatabaseHelper.query(TAG, selectStatement, new TwoStepQueryCallback() {

            private Set<Integer> fetchingUserIDs = new HashSet<>();

            @Override
            public void onFetchingData(ResultSet resultSet) throws Exception {

                while (resultSet.next()) {

                    fetchingUserIDs.add(resultSet.getInt(FIELD_USER_ID));
                }
            }

            @Override
            public void onFinishedTask(boolean wasSuccessful) {

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
    }*/

    /*public IPGTransactionData getIPGTransaction(String market, String token) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " WHERE " + FIELD_MARKET + " = ? AND " + FIELD_TOKEN + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, market);
        parameters.put(2, token);

        DatabaseHelper.query(TAG, selectStatement, parameters, resultSet -> {

            if (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                ipgTransactionHolder.set(ipgTransaction);
            }
        });

        return ipgTransactionHolder.get();
    }*/

    /*public List<IPGTransactionData> getPendingTransactions() {

        List<IPGTransactionData> transactionsList = new ArrayList<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " WHERE " + FIELD_STATE + " = " + BaseIPGPaymentManager.PURCHASE_STATE_PENDING +
                " OR " + FIELD_STATE + " = " + BaseIPGPaymentManager.PURCHASE_STATE_VERIFIED;

        DatabaseHelper.query(TAG, selectStatement, resultSet -> {

            while (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                transactionsList.add(ipgTransaction);
            }
        });

        return transactionsList;
    }*/

    public boolean updateTransactionState(Connection connection, int id, int state, long purchaseDate, long expirationDate) {

        System.out.println("UPDATING " + id + " : " + new Timestamp(purchaseDate) + " - " + new Timestamp(expirationDate));

        String updateStatement = "UPDATE " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " SET " + FIELD_STATE + " = ?, " + FIELD_PURCHASE_DATE + " = ?, " +
                FIELD_EXPIRATION_DATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, purchaseDate == 0 ? null : new Timestamp(purchaseDate));
        parameters.put(3, expirationDate == 0 ? null : new Timestamp(expirationDate));
        parameters.put(4, id);

        if (connection != null) {

            return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);

        } else {

            return BaseDatabaseHelperOld.update(TAG, updateStatement, parameters);
        }
    }

    public boolean updateExpirationDate(int id, long expirationDate) {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_PURCHASES_IPG_SUBSCRIPTIONS +
                " SET " + FIELD_EXPIRATION_DATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, expirationDate == 0 ? null : new Timestamp(expirationDate));
        parameters.put(2, id);

        return BaseDatabaseHelperOld.update(TAG, updateStatement, parameters);
    }

    /*private IPGTransactionData readTransaction(ResultSet resultSet) throws Exception {

        IPGTransactionData ipgTransaction = (IPGTransactionData) BaseClasses
                .construct(BaseClasses.getInstance().ipgTransactionClass);

        ipgTransaction.category = BaseProductsManager.CATEGORY_SUBSCRIPTION;

        ipgTransaction.id = resultSet.getInt(FIELD_ID);
        ipgTransaction.userID = resultSet.getInt(FIELD_USER_ID);
        ipgTransaction.appName = resultSet.getString(FIELD_APP_NAME);
        ipgTransaction.market = resultSet.getString(FIELD_MARKET);
        ipgTransaction.sku = resultSet.getString(FIELD_SKU);
        ipgTransaction.token = resultSet.getString(FIELD_TOKEN);
        ipgTransaction.payload = resultSet.getString(FIELD_PAYLOAD);
        ipgTransaction.state = resultSet.getInt(FIELD_STATE);

        Timestamp purchaseDate = resultSet.getTimestamp(FIELD_PURCHASE_DATE);
        Timestamp expirationDate = resultSet.getTimestamp(FIELD_EXPIRATION_DATE);

        if (purchaseDate != null) {

            ipgTransaction.purchaseDate = purchaseDate.getTime();
        }

        if (expirationDate != null) {

            ipgTransaction.expirationDate = expirationDate.getTime();
        }

        return ipgTransaction;
    }*/

    /*public Boolean cancelIPGTransaction(BaseIPGTransactionData transaction) {

        String sku = transaction.sku;
        String token = transaction.token;
        String market = transaction.market;
        String appName = transaction.appName;
        String packageName = BackendHelper.getPackageName(appName, market);

        String apiUrl;

        Boolean canceled = null;

        CafeBazaarClass marketData;
        String response;

        switch (market) {

            case BaseConfig.MARKET_ROYAL:

                apiUrl = JHOOBIN_API_URL + packageName + "/purchases/subscriptions/" + sku +
                        "/tokens/" + token + ":cancel?access_token=" + JHOOBIN_ACCESS_TOKEN;

                response = DataTransportManager.httpGet(apiUrl);

                marketData = JsonHelper.getObject(response, CafeBazaarClass.class);

                if (marketData != null) {

                    canceled = marketData.error == null;
                }

                break;
        }

        if (canceled == null) {

            return null;
        }

        Connection connection = ConnectionManager.getConnection();

        if (connection == null) {

            return null;
        }

        if (canceled) {

            //transaction.verified = false;

            //FIXME CHANGE FROM 0
            updateTransactionState(connection, transaction.id, 0, transaction.purchaseDate, transaction.expirationDate);

            //BaseResources.getInstance().ipgPaymentManager.updateIPGTransaction(connection, transaction);
        }

        ConnectionManager.close(connection);

        return canceled;
    }*/
}