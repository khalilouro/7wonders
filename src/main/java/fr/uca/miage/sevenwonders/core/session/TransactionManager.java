package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.NeighborTrading;
import fr.uca.miage.sevenwonders.models.player.Player;

import fr.uca.miage.sevenwonders.utils.Log;

import java.util.Arrays;

public class TransactionManager {
    private final Bank bank;

    public TransactionManager(Bank bank) {
        this.bank = bank;
    }

    public void payCost(Cost cost, Player payer) {
        switch (cost) {
            case Cost.Free free -> Log.logEvent(payer.getName() + " pays nothing (free cost)");
            case Cost.Gold gold -> payGoldCost(payer, gold.amount());
            case Cost.Materials materials -> Log.logEvent(payer.getName() + " uses materials: " + Arrays.toString(materials.materials()));
            case Cost.Compound compound -> {
                Log.logEvent(payer.getName() + " pays compound cost with " + compound.costs().size() + " components");
                for (Cost subCost : compound.costs()) {
                    payCost(subCost, payer);
                }
            }
            case Cost.Trading trading -> {
                Log.logEvent(payer.getName() + " pays trading cost");
                payCost(trading.baseCost(), payer);

                NeighborTrading tradingInfo = trading.tradingInfo();
                for (NeighborTrading.Trade trade : tradingInfo.getTrades()) {
                    if (!transferGold(payer, trade.neighbor(), trade.cost())) {
                        throw new IllegalStateException("Failed to complete trade with neighbor");
                    }
                    Log.logEvent(String.format("%s buys %s from %s for %d coins.",
                            payer.getName(), trade.resource(), trade.neighbor().getName(), trade.cost()));
                }
            }
            default -> throw new IllegalStateException("Unknown cost type: " + cost.getClass().getSimpleName());
        }
    }

    public void payGoldCost(Player player, int goldAmount) {
        Log.logEvent(player.getName() + " attempts to pay " + goldAmount + " coins");
        if (!bank.Pay(player, goldAmount)) {
            throw new IllegalStateException("Player " + player.getName() + " cannot afford " + goldAmount);
        }
        Log.logEvent(player.getName() + " successfully paid " + goldAmount + " coins.");
    }

    public boolean transferGold(Player from, Player to, int amount) {
        Log.logEvent(String.format("%s is transferring %d coins to %s.", from.getName(), amount, to.getName()));
        boolean success = bank.trade(from, to, amount);
        if (!success) {
            Log.logEvent(from.getName() + " cannot afford the trade");
            return false;
        }
        Log.logEvent(String.format("%s transferred %d coins to %s.", from.getName(), amount, to.getName()));
        return true;
    }
}
