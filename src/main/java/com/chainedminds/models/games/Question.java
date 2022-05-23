package com.chainedminds.models.games;

public class Question {

    public int id;

    public String questionText;
    public int questionScore;

    public int rightOption;
    public int leftOption;

    public int answer;
    public int index;

    public int[] answersStatistics;

    public int providerID;
    public String providerName;

    public String option1;
    public String option2;
    public String option3;
    public String option4;

    public int option1Price;
    public int option2Price;
    public int option3Price;
    public int option4Price;

    public int status;
}