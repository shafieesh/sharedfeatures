package com.chainedminds.dataClasses.market;

public class CafeBazaarClass {

    public String error;
    public String error_description;

    public String access_token;
    public String token_type;
    public int expires_in;
    public String refresh_token;
    public String scope;

    public int consumptionState;
    public int purchaseState;
    public String kind;
    public String developerPayload;
    public long purchaseTime;

    public long initiationTimestampMsec;
    public long validUntilTimestampMsec;
    public boolean autoRenewing;
}