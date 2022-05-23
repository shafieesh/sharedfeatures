package com.chainedminds.models.games;

import java.util.List;

public class C4_Data extends GameData {

    public int gameState;
    public int state;

    public int fillColumn;
    public int fillRow;
    public int fillWith;

    public List<Point> winningPoints;
    public int winner;
}