package com.chainedminds.models.account;

import com.chainedminds.models.payment._TransactionData;

import java.util.List;
import java.util.Set;

public class _AccountData {

    public int id;
    public String name;
    public String username;
    public String password;
    public boolean isActive;
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

    public List<_TransactionData> transactions;

}