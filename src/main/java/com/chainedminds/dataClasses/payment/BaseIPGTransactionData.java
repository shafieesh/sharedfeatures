package com.chainedminds.dataClasses.payment;

import java.util.Map;

public class BaseIPGTransactionData extends BaseTransactionData {

    public String type;
    public String sku;
    public String gateway;
    public String orderID;
    public String transactionID;
    public String payload;
    public int state;
    public long expirationDate;
    public int amount;
    public String paymentLink;
    public Map<String, String> arbitraryData;
    public String redirectAddress;
}