package com.chainedminds.models.games;

import java.util.List;

public class MCH_Data extends GameData {

    public int winner;
    public int diceValue;
    public int rollFor;
    public List<PawnPosition> movablePawns;
    public PawnPosition pawn;
    public List<PawnPosition> pawnsMoves;
    public int state;
}
