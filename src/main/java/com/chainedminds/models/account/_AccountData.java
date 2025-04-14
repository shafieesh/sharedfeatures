package com.chainedminds.models.account;

import com.chainedminds.models.payment._TransactionData;

import java.util.List;
import java.util.Set;

public class _AccountData {

    public int id;
    public String name;
    public String username;
    public String password;
    public Boolean isActive;
    public Long phoneNumber;
    public String email;
    public String language;
    public Set<String> permissions;
    public Set<String> subscriptions;
    public Integer onlineStatus;
    public String activity;
    public String title;

    public String credential;
    public Long registrationTime;
    public Long lastUpdate;

    public List<_TransactionData> transactions;

}