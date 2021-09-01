package com.chainedminds.dataClasses.games;

import com.chainedminds.dataClasses.BaseMessageData;
import com.chainedminds.dataClasses.GameSettings;
import com.chainedminds.dataClasses.Player;
import com.chainedminds.dataClasses.account.BaseAccountData;

public class GameData {

    public int request;
    public int subRequest;
    public int gameRequest;
    public int response;

    public int position;

    public GameSettings gameSettings;

    public BaseMessageData newMessage;

    public BaseAccountData account;
    public Player player;

    public long startTime;
    public long finishTime;

    public int currentTurn;

    public String message;

    public int lastMessageNumber;
}