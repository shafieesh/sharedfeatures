package com.chainedminds.models.games;

import java.util.List;

public class QW_Data extends GameData {

    public Question question;

    public int[] scores;

    public List<QW_Choice> choices;

    public int[] removeOptions;

    public long timestamp;
}