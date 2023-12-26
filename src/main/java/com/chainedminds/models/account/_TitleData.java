package com.chainedminds.models.account;

public class _TitleData {

    public int userID;
    public String title;
    public long dateTime;
    public String gamerTag;

    @Override
    public int hashCode() {

        return userID;
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (!(anotherObject instanceof _TitleData)) {

            return false;
        }

        if (anotherObject == this) {

            return true;
        }

        return this.userID == ((_TitleData) anotherObject).userID;
    }
}