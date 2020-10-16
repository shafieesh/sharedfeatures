package com.chainedminds.dataClasses.payment;

public class BaseIABTransactionData extends BaseTransactionData {

    public int id;
    public int userID;
    public String appName;
    public String market;

    public String sku;
    public String token;
    public String payload;
    public int state;
    public long purchaseDate;
    public long expirationDate;
}