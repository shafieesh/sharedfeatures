package com.chainedminds.models.games;

import com.chainedminds.models.Position;
import com.chainedminds.models.games.fluffia.MapObject;

public class FF_Data extends GameData {

    public int winner;
    public Position myPosition;
    public MapObject[] inventory;

    public int score;

    public Position[] playerPositions;

    //public int team;

    public MapObject mapObject;

    public int collectedItemType;


}
