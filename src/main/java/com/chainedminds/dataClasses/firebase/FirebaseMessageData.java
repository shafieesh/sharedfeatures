package com.chainedminds.dataClasses.firebase;

import java.util.Map;

public class FirebaseMessageData {

    public String to;
    public String priority;
    public String color;
    //public List<String> registration_ids = new LinkedList<>();
    //public List<String> registration_ids;
    public NotificationData notification;
    public Map<String, String> data;
    public OptionsData fcm_options = new OptionsData();

    public static class NotificationData {

        public String title;
        public String body;
        public String image;
        public String icon = "icon_notification";
        public String color;// = "#aaee33";
        public String sound = "default";
        public String tag;
        public String click_action;
        public String sticky;
    }

    public static class OptionsData {

        public String analytics_label = "default";
    }
}