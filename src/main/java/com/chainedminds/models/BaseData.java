package com.chainedminds.models;

import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.models.account.BaseFriendData;
import com.chainedminds.models.advertisements.AdData;
import com.chainedminds.models.notification.BaseNotificationData;
import com.chainedminds.models.payment.BaseIABTransactionData;
import com.chainedminds.models.payment.BaseIPGTransactionData;

import java.util.List;

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