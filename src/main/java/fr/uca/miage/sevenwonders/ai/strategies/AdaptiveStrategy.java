package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.*;

import java.util.List;

/**
 * A dynamic, state-aware strategy that adapts to the game flow.
 * <p>
 * This strategy evaluates every possible move (Build, Wonder, Discard) based on
 * a comprehensive scoring system that considers:
 * <ul>
 * <li><b>Age Context:</b> Prioritizes resources in Age 1, points/military in
 * Age 2, and max points in Age 3.</li>
 * <li><b>Needs Analysis:</b> Checks if the bot is starving for resources or
 * gold.</li>
 * <li><b>Neighbor Analysis:</b> Reacts to neighbors' military strength and
 * science progress.</li>
 * <li><b>Wonder Progress:</b> Prioritizes wonder stages when they offer
 * significant advantages.</li>
 * </ul>
 * </p>
 */
public class AdaptiveStrategy implements Strategy {

    private final MilitaryStrategy militaryStrategy;
    private final ScienceStrategy scienceStrategy;
    private final EconomicStrategy economicStrategy;
    private final WonderStrategy wonderStrategy;
    private final BlueStrategy blueStrategy;
    private final Strategy randomStrategy;

    public AdaptiveStrategy() {
        this.militaryStrategy = new MilitaryStrategy();
        this.scienceStrategy = new ScienceStrategy();
        this.economicStrategy = new EconomicStrategy();
        this.wonderStrategy = new WonderStrategy();
        this.blueStrategy = new BlueStrategy();
        this.randomStrategy = new RandomStrategy();
    }

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        if (hand.isEmpty()) {
            return -1;
        }

        // 1. Wonder Strategy: Check if we should focus on the wonder
        // If we can build a stage and it's a good move (WonderStrategy handles this
        // priority internally somewhat,
        // but we want to see if it recommends a build action).
        int wonderMove = wonderStrategy.applyStrategy(bot, bank);
        if (isWonderBuildAction(wonderMove)) {
            return wonderMove;
        }

        // 2. Military Strategy: Check if we are threatened or can dominate
        // If we are losing to neighbors, prioritize military.
        if (shouldFocusOnMilitary(bot)) {
            int militaryMove = militaryStrategy.applyStrategy(bot, bank);
            // Only take it if it's actually a build action (not a fallback discard/random)
            if (isBuildAction(militaryMove)) {
                return militaryMove;
            }
        }

        // 3. Economic Strategy: If we are poor
        if (bot.getGoldPoints() < 3) {
            int economicMove = economicStrategy.applyStrategy(bot, bank);
            // Economic strategy might return discard (0) which is fine if we are poor
            return economicMove;
        }

        // 4. Science Strategy: If we have started science or it's early
        if (shouldFocusOnScience(bot)) {
            int scienceMove = scienceStrategy.applyStrategy(bot, bank);
            if (isBuildAction(scienceMove)) {
                return scienceMove;
            }
        }

        // 5. Default / Points: Blue Strategy (Civilian)
        // Try to get points if nothing else is pressing
        int blueMove = blueStrategy.applyStrategy(bot, bank);
        if (isBuildAction(blueMove)) {
            return blueMove;
        }

        // 6. Fallback: If Blue didn't find a build, maybe Economic can find a better
        // move (like a yellow card)
        // or just use Random/Economic as final fallback.
        int economicFallback = economicStrategy.applyStrategy(bot, bank);
        if (economicFallback != -1) {
            return economicFallback;
        }

        return randomStrategy.applyStrategy(bot, bank);
    }

    private boolean isWonderBuildAction(int move) {
        return move / 10 == 2;
    }

    private boolean isBuildAction(int move) {
        return move / 10 == 1;
    }

    private boolean shouldFocusOnMilitary(Bot bot) {
        int myStrength = bot.getMilitaryStrength();
        int leftStrength = (bot.getLeft() != null) ? bot.getLeft().getMilitaryStrength() : 0;
        int rightStrength = (bot.getRight() != null) ? bot.getRight().getMilitaryStrength() : 0;
        // If we are weaker than any neighbor, or equal (to try to win)
        return myStrength <= leftStrength || myStrength <= rightStrength;
    }

    private boolean shouldFocusOnScience(Bot bot) {
        int scienceSymbols = bot.getTablet() + bot.getCompass() + bot.getWheel();
        // Focus on science if we already have some, or if it's Age 1 (start early)
        // We can check age via hand (assuming all cards in hand are same age)
        boolean isAge1 = !bot.getHand().isEmpty() && bot.getHand().get(0).getAge() == Card.Age.AGE_I;
        return scienceSymbols > 0 || isAge1;
    }

    @Override
    public String getName() {
        return "Adaptive";
    }
}
