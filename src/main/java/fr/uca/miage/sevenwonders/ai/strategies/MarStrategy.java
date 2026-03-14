package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.ai.Strategy;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A custom strategy implementing a simplified heuristic-driven logic.
 * <p>
 * This strategy adjusts priorities depending on the current age: 1. Early
 * resource development. 2. Catching up in military. 3. Transitioning into
 * victory points.
 * </p>
 */
public class MarStrategy implements Strategy {

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        if (hand == null || hand.isEmpty()) {
            return -1;
        }

        // Determine Age based on the first card in hand
        return switch (hand.get(0).getAge()) {
            case AGE_I -> playAge1(bot);
            case AGE_II -> playAge2(bot);
            case AGE_III -> playAge3(bot);
        };
    }

    /**
     * Age I strategy
     */
    private int playAge1(Bot bot) {
        List<Card> hand = bot.getHand();

        if (getResourceCount(bot) < 3) {
            int idx = findBuildableCardByColor(bot, Card.Color.BROWN, hand);
            if (idx != -1)
                return encode(1, idx);
            idx = findBuildableCardByColor(bot, Card.Color.GREY, hand);
            if (idx != -1)
                return encode(1, idx);
        }

        if (!isLargelyAheadMilitarily(bot)) {
            int idx = findBuildableCardByColor(bot, Card.Color.RED, hand);
            if (idx != -1)
                return encode(1, idx);
        }

        int blueIdx = findBuildableCardByColor(bot, Card.Color.BLUE, hand);
        if (blueIdx != -1)
            return encode(1, blueIdx);

        int anyIdx = findAnyBuildableCard(bot, hand);
        if (anyIdx != -1)
            return encode(1, anyIdx);

        return encode(0, 0);
    }

    /**
     * Age II strategy
     */
    private int playAge2(Bot bot) {
        List<Card> hand = bot.getHand();

        int maxNeighborStr = getMaxNeighborMilitary(bot);
        int myStr = bot.getMilitaryStrength();

        if (myStr < maxNeighborStr) {
            int idx = findBuildableCardByColor(bot, Card.Color.RED, hand);
            if (idx != -1)
                return encode(1, idx);
        }

        int blueIdx = findBuildableCardByColor(bot, Card.Color.BLUE, hand);
        if (blueIdx != -1)
            return encode(1, blueIdx);

        if (getScienceSymbolCount(bot) >= 2) {
            int greenIdx = findBuildableCardByColor(bot, Card.Color.GREEN, hand);
            if (greenIdx != -1)
                return encode(1, greenIdx);
        }

        if (getResourceCount(bot) < 5) {
            int idx = findBuildableCardByColor(bot, Card.Color.BROWN, hand);
            if (idx != -1)
                return encode(1, idx);
            idx = findBuildableCardByColor(bot, Card.Color.GREY, hand);
            if (idx != -1)
                return encode(1, idx);
        }

        int anyIdx = findAnyBuildableCard(bot, hand);
        if (anyIdx != -1)
            return encode(1, anyIdx);

        return encode(0, 0);
    }

    /**
     * Age III strategy
     */
    private int playAge3(Bot bot) {
        List<Card> hand = bot.getHand();

        int maxNeighborStr = getMaxNeighborMilitary(bot);
        int myStr = bot.getMilitaryStrength();

        if (myStr < maxNeighborStr) {
            int redIdx = findBuildableCardByColor(bot, Card.Color.RED, hand);
            if (redIdx != -1)
                return encode(1, redIdx);
        }

        int purpleIdx = findBuildableCardByColor(bot, Card.Color.PURPLE, hand);
        if (purpleIdx != -1)
            return encode(1, purpleIdx);

        int blueIdx = findBuildableCardByColor(bot, Card.Color.BLUE, hand);
        if (blueIdx != -1)
            return encode(1, blueIdx);

        int anyIdx = findAnyBuildableCard(bot, hand);
        if (anyIdx != -1 && canBuildWonderStage(bot)) {
            return encode(2, anyIdx);
        }

        if (anyIdx != -1)
            return encode(1, anyIdx);

        return encode(0, 0);
    }

    // --- Helper Methods ---

    private int getResourceCount(Bot bot) {
        // Access via the new Player facade -> Board component
        Map<String, Integer> board = bot.getBoardElement();
        int brown = board.getOrDefault("BROWN", 0);
        int grey = board.getOrDefault("GREY", 0);
        return brown + grey;
    }

    private int getScienceSymbolCount(Bot bot) {
        // Access via the new Player facade -> Science component
        return bot.getCompass() + bot.getTablet() + bot.getWheel();
    }

    private boolean isLargelyAheadMilitarily(Bot bot) {
        return bot.getMilitaryStrength() >= getMaxNeighborMilitary(bot) + 1;
    }

    private int getMaxNeighborMilitary(Bot bot) {
        Player left = bot.getLeft();
        Player right = bot.getRight();
        int leftStr = (left != null) ? left.getMilitaryStrength() : 0;
        int rightStr = (right != null) ? right.getMilitaryStrength() : 0;
        return Math.max(leftStr, rightStr);
    }

    private int findBuildableCardByColor(Bot bot, Card.Color color, List<Card> hand) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            // Use bot.canBuild() from facade
            if (c != null && c.getColor() == color && bot.canBuild(c).isPresent()) {
                return i;
            }
        }
        return -1;
    }

    private int findAnyBuildableCard(Bot bot, List<Card> hand) {
        for (int i = 0; i < hand.size(); i++) {
            Card c = hand.get(i);
            if (c != null && bot.canBuild(c).isPresent()) {
                return i;
            }
        }
        return -1;
    }

    private boolean canBuildWonderStage(Bot bot) {
        Wonder w = bot.getWonder();
        if (w == null || w.isCompleted())
            return false;

        WonderStage stage = w.getCurrentStage();
        if (stage == null)
            return false;

        Optional<Cost> cost = bot.canBuild(stage.getCosts());
        return cost.isPresent();
    }

    private int encode(int action, int index) {
        return action * 10 + index;
    }

    @Override
    public String getName() {
        return "Mar";
    }
}
