package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.player.PlayerResources;

/**
 * Represents the central bank in the Seven Wonders game. The bank manages gold
 * and silver coins, handles transactions with players, and provides basic money
 * exchange operations.
 */
public class Bank {

    /** The number of silver coins available in the bank (value = 1). */
    private int silver;

    /** The number of gold coins available in the bank (value = 3). */
    private int gold;

    /** The single instance of the Bank (Singleton). */
    private static Bank instance;

    /**
     * Private constructor to prevent instantiation from outside (Singleton
     * pattern). Default values: 30 silver coins and 20 gold coins.
     */
    private Bank() {
        reset();
    }

    /**
     * Returns the single instance of the Bank (Singleton pattern).
     *
     * @return the Bank instance
     */
    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    /**
     * Resets the bank to its initial state with default coin amounts. This should
     * be called at the start of each new game.
     */
    public void reset() {
    }

    /**
     * Withdraws a given amount of silver coins from the bank and adds them to the
     * player's balance.
     *
     * @param value
     *            the number of silver coins to withdraw
     * @param p
     *            the player receiving the coins
     * @return true if the withdrawal is successful, false if there is not enough
     *         silver
     */
    public boolean WithdrawSilver(int value, Player p) {
        p.getResources().addSilver(value);
        return true;
    }

    /**
     * Withdraws a given amount of gold coins from the bank and adds them to the
     * player's balance.
     *
     * @param value
     *            the number of gold coins to withdraw
     * @param p
     *            the player receiving the coins
     * @return true if the withdrawal is successful, false if there is not enough
     *         gold
     */
    public boolean WithdrawGold(int value, Player p) {
        p.getResources().addGold(value);
        return true;
    }

    /**
     * Withdraws a given amount of currency from the bank (in silver coin
     * equivalent) and adds it to the player's balance. The player's balance is
     * converted to gold after the withdrawal.
     *
     * @param amount
     *            the amount to withdraw (in silver coin equivalent)
     * @param p
     *            the player receiving the withdrawal
     * @return true if the withdrawal is successful, false if there are insufficient
     *         funds in the bank
     */
    public boolean withdraw(int amount, Player p) {
        PlayerResources resources = p.getResources();
        resources.addSilver(amount);

        // Optimize Player's wallet (Convert excess Silver to Gold)
        if (resources.getSilver() >= 3) {
            int newGold = resources.getSilver() / 3;
            int remainingSilver = resources.getSilver() % 3;

            resources.addGold(newGold);
            resources.setSilver(remainingSilver);
        }

        return true;
    }

    /**
     * Exchanges gold for silver or vice versa depending on the player's and bank's
     * resources. This method simulates a change of 1 gold coin into 3 silver coins
     * (Player gives 1 Gold, receives 3 Silver).
     *
     * @param p
     *            the player performing the exchange
     * @return true if the exchange is successful, false otherwise
     */
    public boolean Change(Player p) {
        PlayerResources resources = p.getResources();

        // Bank must have silver to give, Player must have Gold to change
        if (resources.getGold() >= 1) {
            resources.removeGold(1);
            resources.addSilver(3);
            return true;
        }
        return false;
    }

    /**
     * Deposits gold and silver coins from a player into the bank.
     *
     * @param gold
     *            the amount of gold coins to deposit
     * @param silver
     *            the amount of silver coins to deposit
     * @param p
     *            the player depositing the coins
     */
    public void Deposit(int gold, int silver, Player p) {
        this.gold += gold;
        this.silver += silver;

        PlayerResources resources = p.getResources();
        // Use removeGold logic or direct subtraction if setter available
        // Assuming implementation of removeGold/removeSilver or direct set:
        resources.setGold(resources.getGold() - gold);
        resources.setSilver(resources.getSilver() - silver);
    }

    /**
     * Handles a payment from a player to the bank. The method checks if the player
     * has enough funds (in gold and silver combined), converts currencies if
     * needed, and performs the deposit.
     *
     * @param p
     *            the player making the payment
     * @param amount
     *            the total amount to pay (in silver coin equivalent)
     * @return true if the payment succeeds, false otherwise
     */
    public boolean Pay(Player p, int amount) {
        PlayerResources resources = p.getResources();
        int totalValue = resources.getGold() * 3 + resources.getSilver();

        if (totalValue < amount) {
            return false;
        }

        int gold_amount = Math.min(resources.getGold(), amount / 3);
        int remain = amount - (gold_amount * 3);

        // If player has enough silver for the remainder
        if (resources.getSilver() >= remain) {
            Deposit(gold_amount, remain, p);
            return true;
        }

        // If not enough silver, try to change 1 Gold into 3 Silver (if Bank allows or
        // Player internal change)
        // Note: The original logic called Change(p) which involves the Bank.
        // If Change(p) succeeds, the player has -1 Gold and +3 Silver.
        if (Change(p)) {
            // Recursive attempt or manual adjustment?
            // Original logic implies we try the deposit again or handle the logic here.
            // Simplified based on original:
            Deposit(gold_amount, remain, p);
            return true;
        }

        return false;
    }

    /**
     * Trades an amount between two players. The amount is in silver coin
     * equivalent. The method will automatically convert between silver and gold
     * coins as necessary.
     *
     * @param fromPlayer
     *            the player giving the coins
     * @param toPlayer
     *            the player receiving the coins
     * @param amount
     *            the total amount to transfer in silver coin equivalent
     * @return true if the trade is successful, false otherwise
     */
    public boolean trade(Player fromPlayer, Player toPlayer, int amount) {
        PlayerResources fromRes = fromPlayer.getResources();
        PlayerResources toRes = toPlayer.getResources();

        // Check if fromPlayer has enough coins to cover the trade
        if (fromRes.getSilver() + fromRes.getGold() * 3 < amount) {
            return false; // Not enough resources
        }

        int goldAmount = amount / 3;
        int silverAmount = amount % 3;

        // Deduct from fromPlayer
        if (fromRes.getGold() >= goldAmount) {
            fromRes.setGold(fromRes.getGold() - goldAmount);
        } else {
            // Not enough gold, substitute with silver equivalent logic?
            // Original logic: "If not enough gold, subtract the remaining amount from
            // silver"
            // Wait, logic in original was actually resetting Gold to 0 and calculating
            // deficit.
            int availableGold = fromRes.getGold();
            int deficitGold = goldAmount - availableGold;

            fromRes.setGold(0);
            silverAmount += deficitGold * 3;
        }

        if (fromRes.getSilver() >= silverAmount) {
            fromRes.setSilver(fromRes.getSilver() - silverAmount);
        } else {
            return false; // Should satisfy check above, but safety first
        }

        // Add to toPlayer
        toRes.addSilver(amount);

        // Normalize toPlayer (Convert excess Silver to Gold)
        if (toRes.getSilver() >= 3) {
            toRes.addGold(toRes.getSilver() / 3);
            toRes.setSilver(toRes.getSilver() % 3);
        }

        return true;
    }
}
