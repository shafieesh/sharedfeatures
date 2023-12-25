package com.chainedminds.models;

import com.chainedminds.models.account.BaseAccountData;
import com.chainedminds.models.account.BaseFriendData;
import com.chainedminds.models.advertisements.AdData;

import java.util.List;

public class BaseData<Account extends BaseAccountData> {

    public ClientData client;

    public Account account;
    public List<Account> accounts;

    public int request;
    public int subRequest;
    public int lowerSubRequest;
    public int response;
    public String message;

    public BaseFileData file;
    public List<BaseFileData> files;

    public BaseFriendData friend;
    public List<BaseFriendData> friends;

    public AdData ad;

    public BaseDbData database;
}