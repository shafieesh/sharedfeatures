package com.chainedminds.dataClasses.account;

import java.util.Comparator;

public class LeaderboardData {

    public int id;
    public String gamerTag;
    public int score;

    public LeaderboardData() {

    }

    public LeaderboardData(int id, String gamerTag, int score) {

        this.id = id;
        this.gamerTag = gamerTag;
        this.score = score;
    }

    public static Comparator<LeaderboardData> getComparator(){

        return (player1, player2) -> {

            if (player1.score == player2.score) {

                if (player1.gamerTag != null && player2.gamerTag != null) {

                    return player1.gamerTag.compareToIgnoreCase(player2.gamerTag);

                } else {

                    return 0;
                }

            } else {

                if (player1.score > player2.score) {

                    return -1;

                } else {

                    return 1;
                }
            }
        };
    }

    @Override
    public int hashCode() {

        return id;
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (anotherObject instanceof Integer) {

            return this.id == (int) anotherObject;
        }

        if (anotherObject == this) {

            return true;
        }

        if (anotherObject instanceof LeaderboardData) {

            LeaderboardData anotherAsset = (LeaderboardData) anotherObject;

            return this.id == anotherAsset.id;

        } else {

            return false;
        }
    }
}