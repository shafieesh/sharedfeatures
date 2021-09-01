package com.chainedminds.dataClasses.notification;

import com.chainedminds.dataClasses.BarNotification;
import com.chainedminds.dataClasses.BaseMessageData;
import com.chainedminds.dataClasses.BaseNewsData;
import com.chainedminds.dataClasses.account.BaseAccountData;

import java.util.List;

public class Action {

    public int request;
    public int subRequest;
    public String name;
    public BaseAccountData account;
    public BaseNewsData news;
    public BaseMessageData message;
    public List<BarNotification> barNotifications;
}
