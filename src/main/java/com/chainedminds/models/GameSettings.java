package com.chainedminds.models;

import com.chainedminds.models.games.Point;
import com.chainedminds.models.games.Question;

import java.util.List;
import java.util.Map;

public class GameSettings {

    public long lobbyID;
    public String gameID;

    public int lastMessageNumber;

    public String name;
    public int state = 0;
    public int loadingPercent;

    public String[] playerNames;
    public String[] playerColors = new String[] {"#ea832e", "#63d157", "#a43fdb", "#3fdbdb"};
    public int[] playerScores;
    public int[] playerIDs;
    public String[] gamerTags;
    public String[] chatSentences;
    public String[][] honors;
    public String message;
    public String gameRule;

    public List<BaseMessageData> newMessages;

    public int[] numbers;

    public String[] availableGames;

    public Question[] questions;
    public String[] wordEaterWords;

    public int gameDimension;

    public String lobbyName;
    public int requestCode;
    public int response;
    public String responseMessage;

    public int yourPosition;

    public int gameRequest;
    public int gameState;

    public int currentTurn;

    public List<Point> winningPoints;
    public int winner;

    public int battlingWith;
    public int battlingFor;

    public int fillColumn;
    public int fillRow;
    public int fillWith;

    public Position[] playerPositions;
    public int[][] gameMatrix;
    public int[][] bombMatrix;
    public int[][] sawMatrix;
    public int[][] collectableMatrix;

    public int[] teamA;
    public int[] teamB;

    public int entryCoins;

    public int winnerCoins;
    public int winnerTickets;
    public int winnerScore;
    public int loserScore;

    public Map<String, Integer> powerUpPrice;

    public int givenScore;
    public int givenCoins;

}