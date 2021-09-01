package com.chainedminds.dataClasses;

import com.chainedminds.dataClasses.account.BaseAccountData;
import com.chainedminds.dataClasses.account.BaseFriendData;
import com.chainedminds.dataClasses.account.TitleData;
import com.chainedminds.dataClasses.advertisements.AdData;
import com.chainedminds.dataClasses.games.Question;
import com.chainedminds.dataClasses.notification.BaseNotificationData;
import com.chainedminds.dataClasses.payment.BaseIABTransactionData;
import com.chainedminds.dataClasses.payment.BaseIPGTransactionData;

import java.util.List;
import java.util.Map;

public class BaseData<
        Account extends BaseAccountData<? extends BaseFriendData>,
        ProfileData extends BaseProfileData,
        FriendData extends BaseFriendData,
        FileData extends BaseFileData,
        IABTransactionData extends BaseIABTransactionData,
        IPGTransactionData extends BaseIPGTransactionData,
        ProductData extends BaseProductData,
        MessageClass extends BaseMessageData,
        NotificationData extends BaseNotificationData,
        NewsData extends BaseNewsData>{

    public ClientData client;

    public Account account;
    public List<Account> accounts;

    public List<Account> topPlayers;

    public Account player;
    public List<Account> players;

    public ProfileData profile;
    public List<ProfileData> profiles;

    public FriendData friend;
    public List<FriendData> friends;

    public FileData file;
    public List<FileData> files;

    public IABTransactionData iabTransaction;
    public IPGTransactionData ipgTransaction;

    public ProductData product;
    public List<ProductData> products;

    public NotificationData notification;

    public GameSettings gameSettings;


    @Deprecated
    public String appName;
    @Deprecated
    public int appVersion;
    @Deprecated
    public int apiVersion;
    @Deprecated
    public String platform;
    @Deprecated
    public String market;
    @Deprecated
    public String language;
    @Deprecated
    public String country;
    @Deprecated
    public String firebaseID;


    @Deprecated
    public String gameName;
    @Deprecated
    public int gamerTagChangePrice;

    public int request;
    public int subRequest;
    public int lowerSubRequest;
    public int response;
    public String message;

    public int gameRequest;

    public AdData ad;

    public CoinChargeSettings coinChargeSettings;

    public List<TitleData> titles;

    public List<Gift> giftsList;

    public UpdateInfoClass updateInfo;

    public Asset asset;
    public List<Asset> assets;

    public List<Lottery> lotteries;
    public Lottery lottery;

    public WheelOfFortune wheelOfFortune;

    public League league;
    public League nextLeague;

    public GamePlay gamePlay;

    public BaseDbData database;

    public MessageClass messageData;

    public Map<String, Integer> onlinePlayers;
    public Map<String, Integer> gameStats;

    @Deprecated
    public int entryFee;
    @Deprecated
    public long time;


    public Report report;

    public Question question;
    public List<Question> questions;

    public List<Mapping> mappings;

    public NewsData news;
    public List<NewsData> newsList;

    public Toast toast;
    public Session session;
    public List<Session> sessions;
}