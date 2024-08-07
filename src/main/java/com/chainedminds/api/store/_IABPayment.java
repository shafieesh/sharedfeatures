package com.chainedminds.api.store;

import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.api._API;
import com.chainedminds.models._ProductData;
import com.chainedminds.models.market.CafeBazaarClass;
import com.chainedminds.models.market.JhoobinClass;
import com.chainedminds.models.market.MarketData;
import com.chainedminds.models.payment._IABTransactionData;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.json.Json;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class _IABPayment<
        IABTransactionData extends _IABTransactionData,
        ProductData extends _ProductData> extends _Payment<ProductData> {

    private static final String TAG = _IABPayment.class.getSimpleName();

    public static final Map<String, String> ACCESS_TOKENS_MAP = new HashMap<>();

    private static final String CAFEBAZAAR_API_URL = "https://pardakht.cafebazaar.ir/devapi/v2/";
    private static final String GOOGLEPLAY_API_URL = "https://www.googleapis.com/androidpublisher/v3/applications/";
    private static final String JHOOBIN_API_URL = "https://seller.jhoobin.com/ws/androidpublisher/v2/applications/";
    private static final String MYKET_API_URL = "https://developer.myket.ir/api/applications/";

    private static final String GOOGLEPLAY_PRODUCT = "androidpublisher#productPurchase";
    private static final String GOOGLEPLAY_SUBSCRIPTION = "androidpublisher#subscriptionPurchase";

    private static final String CAFEBAZAAR_PRODUCT = "androidpublisher#inappPurchase";
    private static final String CAFEBAZAAR_SUBSCRIPTION = "androidpublisher#subscriptionPurchase";

    private static final String MYKET_PRODUCT_OR_SUBSCRIPTION = "androidpublisher#productPurchase";

    private static final String JHOOBIN_PRODUCT = "androidpublisher#productPurchase";
    private static final String JHOOBIN_SUBSCRIPTION = "androidpublisher#subscriptionPurchase";

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

    private static final String FIELD_ID = "ID";
    private static final String FIELD_USER_ID = "UserID";
    private static final String FIELD_APP_NAME = "AppName";
    private static final String FIELD_MARKET = "Market";
    private static final String FIELD_CATEGORY = "Category";
    private static final String FIELD_SKU = "SKU";
    private static final String FIELD_TOKEN = "Token";
    private static final String FIELD_DATETIME = "DateTime";
    private static final String FIELD_VERIFIED = "Verified";
    private static final String FIELD_APPLIED = "Applied";
    private static final String FIELD_PAYLOAD = "Payload";
    private static final String FIELD_TRANSACTION_ID = "TransactionID";
    private static final String FIELD_TYPE = "Type";

    private static final int PRODUCT_TYPE_ANNUAL_PURCHASE = 1;
    private static final int PRODUCT_TYPE_1_TIME_PURCHASE = 2;
    private static final int PRODUCT_TYPE_1_WEEK_SUBSCRIPTION = 3;
    private static final int PRODUCT_TYPE_1_MONTH_SUBSCRIPTION = 4;
    private static final int PRODUCT_TYPE_3_MONTH_SUBSCRIPTION = 5;
    private static final int PRODUCT_TYPE_6_MONTH_SUBSCRIPTION = 6;
    private static final int PRODUCT_TYPE_1_YEAR_SUBSCRIPTION = 7;
    private static final int PRODUCT_TYPE_SPRING_SEASON_SUBSCRIPTION = 8;
    private static final int PRODUCT_TYPE_SUMMER_SEASON_SUBSCRIPTION = 9;
    private static final int PRODUCT_TYPE_AUTUMN_SEASON_SUBSCRIPTION = 10;
    private static final int PRODUCT_TYPE_WINTER_SEASON_SUBSCRIPTION = 11;
    private static final int PRODUCT_CATEGORY_UNDEFINED = 0;

    public static final String PRODUCT_CATEGORY_CONSUMABLE = "consumable";
    public static final String PRODUCT_CATEGORY_NON_CONSUMABLE = "non_consumable";
    public static final String PRODUCT_CATEGORY_SUBSCRIPTION = "subscription";

    public static final String CAFEBAZAAR_CLIENT_ID_PRODUCTION = "Rgu7GyKnt0ByxjUCmozpsbM6DLbW0AS9DB8VAaFe";
    public static final String CAFEBAZAAR_CLIENT_SECRET_PRODUCTION = "aLyZH4G9VGPRQGAdwcYFpwr6uhrTOsNCllDQG7w1XhyQQ7op2jGAomNiczxa";
    public static final String CAFEBAZAAR_REFRESH_TOKEN_PRODUCTION = "PIYYSSP38WBLsryXS5z7FhPUwUjApn";

    public static final String CAFEBAZAAR_CLIENT_ID_TEST = "eRMkh8IFSIbpqqJKYuyEEvKTPYS0Zf6oORxrj12n";
    public static final String CAFEBAZAAR_CLIENT_SECRET_TEST = "Gj3Vdw895F9xXzJahgpUyKPf9mHHrCnF0iPLLdaOmvlBl2ao335IODLozHML";
    public static final String CAFEBAZAAR_REFRESH_TOKEN_TEST = "zfsLXWMgAl40X6CXSBhndemF44SI9l";

    public void start() {

        Task.add(Task.Data.build()
                .setName("RefreshMarketsAccessTokens")
                .setTime(0, 0, 0)
                .setInterval(0, 1, 0, 0)
                .setTimingListener(task -> refreshMarketsAccessTokens())
                .runNow()
                .schedule());

        Task.add(Task.Data.build()
                .setName("CheckPendingIabTransactions")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 2, 0)
                .setTimingListener(task -> checkPendingTransactions())
                .schedule());

        Task.add(Task.Data.build()
                .setName("RefreshSubscriptions")
                .setTime(0, 0, 0)
                .setInterval(1, 0, 0, 0)
                .setTimingListener(task -> _Resources.get().iabSubscriptionPurchase.checkSubscriptions())
                .schedule());
    }

    public static void refreshMarketsAccessTokens() {

        String CAFEBAZAAR_CLIENT_ID;
        String CAFEBAZAAR_CLIENT_SECRET;
        String CAFEBAZAAR_REFRESH_TOKEN;

        String GOOGLEPLAY_CLIENT_ID = "868938648792-u65hc6a3jvsohsv5t0d276oip267sjs1.apps.googleusercontent.com";
        String GOOGLEPLAY_CLIENT_SECRET = "ZJHbhRE3AouPnqg3t_dJoJ6F";
        String GOOGLEPLAY_REFRESH_TOKEN = "1/BJ-PPBfZWA69W8BzcnwDdwFHCCGdJ8AuMC_0CS6CRJE";

        if (_Config.DEBUG_MODE) {

            CAFEBAZAAR_CLIENT_ID = CAFEBAZAAR_CLIENT_ID_TEST;
            CAFEBAZAAR_CLIENT_SECRET = CAFEBAZAAR_CLIENT_SECRET_TEST;
            CAFEBAZAAR_REFRESH_TOKEN = CAFEBAZAAR_REFRESH_TOKEN_TEST;

        } else {

            CAFEBAZAAR_CLIENT_ID = CAFEBAZAAR_CLIENT_ID_PRODUCTION;
            CAFEBAZAAR_CLIENT_SECRET = CAFEBAZAAR_CLIENT_SECRET_PRODUCTION;
            CAFEBAZAAR_REFRESH_TOKEN = CAFEBAZAAR_REFRESH_TOKEN_PRODUCTION;
        }

        try {

            String url = CAFEBAZAAR_API_URL + "auth/token/";
            String body = "grant_type=refresh_token&" +
                    "client_id=" + CAFEBAZAAR_CLIENT_ID + "&" +
                    "client_secret=" + CAFEBAZAAR_CLIENT_SECRET + "&" +
                    "refresh_token=" + CAFEBAZAAR_REFRESH_TOKEN;


            RequestBody requestBody = RequestBody.create(body, null);

            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.post(requestBody);

            _API.instance().callAsync(builder, new _API.ApiCallback() {
                @Override
                public void onError(String error, String message) {

                    System.err.println(error + " : " + message);
                }

                @Override
                public void onResponse(int code, Map<String, List<String>> headers, String response) {

                    CafeBazaarClass marketData = Json.getObject(response, CafeBazaarClass.class);

                    if (marketData != null && marketData.access_token != null) {

                        ACCESS_TOKENS_MAP.put("CafeBazaar", marketData.access_token);
                    }
                }
            });

        } catch (Exception e) {

            _Log.error(TAG, e);
        }

        try {

            String url = "https://accounts.google.com/o/oauth2/token";
            String body = "grant_type=refresh_token&" +
                    "client_id=" + GOOGLEPLAY_CLIENT_ID + "&" +
                    "client_secret=" + GOOGLEPLAY_CLIENT_SECRET + "&" +
                    "refresh_token=" + GOOGLEPLAY_REFRESH_TOKEN;


            RequestBody requestBody = RequestBody.create(body, null);

            Request.Builder builder = new Request.Builder();
            builder.url(url);
            builder.post(requestBody);

            _API.instance().callAsync(builder, new _API.ApiCallback() {
                @Override
                public void onError(String error, String message) {

                    System.err.println(error + " : " + message);
                }

                @Override
                public void onResponse(int code, Map<String, List<String>> headers, String response) {

                    CafeBazaarClass marketData = Json.getObject(response, CafeBazaarClass.class);

                    if (marketData != null && marketData.access_token != null) {

                        ACCESS_TOKENS_MAP.put("GooglePlay", marketData.access_token);
                    }
                }
            });

        } catch (Exception e) {

            _Log.error(TAG, e);
        }
    }

    private void checkPendingTransactions() {

        List<_IABTransactionData> pendingTransactionsList = new ArrayList<>();

        pendingTransactionsList.addAll(_Resources.get()
                .iabProductPurchase.getPendingTransactions());

        pendingTransactionsList.addAll(_Resources.get()
                .iabSubscriptionPurchase.getPendingTransactions());

        for (_IABTransactionData transaction : pendingTransactionsList) {

            if (transaction.state == PURCHASE_STATE_PENDING) {

                verifyIABTransaction(transaction);
            }

            if (transaction.state == PURCHASE_STATE_VERIFIED) {

                consumeIABTransaction(transaction);
            }
        }
    }

    /*public DataClass processIABTransaction(DataClass data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.iabTransaction == null ||
                data.iabTransaction.market == null ||
                data.iabTransaction.sku == null ||
                data.iabTransaction.token == null) {

            data.message = Messages.get("GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        data.iabTransaction.state = PURCHASE_STATE_PENDING;

        IABTransactionData iabTransaction = (IABTransactionData) data.iabTransaction;

        String market = iabTransaction.market;
        String token = iabTransaction.token;
        String sku = iabTransaction.sku;
        String language = data.client.language;
        int userID = data.account.id;

        BaseProductData originalProduct = BaseResources.getInstance().productManager.getProduct(market, sku);

        if (originalProduct == null) {

            return data;
        }

        data.product = originalProduct;

        iabTransaction.userID = userID;
        String category = originalProduct.category;

        IABTransactionData storedTransaction;

        if (BaseProductsManager.CATEGORY_PRODUCT.equals(category)) {

            BaseIABProductPurchasesManager<IABTransactionData> productTransactionsManager =
                    BaseResources.getInstance().iabProductPurchasesManager;

            storedTransaction = productTransactionsManager.getIABTransaction(market, token);

            if (storedTransaction == null) {

                int transactionID = productTransactionsManager.addIABTransaction(iabTransaction);

                if (transactionID == BaseCodes.NOT_FOUND) {

                    return data;
                }

                iabTransaction.id = transactionID;

            } else {

                iabTransaction = storedTransaction;
            }
        }

        if (BaseProductsManager.CATEGORY_SUBSCRIPTION.equals(category)) {

            BaseIABSubscriptionPurchasesManager<IABTransactionData> subscriptionTransactionsManager =
                    BaseResources.getInstance().iabSubscriptionPurchasesManager;

            storedTransaction = subscriptionTransactionsManager.getIABTransaction(market, token);

            if (storedTransaction == null) {

                int transactionID = subscriptionTransactionsManager.addIABTransaction(iabTransaction);

                if (transactionID == BaseCodes.NOT_FOUND) {

                    return data;
                }

                iabTransaction.id = transactionID;

            } else {

                iabTransaction = storedTransaction;
            }
        }

        if (iabTransaction.state == PURCHASE_STATE_PENDING) {

            Boolean verified = verifyIABTransaction(iabTransaction);

            if (verified == null) {

                data.message = Messages.get("GENERAL", Messages.General.SERVERS_ARE_NOT_AVAILABLE, language);

                return data;
            }

            if (BaseConfig.MARKET_APP_STORE.equals(market)) {

                data.message = "Your purchase was successful! Thank you for your purchase.";
            }
        }

        if (iabTransaction.state == PURCHASE_STATE_VERIFIED) {

            *//*if (BaseProductsManager.CATEGORY_SUBSCRIPTION.equals(category)) {

                BaseResources.getInstance().subscriptionTransactionsManager
                        .updateExpirationDate(iabTransaction.id, iabTransaction.expirationDate);
            }*//*

            data.response = BaseCodes.RESPONSE_OK;

            consumeIABTransaction(iabTransaction);
        }

        if (iabTransaction.state == PURCHASE_STATE_REJECTED) {

            data.response = BaseCodes.RESPONSE_NOK;

            data.message = Messages.get("GENERAL", Messages.General.PAYMENT_INFO_IS_NOT_VALID, data.client.language);
        }

        if (iabTransaction.state == PURCHASE_STATE_APPLIED) {

            data.response = BaseCodes.RESPONSE_OK;
        }

        return data;
    }*/

    public Boolean verifyIABTransaction(_IABTransactionData transaction) {

        String sku = transaction.sku;
        String purchaseToken = transaction.token;
        String market = transaction.market;
        String appName = transaction.appName;
        String packageName = DynamicConfig.getMap("PackageName", appName + "-" + market);

        ProductData originalProduct = (ProductData) _Resources.get().product.getProduct(market, sku);

        if (originalProduct == null) {

            return null;
        }

        String productCategory = originalProduct.category;

        String accessToken = ACCESS_TOKENS_MAP.get(market);
        Map<String, String> headers = new HashMap<>();
        String apiUrl = null;

        AtomicReference<Boolean> verified = new AtomicReference<>();

        if (_Config.MARKET_APP_STORE.equals(market)) {

            verified.set(true);

            if (transaction.state == PURCHASE_STATE_PENDING) {

                transaction.state = PURCHASE_STATE_VERIFIED;
            }

            transaction.purchaseDate = System.currentTimeMillis();

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                //transaction.expirationDate = marketData.expiryTimeMillis;
            }
        }

        if (_Config.MARKET_CAFEBAZAAR.equals(market)) {

            if (_Product.CATEGORY_PRODUCT.equals(productCategory)) {

                apiUrl = CAFEBAZAAR_API_URL + "api/validate/" + packageName + "/inapp/" +
                        sku + "/purchases/" + purchaseToken + "?access_token=" + accessToken;
            }

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                apiUrl = CAFEBAZAAR_API_URL + "api/applications/" + packageName + "/subscriptions/" +
                        sku + "/purchases/" + purchaseToken + "?access_token=" + accessToken;
            }

            Utilities.openConnection(apiUrl, null, (responseCode, responseMessage) -> {

                //System.out.println(responseCode + " : " + responseMessage + "\n\n");

                if (responseCode == 200) {

                    CafeBazaarClass marketData = Json.getObject(responseMessage, CafeBazaarClass.class);

                    if (marketData == null) {

                        return;
                    }

                    if (CAFEBAZAAR_PRODUCT.equals(marketData.kind) || CAFEBAZAAR_SUBSCRIPTION.equals(marketData.kind)) {

                        transaction.purchaseDate = marketData.initiationTimestampMsec;
                        transaction.expirationDate = marketData.validUntilTimestampMsec;

                        verified.set(true);

                        if (transaction.state == PURCHASE_STATE_PENDING) {

                            transaction.state = PURCHASE_STATE_VERIFIED;
                        }
                    }

                    if ("not_found".equals(marketData.error)) {

                        verified.set(false);
                        transaction.state = PURCHASE_STATE_REJECTED;
                        transaction.expirationDate = transaction.purchaseDate;
                    }
                }

                if (responseCode == 404) {

                    verified.set(false);
                    transaction.state = PURCHASE_STATE_REJECTED;
                    transaction.expirationDate = transaction.purchaseDate;
                }

            }, false);
        }

        if (_Config.MARKET_MYKET.equals(market)) {

            if (_Product.CATEGORY_PRODUCT.equals(productCategory)) {

                apiUrl = MYKET_API_URL + packageName + "/purchases" +
                        "/products/" + sku + "/tokens/" + purchaseToken;
            }

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                apiUrl = MYKET_API_URL + packageName + "/purchases" +
                        "/subscription/" + sku + "/tokens/" + purchaseToken;
            }

            headers.put("X-Access-Token", "3bea2378-2df0-455c-b573-b24d1aec0315");

            Utilities.openConnection(apiUrl, headers, (responseCode, responseMessage) -> {

                if (responseCode == 200) {

                    MarketData marketData = Json.getObject(responseMessage, MarketData.class);

                    if (marketData != null) {

                        if (MYKET_PRODUCT_OR_SUBSCRIPTION.equals(marketData.kind)) {

                            verified.set(true);

                            if (transaction.state == PURCHASE_STATE_PENDING) {

                                transaction.state = PURCHASE_STATE_VERIFIED;
                            }

                            transaction.purchaseDate = marketData.startTimeMillis;

                            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                                transaction.expirationDate = marketData.expiryTimeMillis;
                            }
                        }
                    }
                }

                if (responseCode == 404 || responseCode == 403) {

                    verified.set(false);
                    transaction.state = PURCHASE_STATE_REJECTED;
                    transaction.expirationDate = transaction.purchaseDate;
                }

            }, false);
        }

        if (_Config.MARKET_GOOGLE_PLAY.equals(market)) {

            if (_Product.CATEGORY_PRODUCT.equals(productCategory)) {

                apiUrl = GOOGLEPLAY_API_URL + packageName + "/purchases" +
                        "/products/" + sku + "/tokens/" + purchaseToken + "?access_token=" + accessToken;
            }

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                apiUrl = GOOGLEPLAY_API_URL + packageName + "/purchases" +
                        "/subscriptions/" + sku + "/tokens/" + purchaseToken + "?access_token=" + accessToken;
            }

            System.out.println(apiUrl);

            Utilities.openConnection(apiUrl, null, (responseCode, responseMessage) -> {

                System.out.println(responseCode + " : " + responseMessage);

                if (responseCode == 200) {

                    MarketData marketData = Json.getObject(responseMessage, MarketData.class);

                    if (marketData != null) {

                        if (GOOGLEPLAY_PRODUCT.equals(marketData.kind) || GOOGLEPLAY_SUBSCRIPTION.equals(marketData.kind)) {

                            verified.set(true);

                            if (transaction.state == PURCHASE_STATE_PENDING) {

                                transaction.state = PURCHASE_STATE_VERIFIED;
                            }

                            if (GOOGLEPLAY_PRODUCT.equals(marketData.kind)) {

                                transaction.purchaseDate = marketData.purchaseTimeMillis;
                            }

                            if (GOOGLEPLAY_SUBSCRIPTION.equals(marketData.kind)) {

                                transaction.purchaseDate = marketData.startTimeMillis;
                            }

                            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                                transaction.expirationDate = marketData.expiryTimeMillis;
                            }
                        }
                    }
                }

                if (responseCode == 400 || responseCode == 404) {

                    verified.set(false);
                    transaction.state = PURCHASE_STATE_REJECTED;
                    transaction.expirationDate = transaction.purchaseDate;
                }

            }, true);
        }

        if (_Config.MARKET_ROYAL.equals(market)) {

            if (_Product.CATEGORY_PRODUCT.equals(productCategory)) {

                apiUrl = JHOOBIN_API_URL + packageName + "/purchases/products/" + sku +
                        "/tokens/" + purchaseToken + "?access_token=" + JHOOBIN_ACCESS_TOKEN;
            }

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                apiUrl = JHOOBIN_API_URL + packageName + "/purchases/subscriptions/" + sku +
                        "/tokens/" + purchaseToken + "?access_token=" + JHOOBIN_ACCESS_TOKEN;
            }

            Utilities.openConnection(apiUrl, null, (responseCode, responseMessage) -> {

                if (responseCode == 200) {

                    JhoobinClass marketData = Json.getObject(responseMessage, JhoobinClass.class);

                    if (marketData == null) {

                        return;
                    }

                    if (JHOOBIN_PRODUCT.equals(marketData.kind) || JHOOBIN_SUBSCRIPTION.equals(marketData.kind)) {

                        verified.set(true);

                        if (transaction.state == PURCHASE_STATE_PENDING) {

                            transaction.state = PURCHASE_STATE_VERIFIED;
                        }

                        transaction.purchaseDate = marketData.startTimeMillis;
                        transaction.expirationDate = marketData.expiryTimeMillis;
                    }
                }

                /*if (responseCode >= 400 && responseCode < 500) {

                    verified.set(false);
                    transaction.state = PURCHASE_STATE_REJECTED;
                    transaction.expirationDate = transaction.purchaseDate;
                }*/

            }, false);
        }

        if (_Config.MARKET_IRANAPPS.equals(market)) {

            verified.set(true);

            if (transaction.state == PURCHASE_STATE_PENDING) {

                transaction.state = PURCHASE_STATE_VERIFIED;
            }

            transaction.purchaseDate = System.currentTimeMillis();
        }

        if (verified.get() != null) {

            boolean wasSuccessful = false;

            //if (transaction.state == PURCHASE_STATE_VERIFIED || transaction.state == PURCHASE_STATE_REJECTED) {

            if (_Product.CATEGORY_PRODUCT.equals(productCategory)) {

                wasSuccessful = _Resources.get().iabProductPurchase
                        .updateTransactionState(null, transaction.id, transaction.state);
            }

            if (_Product.CATEGORY_SUBSCRIPTION.equals(productCategory)) {

                wasSuccessful = _Resources.get().iabSubscriptionPurchase
                        .updateTransactionState(null, transaction.id, transaction.state,
                                transaction.purchaseDate, transaction.expirationDate);
            }

            //} else {

            wasSuccessful = true;
            //}

            return verified.get();

            /*if (wasSuccessful) {

                return verified.get();
            }*/
        }

        return null;
    }

    protected void consumeIABTransaction(_IABTransactionData transaction) {

        new Thread(() -> {

            if (transaction == null) {

                return;
            }

            synchronized (transaction.userID + "") {

                try {

                    if (transaction.state != PURCHASE_STATE_VERIFIED) {

                        return;
                    }

                    String market = transaction.market;
                    String sku = transaction.sku;

                    ProductData originalProduct = (ProductData) _Resources.get()
                            .product.getProduct(market, sku);

                    if (originalProduct == null) {

                        return;
                    }

                    Connection connection = _DBConnectionOld.connect(_DBConnectionOld.MANUAL_COMMIT);

                    boolean wasSuccessful = onConsuming(connection, transaction, originalProduct);

                    if (_Product.CATEGORY_PRODUCT.equals(originalProduct.category)) {

                        wasSuccessful &= _Resources.get().iabProductPurchase
                                .updateTransactionState(connection, transaction.id, PURCHASE_STATE_APPLIED);
                    }

                    if (_Product.CATEGORY_SUBSCRIPTION.equals(originalProduct.category)) {

                        wasSuccessful &= _Resources.get().iabSubscriptionPurchase
                                .updateTransactionState(connection, transaction.id, PURCHASE_STATE_APPLIED,
                                        transaction.purchaseDate, transaction.expirationDate);
                    }

                    if (wasSuccessful) {

                        _DBConnectionOld.commit(connection);

                        transaction.state = PURCHASE_STATE_APPLIED;

                    } else {

                        _DBConnectionOld.rollback(connection);
                    }

                    _DBConnectionOld.close(connection);

                    if (wasSuccessful) {

                        onConsumeFinished(transaction, originalProduct);
                    }

                } catch (Exception e) {

                    _Log.error(TAG, e);
                }
            }

        }).start();
    }

    /*public boolean onConsuming(Connection connection, IABTransactionData transaction, ProductData product) {

        return false;
    }

    public void onConsumeFinished(IABTransactionData transaction, ProductData product) {

    }*/

    /*public DataClass cancelSubscription(DataClass data) {

        data.response = BaseCodes.RESPONSE_NOK;

        int userID = data.account.id;
        String appName = data.client.appName;

        BaseIABTransactionData iabTransaction = data.iabTransaction;

        String market = iabTransaction.market;
        String token = iabTransaction.token;
        String sku = iabTransaction.sku;

        BaseIABTransactionData storedTransaction = BaseResources.getInstance()
                .iabSubscriptionPurchasesManager.getIABTransaction(market, token);

        if (storedTransaction != null) {

            Boolean wasSuccessful = BaseResources.getInstance()
                    .iabSubscriptionPurchasesManager.cancelIABTransaction(storedTransaction);

            if (wasSuccessful != null) {

                if (wasSuccessful) {

                    data.response = BaseCodes.RESPONSE_OK;
                    data.message = null;

                    //TODO : FIX LANGUAGE

                    String notificationLanguage = "FA";

                    String notificationMessage = Messages.get("GENERAL",
                            Messages.General.SUBSCRIPTION_CANCELED, notificationLanguage);

                    BaseNotificationManager.sendNotification(userID, appName, BaseConfig.APP_NAME_CAFE_GAME, notificationMessage);

                    for (String gamerTag : BaseConfig.ADMINS) {

                        BaseNotificationManager.sendNotification(gamerTag, BaseConfig.APP_NAME_CAFE_GAME, "Subscription Cancellation",
                                "User " + userID + " canceled subscription : " + sku);
                    }

                } else {

                    data.response = BaseCodes.RESPONSE_NOK;
                    data.message = Messages.get("GENERAL", Messages.General.PAYMENT_INFO_IS_NOT_VALID, data.client.language);
                }
            }
        }

        return data;
    }*/
}