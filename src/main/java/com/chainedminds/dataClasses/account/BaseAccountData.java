package com.chainedminds.dataClasses.account;

import com.chainedminds.dataClasses.BaseProfileData;
import com.chainedminds.dataClasses.Customization;
import com.chainedminds.dataClasses.payment.BaseTransactionData;

import java.util.List;

public class BaseAccountData<FriendData extends BaseFriendData> {

    public int position;
    public int id;

    public String username;
    public String gamerTag;
    public String password;
    public String credential;
    public String token;

    public long phoneNumber;
    public String name;
    public String email;
    public String bio;

    public int score;
    public int level;
    public int nextLevelScore;
    public int currentLevelScore;
    public int coins;
    public int tickets;

    public int qwTeamID;

    @Deprecated
    public BaseAccountData<? extends BaseFriendData> account;

    public BaseProfileData profile;

    public String language;

    public FriendData friendship;

    public List<String> permissions;
    public List<String> subscriptions;
    public Customization customizations;

    public int onlineStatus;
    public String activity;
    public String title;
    public int lastCoinChargeAmount;
    public int lastTicketChargeAmount;
    public String avatar;

    public List<BaseTransactionData> transactions;
}