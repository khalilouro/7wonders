package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains detailed information about trading with neighbors
 */
public class NeighborTrading {
    private final List<Trade> trades;
    private int baseCostPerResource;

    public NeighborTrading() {
        this(2); // Default cost is 2 gold per resource
    }

    public NeighborTrading(int baseCostPerResource) {
        this.trades = new ArrayList<>();
        this.baseCostPerResource = baseCostPerResource;
    }

    public void addTrade(Player neighbor, Card.Materials resource, int cost) {
        this.trades.add(new Trade(neighbor, resource, cost));
    }

    public void addTrade(Player neighbor, Card.Materials resource) {
        this.trades.add(new Trade(neighbor, resource, baseCostPerResource));
    }

    public List<Trade> getTrades() {
        return new ArrayList<>(trades);
    }

    public int getTotalTradingCost() {
        return trades.stream().mapToInt(Trade::cost).sum();
    }

    public void applyDiscount(int discount) {
        this.baseCostPerResource = Math.max(1, baseCostPerResource - discount);
        // Recalculate existing trades with new base cost
        for (int i = 0; i < trades.size(); i++) {
            Trade oldTrade = trades.get(i);
            trades.set(i, new Trade(oldTrade.neighbor(), oldTrade.resource(), baseCostPerResource));
        }
    }

    public void applyNeighborDiscount(Player neighbor, int discount) {
        for (int i = 0; i < trades.size(); i++) {
            Trade trade = trades.get(i);
            if (trade.neighbor().equals(neighbor)) {
                int newCost = Math.max(1, trade.cost() - discount);
                trades.set(i, new Trade(trade.neighbor(), trade.resource(), newCost));
            }
        }
    }

    public record Trade(Player neighbor, Card.Materials resource, int cost) {
    }
}
