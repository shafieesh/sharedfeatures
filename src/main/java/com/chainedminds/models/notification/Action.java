package com.chainedminds.models.notification;

import com.chainedminds.models.BarNotification;
import com.chainedminds.models.BaseMessageData;
import com.chainedminds.models.BaseNewsData;
import com.chainedminds.models.account.BaseAccountData;

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
