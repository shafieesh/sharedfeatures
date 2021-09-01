package com.chainedminds.dataClasses;

public class BaseMessageData {

    public int ownerID;

    public String ownerName;
    @Deprecated
    public String ownerNameColor;

    public String name;
    public String nameColor;

    public String message;
    public String messageColor;

    public String title;
    public String titleColor;

    public String backgroundColor;
    public String backgroundTexture;
    public BackgroundGradient backgroundGradient;

    @Deprecated
    public String backgroundFooter;
    public String badge;
    public String badgeColor;

    public long time;

    public int receptionistID;
    public String receptionistName;

    public int onlineState;
}