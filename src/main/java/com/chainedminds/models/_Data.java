package com.chainedminds.models;

import com.chainedminds.models.account._AccountData;
import com.chainedminds.models.account._FriendData;

import java.util.List;

public class _Data<Account extends _AccountData> {

    public ClientData client;

    public Account account;
    public List<Account> accounts;

    public int request;
    public int subRequest;
    public int lowerSubRequest;
    public int response;
    public String message;

    public _FileData file;
    public List<_FileData> files;

    public _FriendData friend;
    public List<_FriendData> friends;

    public _DbData database;
}