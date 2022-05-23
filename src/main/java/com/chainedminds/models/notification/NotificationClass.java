package com.chainedminds.models.notification;

import java.util.List;

public class NotificationClass {

    public int id;
    public String title;
    public String content;
    public Action action;
    public String tag;

    public NotificationButton button;
    public List<NotificationButton> buttons;
}
