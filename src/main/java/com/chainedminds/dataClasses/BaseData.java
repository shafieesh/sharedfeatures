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

    public int request;
    public int subRequest;
    public int lowerSubRequest;
    public int response;
    public String message;

    public FileData file;
    public List<FileData> files;

    public FriendData friend;
    public List<FriendData> friends;

    public AdData ad;

    public BaseDbData database;
}