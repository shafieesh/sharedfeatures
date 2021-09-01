package com.chainedminds.dataClasses.notification;

import java.util.List;

public class BaseNotificationData {

    public int id;
    public String title;
    public String content;
    public Action action;
    public String tag;

    public NotificationButton button;
    public List<NotificationButton> buttons;
    public String appName;
}
