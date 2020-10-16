package com.chainedminds.dataClasses;

import com.chainedminds.dataClasses.account.BaseAccountData;

public class Player {

    public String uuid;
    public String market;
    public String language;
    public BaseAccountData account;

    //public transient ReentrantLock SOCKET_READ_LOCK;
    //public transient ReentrantLock SOCKET_WRITE_LOCK;
    //public transient Socket socket;
    //public transient InputStream inputStream;
    //public transient OutputStream outputStream;
    public int balance;
    public int points;
    public int score;
    public ClientData client;
    public int position;
    public int team;
    public int joinedHolder;

    public String channel;

    @Deprecated
    public String appName;
    @Deprecated
    public int appVersion;
    @Deprecated
    public boolean isBot;
}