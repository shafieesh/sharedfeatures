package com.chainedminds.models.notification;

import com.chainedminds.models.account._AccountData;

import java.util.List;

public class Action {

    public int request;
    public int subRequest;
    public String name;
    public _AccountData account;
    public _MessageData message;
    public List<BarNotification> barNotifications;
}
