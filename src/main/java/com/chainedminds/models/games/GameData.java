package com.chainedminds.models.games;

import com.chainedminds.models.BaseMessageData;
import com.chainedminds.models.GameSettings;
import com.chainedminds.models.Player;
import com.chainedminds.models.account.BaseAccountData;

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