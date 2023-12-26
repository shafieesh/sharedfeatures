package com.chainedminds.api.accounting;

import com.chainedminds._Config;
import com.chainedminds._Resources;
import com.chainedminds.utilities.TaskManager;
import com.chainedminds.utilities.database._DatabaseOld;

import java.sql.Connection;

public class _Coins {

    private static final String TAG = _Coins.class.getSimpleName();

    protected static final String FIELD_COINS = "Coins";
    protected static final String FIELD_LAST_COIN_CHARGE_AMOUNT = "LastCoinChargeAmount";
    protected static final String FIELD_PREMIUM_PASS = "PremiumPass";

    public void start() {

        TaskManager.addTask(TaskManager.Task.build()
                .setName("ChargeCoins")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 10, 0)
                .setTimingListener(task -> chargeCoins())
                .schedule());

        TaskManager.addTask(TaskManager.Task.build()
                .setName("ChargePremiumCoins")
                .setTime(0, 0, 0)
                .setInterval(0, 0, 3, 0)
                .setTimingListener(task -> chargePremiumCoins())
                .schedule());
    }

    protected void chargeCoins() {

        String updateStatement = "UPDATE " + _Config.TABLE_ACCOUNTS + " SET " + FIELD_COINS + " = " +
                FIELD_COINS + " + 20, " + FIELD_LAST_COIN_CHARGE_AMOUNT + " = " + FIELD_LAST_COIN_CHARGE_AMOUNT +
                " + 20 WHERE " + FIELD_PREMIUM_PASS + " = FALSE AND " + FIELD_COINS + " < 100";

        _DatabaseOld.update(TAG, updateStatement);
    }

    protected void chargePremiumCoins() {

        String updateStatement = "UPDATE " + _Config.TABLE_ACCOUNTS + " SET " + FIELD_COINS + " = " +
                FIELD_COINS + " + 20, " + FIELD_LAST_COIN_CHARGE_AMOUNT + " = " + FIELD_LAST_COIN_CHARGE_AMOUNT +
                " + 20 WHERE " + FIELD_PREMIUM_PASS + " = TRUE AND " + FIELD_COINS + " < 100";

        _DatabaseOld.update(TAG, updateStatement);
    }

    public int getCoins(int userID) {

        return _Resources.getInstance().accountManager.getProperty(userID, FIELD_COINS, Integer.class);
    }

    public int getCoins(Connection connection, int userID) {

        return _Resources.getInstance().accountManager.getProperty(connection, userID, FIELD_COINS, Integer.class);
    }

    protected boolean setCoins(Connection connection, int userID, int coins) {

        return _Resources.getInstance().accountManager.setProperty(connection, userID, FIELD_COINS, coins);
    }

    public boolean changeCoins(Connection connection, int userID, int amount) {

        synchronized (userID + "") {

            int storedCoins = getCoins(connection, userID);

            if (storedCoins != -1) {

                int newCoins = storedCoins + amount;

                boolean wasSuccessful;

                if (newCoins >= 0) {

                    wasSuccessful = setCoins(connection, userID, newCoins);

                } else {

                    wasSuccessful = setCoins(connection, userID, 0);
                }

                return wasSuccessful;

            } else {

                return false;
            }
        }
    }

    public boolean hasEnoughCoins(int userID, int coins) {

        int storedCoins = getCoins(userID);

        return storedCoins != -1 && storedCoins >= coins;
    }
}
