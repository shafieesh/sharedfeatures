package com.chainedminds.dataClasses;

public class GameStatus {

    public String name;

    public int state;
    public int loadingPercent;

    public int currentTurn;
    public int currentTurnRemainingTime;

    public Toast toast;
    public int winner;

    public String message;

    public int getCurrentTurnRemainingTime() {

        return currentTurnRemainingTime;
    }

    public void setCurrentTurnRemainingTime(int newTime) {

        currentTurnRemainingTime = newTime;
    }

    public int getState() {

        return state;
    }

    public void setState(int newState) {

        state = newState;
    }
}