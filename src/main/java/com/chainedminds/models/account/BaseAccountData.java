package com.chainedminds.models.account;

import com.chainedminds.models.payment.BaseTransactionData;

import java.util.List;
import java.util.Set;

public class BaseAccountData {

    public int id;
    public String username;
    public String password;
    public boolean isActive;
    public String name;
    public long phoneNumber;
    public String email;
    public String language;
    public Set<String> permissions;
    public Set<String> subscriptions;
    public int onlineStatus;
    public String activity;
    public String title;

    public String credential;
    public long registrationTime;
    public long lastUpdate;

    public List<BaseTransactionData> transactions;

}