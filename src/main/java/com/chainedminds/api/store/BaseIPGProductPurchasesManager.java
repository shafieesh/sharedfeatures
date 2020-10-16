package com.chainedminds.api.store;

import com.chainedminds.dataClasses.payment.BaseIPGTransactionData;

public class BaseIPGProductPurchasesManager<IPGTransactionData extends BaseIPGTransactionData> {

    private static final String TAG = BaseIPGProductPurchasesManager.class.getSimpleName();

    private static final String FIELD_ID = "ID";
    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_MARKET = "Market";
    private static final String FIELD_SKU = "SKU";
    private static final String FIELD_TOKEN = "Token";
    private static final String FIELD_PAYLOAD = "Payload";
    private static final String FIELD_STATE = "State";
    private static final String FIELD_PURCHASE_DATE = "PurchaseDate";

    /*public int addIPGTransaction(IPGTransactionData transaction) {

        AtomicInteger transactionID = new AtomicInteger(BaseCodes.NOT_FOUND);

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        String token = transaction.token;
        String payload = transaction.payload;

        String statement = "INSERT " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
                " (" + FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET +
                ", " + FIELD_SKU + ", " + FIELD_TOKEN + ", " + FIELD_PAYLOAD + ") " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, userID);
        parameters.put(2, appName);
        parameters.put(3, market);
        parameters.put(4, sku);
        parameters.put(5, token);
        parameters.put(6, payload);

        DatabaseHelper.insert(TAG, statement, parameters, transactionID::set);

        return transactionID.get();
    }*/

    /*public IPGTransactionData getTransactionByID(int transactionID) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
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

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
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

    /*public IPGTransactionData getIPGTransaction(String market, String token) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
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

        String selectStatement = "SELECT * FROM " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
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

    /*public boolean updateTransactionState(Connection connection, int id, int state) {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
                " SET " + FIELD_STATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, id);

        if (connection != null) {

            return DatabaseHelper.update(connection, TAG, updateStatement, parameters);

        } else {

            return DatabaseHelper.update(TAG, updateStatement, parameters);
        }
    }*/

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

        if (purchaseDate != null) {

            ipgTransaction.purchaseDate = purchaseDate.getTime();
        }

        return ipgTransaction;
    }*/
}