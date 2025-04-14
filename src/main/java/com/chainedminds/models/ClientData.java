package com.chainedminds.models;

import io.netty.channel.ChannelHandlerContext;

public class ClientData {

    public String firebaseID;
    public String appName;
    public String version;
    public String latestVersion;
    public Integer apiVersion;
    public String platform;
    public String language;
    public String market;

    public String address;
    public Integer sdk;
    public String brand;
    public String manufacturer;
    public String model;
    public String product;
    public String uuid;
    //public transient String channelID;
    public transient ChannelHandlerContext channelContext;
    public String packageName;
    public String country;
    public String gameName;
}
