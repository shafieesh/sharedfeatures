package com.chainedminds.models.payment;

public class BaseIABTransactionData extends BaseTransactionData {

    public String sku;
    public String token;
    public String payload;
    public int state;
    public long expirationDate;
}