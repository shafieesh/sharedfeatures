package com.chainedminds.models;

import com.chainedminds._Config;
import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.account._FriendData;

import java.util.List;

public class _Data<Account extends _AccountData> {

    public ClientData client;

    public Account account;
    public List<Account> accounts;

    public int request;
    public Integer subRequest;
    public int response;
    public String message;

    public _FriendData friend;
    public List<_FriendData> friends;

    public String engine = _Config.ENGINE_NAME;
}