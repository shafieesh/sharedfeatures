package com.chainedminds.models;

import com.chainedminds.models.account.BaseAccountData;

public class Prize {

    public int id;
    public String name;
    public BaseAccountData owner;
    public String status;
    public String statusColor;
    public String payload;
    public String description;
    public boolean isClaimed;

    public String type;
    public int value;
    public String market;
    public int lotteryID;
    public long payloadEditTime;
}
