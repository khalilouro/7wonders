package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.utils.Log;
import fr.uca.miage.sevenwonders.models.player.*;

import java.util.Map;

/**
 * Represents a purple effect card in the Seven Wonders game. Can be either
 * immediate (golden cards) or end-game (purple cards).
 */
public class PurpleEffect {
    private boolean includeSelf;
    private boolean includeLeft;
    private boolean includeRight;
    private Effect.VictoryPoints points;
    private Effect.Gold gold;
    private String type;
    private String[] color;
    private boolean immediate; // true = apply immediately, false = apply at end game

    public PurpleEffect(boolean includeSelf, boolean includeLeft, boolean includeRight, Effect.VictoryPoints points,
            Effect.Gold gold, String type, String[] color, boolean immediate) {
        this.includeSelf = includeSelf;
        this.includeLeft = includeLeft;
        this.includeRight = includeRight;
        this.points = points;
        this.gold = gold;
        this.type = type;
        this.color = color;
        this.immediate = immediate;
    }

    public void applyPurpleEffect(Player player) {
        int count = 0;

        if (includeSelf) {
            count += countBoardElements(player, type, color);
        }
        if (includeRight) {
            count += countBoardElements(player.getRight(), type, color);
        }
        if (includeLeft) {
            count += countBoardElements(player.getLeft(), type, color);
        }

        for (int i = 0; i < count; i++) {
            if (points != null) {
                points.apply(player);
            }
            if (gold != null) {
                gold.apply(player);
            }
        }

        // Log the application for debugging
        String effectType = immediate ? "immediate (golden)" : "end-game (purple)";
        Log.logEvent(String.format("%s applied %s perBoardElement effect: count=%d, points=%s, gold=%s",
                player.getName(), effectType, count, points != null ? points.points() : "null",
                gold != null ? gold.amount() : "null"));
    }

    public int calculatePoints(Player player) {
        int count = 0;

        if (includeSelf) {
            count += countBoardElements(player, type, color);
        }
        if (includeRight) {
            count += countBoardElements(player.getRight(), type, color);
        }
        if (includeLeft) {
            count += countBoardElements(player.getLeft(), type, color);
        }

        if (points != null) {
            return count * points.points();
        }

        return 0;
    }

    private int countBoardElements(Player player, String type, String[] color) {
        if (player == null)
            return 0; // Safety check

        int count = 0;
        Map<String, Integer> boardElements = player.getBoardElement();

        if (type.equals("CARD")) {
            for (String c : color) {
                // Use getOrDefault to prevent NullPointerException
                count += boardElements.getOrDefault(c, 0);
            }
        } else {
            count = boardElements.getOrDefault(type, 0);
        }
        return count;
    }

    // Getter for debugging/logging
    public boolean isImmediate() {
        return immediate;
    }
}
