package com.chainedminds.dataClasses;

import java.util.List;

public class League {

    public int id;

    public String market;
    public String title;
    public String rules;

    public int price;

    public long startTime;
    public long finishTime;
    public long playStartTime;
    public long playFinishTime;

    public boolean directPayment;
    public boolean active;
    public boolean participating;

    public Prize prize;
    public List<Prize> prizes;
    public String description;
    public String bannerAddress;
    public String backgroundAddress;
    public String link;
}