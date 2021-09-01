package com.chainedminds.dataClasses;

import java.util.List;

public class Session {

    public int id;

    public int ownerID;
    public String ownerGamerTag;

    public BaseMessageData lastMessage;
    public List<BaseMessageData> messages;
}
