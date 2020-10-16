package com.chainedminds.dataClasses;

import java.util.List;

public class Lottery {

    public int id;
    public int participants;
    public int soldTickets;
    public int purchasedTickets;
    public long startTime;
    public long finishTime;
    public Prize prize;
    public List<Prize> prizes;
    public String title;
    public String market;
    public boolean isActive;
    public boolean hasWon;
    public int ticketPrice;
}
