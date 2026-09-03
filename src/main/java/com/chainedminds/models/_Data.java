package com.chainedminds.models;

import com.chainedminds._Config;
import com.chainedminds.models.account._AccountData;

import java.util.List;

@SuppressWarnings("unused")
public class _Data<Account extends _AccountData> {

    public ClientData client;

    public Account account;
    public List<Account> accounts;

    public Integer request;
    public Integer subRequest;
    public Integer response;
    public String message;

    public String engine = _Config.ENGINE_NAME;
}