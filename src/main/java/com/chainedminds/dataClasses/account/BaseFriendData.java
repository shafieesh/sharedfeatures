package com.chainedminds.dataClasses.account;

public class BaseFriendData extends BaseAccountData {

    public int status;

    @Override
    public int hashCode() {

        return super.id;
    }

    @Override
    public boolean equals(Object anotherObject) {

        if (anotherObject instanceof Number) {

            return super.id == (int) anotherObject;
        }

        if (!(anotherObject instanceof BaseFriendData)) {

            return false;
        }

        if (anotherObject == this) {

            return true;
        }

        BaseFriendData anotherFriend = (BaseFriendData) anotherObject;

        return super.id == anotherFriend.id;
    }
}