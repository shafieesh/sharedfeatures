package com.chainedminds.api.store;

import com.chainedminds.BaseClasses;
import com.chainedminds.BaseCodes;
import com.chainedminds.BaseConfig;
import com.chainedminds.BaseResources;
import com.chainedminds.dataClasses.BaseData;
import com.chainedminds.dataClasses.BaseProductData;
import com.chainedminds.dataClasses.payment.BaseIPGTransactionData;
import com.chainedminds.dataClasses.payment.PasargadData;
import com.chainedminds.dataClasses.payment.ZarinPalData;
import com.chainedminds.utilities.*;
import com.chainedminds.utilities.database.BaseDatabaseHelperOld;
import com.chainedminds.utilities.json.JsonHelper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class BaseIPGPaymentManager<Data extends BaseData,
        IPGTransactionData extends BaseIPGTransactionData,
        ProductData extends BaseProductData> extends BasePaymentManager<ProductData> {

    private static final String TAG = BaseIPGPaymentManager.class.getSimpleName();

    private static final Map<String, String> ACCESS_TOKENS_MAP = new HashMap<>();

    //-------------------------------------------------------------//

    public static final String ZARINPAL_SERVICES_LINK = "https://www.zarinpal.com/pg/services/WebGate/service";
    public static final String ZARINPAL_METHOD_PAYMENT_REQUEST = "PaymentRequest";
    public static final String ZARINPAL_METHOD_PAYMENT_VERIFICATION = "PaymentVerification";
    public static final String ZARINPAL_METHODS_XMLNS = "http://zarinpal.com/";

    //-------------------------------------------------------------//

    public static final String FIELD_ID = "ID";
    public static final String FIELD_USER_ID = "UserID";
    public static final String FIELD_APP_NAME = "AppName";
    public static final String FIELD_MARKET = "Market";
    public static final String FIELD_SKU = "SKU";
    public static final String FIELD_GATEWAY = "Gateway";
    public static final String FIELD_ORDER_ID = "OrderID";
    public static final String FIELD_REFERENCE_ID = "ReferenceID";
    public static final String FIELD_TRANSACTION_ID = "TransactionID";
    public static final String FIELD_PAYLOAD = "Payload";
    public static final String FIELD_STATE = "State";
    public static final String FIELD_DATETIME = "DateTime";
    public static final String FIELD_AMOUNT = "Amount";

    public static final String FIELD_ZARINPAL_AUTHORITY = "Authority";

    public static final String FIELD_PASARGAD_INVOICE_NUMBER = "InvoiceNumber";
    public static final String FIELD_PASARGAD_INVOICE_DATE = "InvoiceDate";

    public Data preparePayment(Data data) {

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.ipgTransaction == null || data.ipgTransaction.sku == null) {

            data.message = Messages.get("SYSTEM_GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        int userID = data.account.id;
        String appName = data.client.appName;
        String market = data.client.market;
        String sku = data.ipgTransaction.sku;

        BaseProductData originalProduct = BaseResources.getInstance().productManager.getProduct(market, sku);

        if (originalProduct == null) {

            return data;
        }

        int price = (int) originalProduct.price;

        BaseIPGTransactionData transaction = BaseClasses.construct(
                BaseClasses.getInstance().ipgTransactionClass);

        transaction.orderID = System.currentTimeMillis() + "";
        transaction.userID = userID;
        transaction.appName = appName;
        transaction.market = market;
        transaction.sku = sku;
        transaction.amount = price;
        transaction.redirectAddress = "https://payment.fandoghapps.com/verify/" + transaction.orderID + "/";

        if ("CafeGame".equals(appName)) {

            transaction.redirectAddress = "https://payment.fandoghapps.com/verify/" + transaction.orderID + "/";
        }

        if ("SymoTeb".equals(appName)) {

            transaction.redirectAddress = "https://payment.symoteb.ir/verify/" + transaction.orderID + "/";
        }

        transaction = generatePaymentLink(appName, transaction);

        if (transaction.gateway != null) {

            boolean wasSuccessful = addTransaction(transaction);

            if (wasSuccessful) {

                data.ipgTransaction = transaction;
                data.response = BaseCodes.RESPONSE_OK;
            }
        }

        return data;
    }

    public BaseIPGTransactionData prepareOrderPayment(IPGTransactionData ipgTransaction) {

        if (ipgTransaction == null || ipgTransaction.price == 0) {

            return null;
        }

        int userID = ipgTransaction.userID;
        String appName = ipgTransaction.appName;
        String market = ipgTransaction.market;
        String sku = ipgTransaction.sku;
        int price = (int)ipgTransaction.price;

        BaseIPGTransactionData transaction = BaseClasses.construct(
                BaseClasses.getInstance().ipgTransactionClass);

        transaction.orderID = System.currentTimeMillis() + "";
        transaction.userID = userID;
        transaction.appName = appName;
        transaction.market = market;
        transaction.sku = sku;
        transaction.amount = price;
        transaction.redirectAddress = "https://payment.fandoghapps.com/verify/" + transaction.orderID + "/";

        if ("CafeGame".equals(appName)) {

            transaction.redirectAddress = "https://payment.fandoghapps.com/verify/" + transaction.orderID + "/";
        }

        if ("VRCT".equals(appName)) {

            transaction.redirectAddress = "https://payment.vrct.ir/verifyorders/" + transaction.orderID + "/";
        }

        if ("SymoTeb".equals(appName)) {

            transaction.redirectAddress = "https://payment.symoteb.ir/verify/" + transaction.orderID + "/";
        }

        transaction = generatePaymentLink(appName, transaction);

        if (transaction.gateway != null) {

            boolean wasSuccessful = addTransaction(transaction);

            if (wasSuccessful) {

                return transaction;
            }
        }

        return null;
    }

    public Data verifyPayment(Data data) {

        System.out.println(JsonHelper.getString(data));

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.ipgTransaction == null) {

            data.message = Messages.get("SYSTEM_GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String orderID = data.ipgTransaction.orderID;

        Map<String, String> arbitraryData = data.ipgTransaction.arbitraryData;

        System.out.println("GETTING ORDER ID : " + orderID);

        IPGTransactionData transaction = getTransaction(orderID);

        if (transaction == null) {

            data.message = "The transaction id was not found";
            return data;
        }

        transaction.arbitraryData.putAll(arbitraryData);

        System.out.println("VERIFYING TRANSACTION : " + JsonHelper.getString(transaction));

        boolean verified = false;

        String gateway = transaction.gateway;

        switch (gateway) {

            case "Pasargad":

                verified = verifyPasargadPayment(transaction);
                break;

            case "ZarinPal":

                verified = verifyZarinPalPayment(transaction);
                break;
        }

        if (verified) {

            String market = transaction.market;
            String sku = transaction.sku;

            ProductData originalProduct = (ProductData) BaseResources.getInstance()
                    .productManager.getProduct(market, sku);

            if (originalProduct == null) {

                return data;
            }

            Connection connection = BaseConnectionManagerOld.getConnection(BaseConnectionManagerOld.MANUAL_COMMIT);

            boolean wasSuccessful = onConsuming(connection, transaction, originalProduct);

            wasSuccessful &= updatePurchaseState(connection, transaction.id, PURCHASE_STATE_APPLIED);

            if (wasSuccessful) {

                BaseConnectionManagerOld.commit(connection);

                transaction.state = PURCHASE_STATE_APPLIED;

            } else {

                BaseConnectionManagerOld.rollback(connection);
            }

            BaseConnectionManagerOld.close(connection);

            if (wasSuccessful) {

                onConsumeFinished(transaction, originalProduct);
            }
            data.response = BaseCodes.RESPONSE_OK;
            data.message = "The transaction needs to be updated";

        } else {

            data.response = BaseCodes.RESPONSE_NOK;
        }

        System.out.println(JsonHelper.getString(data));

        return data;
    }

    /*public Data verifyOrderPayment(Data data) {

        System.out.println(JsonHelper.getString(data));

        data.response = BaseCodes.RESPONSE_NOK;

        if (data.ipgTransaction == null) {

            data.message = Messages.get("SYSTEM_GENERAL", Messages.General.MISSING_DATA, data.client.language);

            return data;
        }

        String orderID = data.ipgTransaction.orderID;

        Map<String, String> arbitraryData = data.ipgTransaction.arbitraryData;

        System.out.println("GETTING ORDER ID : " + orderID);

        IPGTransactionData transaction = getTransaction(orderID);

        if (transaction == null) {

            data.message = "The transaction id was not found";
            return data;
        }

        transaction.arbitraryData.putAll(arbitraryData);

        System.out.println("VERIFYING TRANSACTION : " + JsonHelper.getString(transaction));

        boolean verified = false;

        String gateway = transaction.gateway;

        switch (gateway) {

            case "Pasargad":

                verified = verifyPasargadPayment(transaction);
                break;

            case "ZarinPal":

                verified = verifyZarinPalPayment(transaction);
                break;
        }

        if (verified) {

            String sku = transaction.sku;

            Connection connection = ConnectionManager.getConnection(ConnectionManager.MANUAL_COMMIT);

            boolean wasSuccessful = VRStoreManager.applyPurchasedOrder(connection, sku);

            wasSuccessful &= updatePurchaseState(connection, transaction.id, PURCHASE_STATE_APPLIED);

            if (wasSuccessful) {

                ConnectionManager.commit(connection);

                transaction.state = PURCHASE_STATE_APPLIED;

            } else {

                ConnectionManager.rollback(connection);
            }

            ConnectionManager.close(connection);

            data.response = BaseCodes.RESPONSE_OK;
            data.message = "The transaction needs to be updated";

        } else {

            data.response = BaseCodes.RESPONSE_NOK;
        }

        System.out.println(JsonHelper.getString(data));

        return data;
    }*/

    public boolean verifyZarinPalPayment(IPGTransactionData transaction) {

        String url = "https://www.zarinpal.com/pg/rest/WebGate/PaymentVerification.json";

        ZarinPalData zarinPal = new ZarinPalData();
        zarinPal.MerchantID = DynamicConfig.getMap("ZarinPal-MerchantID", transaction.appName);
        zarinPal.Authority = transaction.arbitraryData.get("Authority");
        zarinPal.Amount = transaction.amount;

        String jsonRequest = JsonHelper.getString(zarinPal);

        System.out.println("Request : " + jsonRequest);

        AtomicBoolean verified = new AtomicBoolean(false);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Utilities.openConnection(url, headers, "POST", jsonRequest, new HttpResponseCallback() {
            @Override
            public void onHttpResponse(int responseCode, String receivedMessage) {

                System.out.println("Response : " + responseCode + " : " + receivedMessage);

                if (responseCode == 200) {

                    ZarinPalData response = JsonHelper.getObject(receivedMessage, ZarinPalData.class);

                    if (response != null) {

                        verified.set(response.Status == 100 || response.Status == 101);
                    }
                }
            }
        });

        return verified.get();
    }

    public boolean verifyPasargadPayment(IPGTransactionData transaction) {

        System.out.println("START VERIFYING WITH PASARGAD");

        AtomicReference<Boolean> paymentSuccessful = new AtomicReference<>();

        String getTransactionResultUrl = "https://pep.shaparak.ir/Api/v1/Payment/CheckTransactionResult";

        PasargadData paymentData = new PasargadData();
        paymentData.TransactionReferenceID = transaction.arbitraryData.get("tref");

        String jsonPaymentData = JsonHelper.getString(paymentData);

        Utilities.openConnection(getTransactionResultUrl, null, "POST", jsonPaymentData, new HttpResponseCallback() {
            @Override
            public void onHttpResponse(int responseCode, String receivedMessage) {

                System.out.println(responseCode + " : " + receivedMessage);

                if (responseCode == 200) {

                    PasargadData checkTransactionResponse = JsonHelper.getObject(receivedMessage, PasargadData.class);

                    if (checkTransactionResponse != null) {

                        paymentSuccessful.set(checkTransactionResponse.IsSuccess);
                    }
                }
            }
        });

        if (!paymentSuccessful.get()) {

            System.out.println("FIRST VERIFICATION FAILED");

            return false;
        }

        System.out.println("FIRST VERIFICATION PASSED");

        AtomicReference<String> signedRequest = new AtomicReference<>();

        String getSignUrl = "https://pep.shaparak.ir/Api/v1/Payment/GetSign";

        Map<String, String> getSignHeaders = new HashMap<>();
        getSignHeaders.put("Pk", "<RSAKeyValue><Modulus>vROIrT3FQzkz15zXhMRTznSEd3HB+rXzI+sszyQE2pWXZ5rQVGNWGSCmgup/DD2n+y4Vj3nPHR/kBfSrU0IxFPLlieqs6YRsxfT+6aoh+byLGOHVthvg8qFo6PEEo0KlF4qMc+hw6JtFWUqLLYqcXZyN21hKRU8s3I85oQ7UIJU=</Modulus><Exponent>AQAB</Exponent><P>4N65paLGbxRR6Uzx+q0g4E+SszOz7wz1eP3wKdlO4HQM4EByglvwysG8edpFqCe/EgA5bt4oDyShbhgBfMVYUw==</P><Q>10BMXgjVlllrCSyTur7Z4SCTAIQeBIG548Sl6lIgLNW2GB4vAx53TBhdiUCSuIuddP4/xgVwTip52wfAUZ1mdw==</Q><DP>i/0794iZHge5YpL7RYJIKDyBaDw0tQoNOzBjNbpJ52N3rJeSceMIyUDIs9cVbDSqN2uaVZHJwHueX6pkKqe0bw==</DP><DQ>GcGl4bKo7B6zAhwFaVkg9SFStcMZXGyxc6G6QLmnA/Zmnagu8+6XU9Guj5fcdpyuMKujsexArfnDLwRUbO+qrQ==</DQ><InverseQ>vWrZmf65jeAHXYkWI80Mf0CdMIuxvCVG7bJOwncL/CPTbseMP07mlH2ts4eGuTMkCAUkW7xHS76pGcOLaB9Y8A==</InverseQ><D>EPPTaVUEfsZ+M3LIkAraSMSBrAPzFHuD1VCcr+XW2ZJYBN4Il8hgS9h1lX9Y7MwKZ6b2H+sX+tCFLVOGmOxyYfr/GYx4vW4Jh9FGDHRAbSAuhmc4HuDOn2FkWEGc5+VwzuBcY2SkHVnCE+Zf2ALt9T7toCr0ZcU50L5sS2wHvnE=</D></RSAKeyValue>");

        paymentData.InvoiceNumber = transaction.arbitraryData.get(FIELD_PASARGAD_INVOICE_NUMBER);
        paymentData.InvoiceDate = transaction.arbitraryData.get(FIELD_PASARGAD_INVOICE_DATE);
        paymentData.Amount = transaction.amount;

        jsonPaymentData = JsonHelper.getString(paymentData);

        System.out.println(jsonPaymentData);

        Utilities.openConnection(getSignUrl, getSignHeaders, "POST",
                jsonPaymentData, (responseCode, receivedMessage) -> {

            System.out.println(responseCode + " : " + receivedMessage);

            if (responseCode == 200) {

                PasargadData signResponse = JsonHelper.getObject(receivedMessage, PasargadData.class);

                if (signResponse != null && signResponse.IsSuccess) {

                    signedRequest.set(signResponse.Sign);
                }
            }
        });

        if (signedRequest.get() == null) {

            return false;
        }

        AtomicReference<Boolean> paymentVerification = new AtomicReference<>();

        String getTokenUrl = "https://pep.shaparak.ir/Api/v1/Payment/VerifyPayment";

        Map<String, String> getTokenHeaders = new HashMap<>();
        getTokenHeaders.put("Sign", signedRequest.get());

        Utilities.openConnection(getTokenUrl, getTokenHeaders, "POST", jsonPaymentData, new HttpResponseCallback() {
            @Override
            public void onHttpResponse(int responseCode, String receivedMessage) {

                System.out.println(responseCode + " : " + receivedMessage);

                if (responseCode == 200) {

                    PasargadData verificationResponse = JsonHelper.getObject(receivedMessage, PasargadData.class);

                    if (verificationResponse != null) {

                        paymentVerification.set(verificationResponse.IsSuccess);
                    }
                }
            }
        });

        return paymentVerification.get();
    }

    public BaseIPGTransactionData generatePaymentLink(String appName, BaseIPGTransactionData transaction) {

        if ("CafeGame".equals(appName)) {

            if (transaction.gateway == null) {

                transaction = generateZarinPalPaymentLink(transaction);
            }
        }

        if ("VRCT".equals(appName)) {

            if (transaction.gateway == null) {

                transaction = generateZarinPalPaymentLink(transaction);
            }
        }

        if ("SymoTeb".equals(appName)) {

            if (transaction.gateway == null) {

                transaction = generatePasargadPaymentLink(transaction);
            }
        }

        return transaction;
    }

    public BaseIPGTransactionData generatePasargadPaymentLink(BaseIPGTransactionData transaction) {

        AtomicReference<String> signedRequest = new AtomicReference<>();

        String getSignUrl = "https://pep.shaparak.ir/Api/v1/Payment/GetSign";

        Map<String, String> getSignHeaders = new HashMap<>();
        getSignHeaders.put("Pk", "<RSAKeyValue><Modulus>vROIrT3FQzkz15zXhMRTznSEd3HB+rXzI+sszyQE2pWXZ5rQVGNWGSCmgup/DD2n+y4Vj3nPHR/kBfSrU0IxFPLlieqs6YRsxfT+6aoh+byLGOHVthvg8qFo6PEEo0KlF4qMc+hw6JtFWUqLLYqcXZyN21hKRU8s3I85oQ7UIJU=</Modulus><Exponent>AQAB</Exponent><P>4N65paLGbxRR6Uzx+q0g4E+SszOz7wz1eP3wKdlO4HQM4EByglvwysG8edpFqCe/EgA5bt4oDyShbhgBfMVYUw==</P><Q>10BMXgjVlllrCSyTur7Z4SCTAIQeBIG548Sl6lIgLNW2GB4vAx53TBhdiUCSuIuddP4/xgVwTip52wfAUZ1mdw==</Q><DP>i/0794iZHge5YpL7RYJIKDyBaDw0tQoNOzBjNbpJ52N3rJeSceMIyUDIs9cVbDSqN2uaVZHJwHueX6pkKqe0bw==</DP><DQ>GcGl4bKo7B6zAhwFaVkg9SFStcMZXGyxc6G6QLmnA/Zmnagu8+6XU9Guj5fcdpyuMKujsexArfnDLwRUbO+qrQ==</DQ><InverseQ>vWrZmf65jeAHXYkWI80Mf0CdMIuxvCVG7bJOwncL/CPTbseMP07mlH2ts4eGuTMkCAUkW7xHS76pGcOLaB9Y8A==</InverseQ><D>EPPTaVUEfsZ+M3LIkAraSMSBrAPzFHuD1VCcr+XW2ZJYBN4Il8hgS9h1lX9Y7MwKZ6b2H+sX+tCFLVOGmOxyYfr/GYx4vW4Jh9FGDHRAbSAuhmc4HuDOn2FkWEGc5+VwzuBcY2SkHVnCE+Zf2ALt9T7toCr0ZcU50L5sS2wHvnE=</D></RSAKeyValue>");

        PasargadData paymentData = new PasargadData();
        paymentData.InvoiceNumber = "" + System.currentTimeMillis();
        paymentData.Amount = transaction.amount * 10;
        paymentData.RedirectAddress = transaction.redirectAddress;

        String jsonPaymentData = JsonHelper.getString(paymentData);

        //System.out.println(jsonPaymentData);

        Utilities.openConnection(getSignUrl, getSignHeaders, "POST",
                jsonPaymentData, (responseCode, receivedMessage) -> {

                    //System.out.println(responseCode + " : " + receivedMessage);

                    if (responseCode == 200) {

                        PasargadData signResponse = JsonHelper.getObject(receivedMessage, PasargadData.class);

                        if (signResponse != null && signResponse.IsSuccess) {

                            signedRequest.set(signResponse.Sign);
                        }
                    }
                });

        if (signedRequest.get() == null) {

            return transaction;
        }

        AtomicReference<String> paymentToken = new AtomicReference<>();

        String getTokenUrl = "https://pep.shaparak.ir/Api/v1/Payment/GetToken";

        Map<String, String> getTokenHeaders = new HashMap<>();
        getTokenHeaders.put("Sign", signedRequest.get());

        Utilities.openConnection(getTokenUrl, getTokenHeaders, "POST",
                jsonPaymentData, (responseCode, receivedMessage) -> {

                    //System.out.println(responseCode + " : " + receivedMessage);

                    if (responseCode == 200) {

                        PasargadData tokenResponse = JsonHelper.getObject(receivedMessage, PasargadData.class);

                        if (tokenResponse != null && tokenResponse.IsSuccess) {

                            transaction.gateway = "Pasargad";
                            transaction.paymentLink = "https://pep.shaparak.ir/payment.aspx?n=" + tokenResponse.Token;
                            transaction.arbitraryData = new HashMap<>();
                            transaction.arbitraryData.put(FIELD_PASARGAD_INVOICE_NUMBER, paymentData.InvoiceNumber);
                            transaction.arbitraryData.put(FIELD_PASARGAD_INVOICE_DATE, paymentData.InvoiceDate);
                        }
                    }
                });

        return transaction;
    }

    public static BaseIPGTransactionData generateZarinPalPaymentLink(BaseIPGTransactionData transaction) {

        String url = "https://www.zarinpal.com/pg/rest/WebGate/PaymentRequest.json";

        ZarinPalData zarinPal = new ZarinPalData();
        zarinPal.MerchantID = DynamicConfig.getMap("ZarinPal-MerchantID", transaction.appName);
        zarinPal.Amount = transaction.amount;
        zarinPal.Description = "خرید درون برنامه\u200Cای";
        zarinPal.CallbackURL = transaction.redirectAddress;

        String jsonRequest = JsonHelper.getString(zarinPal);

        System.out.println("Request : " + jsonRequest);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Utilities.openConnection(url, headers, "POST", jsonRequest, new HttpResponseCallback() {
            @Override
            public void onHttpResponse(int responseCode, String receivedMessage) {

                System.out.println("Response : " + responseCode + " : " + receivedMessage);

                if (responseCode == 200) {

                    ZarinPalData response = JsonHelper.getObject(receivedMessage, ZarinPalData.class);

                    if (response != null) {

                        transaction.gateway = "ZarinPal";
                        transaction.paymentLink = "https://www.zarinpal.com/pg/StartPay/" + response.Authority + "/ZarinGate";
                        transaction.arbitraryData = new HashMap<>();
                        transaction.arbitraryData.put(FIELD_ZARINPAL_AUTHORITY, response.Authority);
                    }
                }
            }
        });

        return transaction;
    }

    public boolean addTransaction(BaseIPGTransactionData transaction) {

        int userID = transaction.userID;
        String appName = transaction.appName;
        String market = transaction.market;
        String sku = transaction.sku;
        int amount = transaction.amount;

        String orderID = transaction.orderID;
        String gateway = transaction.gateway;

        int transactionID = addArbitraryData(gateway, transaction.arbitraryData);

        if (transactionID == BaseCodes.UNDEFINED) {

            return false;
        }

        String statement = "INSERT INTO " + BaseConfig.TABLE_PURCHASES_IPG + " (" +
                FIELD_ORDER_ID + ", " + FIELD_GATEWAY + ", " + FIELD_TRANSACTION_ID + ", " +
                FIELD_USER_ID + ", " + FIELD_APP_NAME + ", " + FIELD_MARKET + ", " +
                FIELD_SKU + ", " + FIELD_AMOUNT + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, orderID);
        parameters.put(2, gateway);
        parameters.put(3, transactionID);
        parameters.put(4, userID);
        parameters.put(5, appName);
        parameters.put(6, market);
        parameters.put(7, sku);
        parameters.put(8, amount);

        return BaseDatabaseHelperOld.insert(TAG, statement, parameters);
    }

    public int addArbitraryData(String gateway, Map<String, String> arbitraryData) {

        AtomicInteger arbitraryID = new AtomicInteger(BaseCodes.NOT_DEFINED);

        String table = "";
        String fields = "";
        String values = "";
        Map<Integer, Object> parameters = new HashMap<>();

        switch (gateway) {

            case "Pasargad":

                table = BaseConfig.TABLE_PURCHASES_IPG_PASARGAD;
                fields = FIELD_PASARGAD_INVOICE_NUMBER + ", " + FIELD_PASARGAD_INVOICE_DATE;
                values = "?, ?";
                parameters.put(1, arbitraryData.get(FIELD_PASARGAD_INVOICE_NUMBER));
                parameters.put(2, arbitraryData.get(FIELD_PASARGAD_INVOICE_DATE));
                break;

            case "ZarinPal":

                table = BaseConfig.TABLE_PURCHASES_IPG_ZARINPAL;
                fields = FIELD_ZARINPAL_AUTHORITY;
                values = "?";
                parameters.put(1, arbitraryData.get(FIELD_ZARINPAL_AUTHORITY));
                break;
        }

        String statement = "INSERT INTO " + table + " (" + fields + ") VALUES (" + values + ")";

        BaseDatabaseHelperOld.insert(TAG, statement, parameters, (wasSuccessful, generatedID, error) -> {

            if (wasSuccessful) {

                arbitraryID.set(generatedID);
            }
        });

        return arbitraryID.get();
    }

    public IPGTransactionData getTransaction(String orderID) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        Map<Integer, Object> parameters = new HashMap<>();

        String statement = "SELECT * FROM " + BaseConfig.TABLE_NAME_PURCHASES_IPG + " WHERE " + FIELD_ORDER_ID + " = ?";

        parameters.put(1, orderID);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                ipgTransactionHolder.set(ipgTransaction);
            }
        });

        int transactionID = ipgTransactionHolder.get().id;
        String gateway = ipgTransactionHolder.get().gateway;
        String table = "";

        switch (gateway) {

            case "Pasargad":

                table = BaseConfig.TABLE_PURCHASES_IPG_PASARGAD;
                break;

            case "ZarinPal":

                table = BaseConfig.TABLE_PURCHASES_IPG_ZARINPAL;
                break;
        }

        statement = "SELECT * FROM " + table + " WHERE " + FIELD_ID + " = ?";

        parameters.put(1, transactionID);

        BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                ipgTransactionHolder.get().arbitraryData = readArbitraryTransaction(gateway, resultSet);
            }
        });

        return ipgTransactionHolder.get();
    }

    public IPGTransactionData getTransaction(String gateway, Map<String, String> arbitraryData) {

        AtomicReference<IPGTransactionData> ipgTransactionHolder = new AtomicReference<>();

        String table = "";
        String condition = "";
        Map<Integer, Object> parameters = new HashMap<>();

        switch (gateway) {

            case "Pasargad":

                table = BaseConfig.TABLE_PURCHASES_IPG_PASARGAD;
                condition = FIELD_PASARGAD_INVOICE_NUMBER + " = ? AND " + FIELD_PASARGAD_INVOICE_DATE + " = ?";
                parameters.put(1, arbitraryData.get(FIELD_PASARGAD_INVOICE_NUMBER));
                parameters.put(2, arbitraryData.get(FIELD_PASARGAD_INVOICE_DATE));
                break;

            case "ZarinPal":

                table = BaseConfig.TABLE_PURCHASES_IPG_ZARINPAL;
                condition = FIELD_ZARINPAL_AUTHORITY + " = ?";
                parameters.put(1, arbitraryData.get(FIELD_ZARINPAL_AUTHORITY));
                break;
        }

        String statement = "SELECT * FROM " + BaseConfig.TABLE_NAME_PURCHASES_IPG + " AS T1" +
                " JOIN " + table + " AS T2 ON T1." + FIELD_TRANSACTION_ID + " = T2." + FIELD_ID +
                " WHERE " + condition;

        BaseDatabaseHelperOld.query(TAG, statement, parameters, resultSet -> {

            if (resultSet.next()) {

                IPGTransactionData ipgTransaction = readTransaction(resultSet);

                ipgTransactionHolder.set(ipgTransaction);
            }
        });

        return ipgTransactionHolder.get();
    }

    public boolean updatePurchaseState(Connection connection, int id, int state) {

        String updateStatement = "UPDATE " + BaseConfig.TABLE_PURCHASES_IPG_PRODUCTS +
                " SET " + FIELD_STATE + " = ? WHERE " + FIELD_ID + " = ?";

        Map<Integer, Object> parameters = new HashMap<>();

        parameters.put(1, state);
        parameters.put(2, id);

        if (connection != null) {

            return BaseDatabaseHelperOld.update(connection, TAG, updateStatement, parameters);

        } else {

            return BaseDatabaseHelperOld.update(TAG, updateStatement, parameters);
        }
    }

    private IPGTransactionData readTransaction(ResultSet resultSet) throws Exception {

        IPGTransactionData ipgTransaction = (IPGTransactionData) BaseClasses
                .construct(BaseClasses.getInstance().ipgTransactionClass);

        ipgTransaction.id = resultSet.getInt(FIELD_ID);
        ipgTransaction.gateway = resultSet.getString(FIELD_GATEWAY);
        ipgTransaction.transactionID = resultSet.getString(FIELD_TRANSACTION_ID);
        ipgTransaction.userID = resultSet.getInt(FIELD_USER_ID);
        ipgTransaction.appName = resultSet.getString(FIELD_APP_NAME);
        ipgTransaction.market = resultSet.getString(FIELD_MARKET);
        ipgTransaction.sku = resultSet.getString(FIELD_SKU);
        ipgTransaction.amount = resultSet.getInt(FIELD_AMOUNT);
        ipgTransaction.state = resultSet.getInt(FIELD_STATE);

        Timestamp purchaseDate = resultSet.getTimestamp(FIELD_DATETIME);

        if (purchaseDate != null) {

            ipgTransaction.purchaseDate = purchaseDate.getTime();
        }

        return ipgTransaction;
    }

    private Map<String, String> readArbitraryTransaction(String gateway, ResultSet resultSet) throws Exception {

        Map<String, String> arbitraryData = new HashMap<>();

        switch (gateway) {

            case "Pasargad":

                arbitraryData.put(FIELD_PASARGAD_INVOICE_NUMBER, resultSet.getString(FIELD_PASARGAD_INVOICE_NUMBER));
                arbitraryData.put(FIELD_PASARGAD_INVOICE_DATE, resultSet.getString(FIELD_PASARGAD_INVOICE_DATE));
                break;

            case "ZarinPal":

                arbitraryData.put(FIELD_ZARINPAL_AUTHORITY, resultSet.getString(FIELD_ZARINPAL_AUTHORITY));
                break;
        }

        return arbitraryData;
    }
}