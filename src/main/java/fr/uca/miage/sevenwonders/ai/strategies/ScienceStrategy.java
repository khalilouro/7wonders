package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.ai.Strategy;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;

import fr.uca.miage.sevenwonders.services.ScoreCalculator;

import java.util.List;
import java.util.Optional;

/**
 * Strategy focused on scientific development.
 * <p>
 * The bot always tries to build a green (science) card first.
 * </p>
 */
public class ScienceStrategy implements Strategy {

    /** Fallback strategy used only when needed (rare cases). */
    private final Strategy fallback = new RandomStrategy();

    /**
     * Applies the scientific strategy.
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        // Access hand via the facade
        List<Card> hand = bot.getHand();

        if (hand.isEmpty()) {
            return -1;
        }

        int bestCardIndex = -1;
        int maxScienceScore = -1;

        // 1) Find the best buildable science card
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            // Check if it is a science card and if it is buildable
            if (isScienceCard(card) && bot.canBuild(card).isPresent()) {
                Optional<Effect.Science.ScienceSymbol> symbolOpt = card.getEffect().getScienceSymbol();
                if (symbolOpt.isPresent()) {
                    int potentialScore = calculatePotentialScore(bot, symbolOpt.get());

                    if (potentialScore > maxScienceScore) {
                        maxScienceScore = potentialScore;
                        bestCardIndex = i;
                    }
                }
            }
        }

        if (bestCardIndex != -1) {
            return 1 * 10 + bestCardIndex; // Build the best science card
        }

        // 2) Buildable non-science card
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            if (!isScienceCard(card) && bot.canBuild(card).isPresent()) {
                return 1 * 10 + i; // build
            }
        }

        // 3) Nothing is buildable → discard the first card
        return 0; // action = 0 (discard), cardIndex = 0
    }

    private int calculatePotentialScore(Bot bot, Effect.Science.ScienceSymbol symbol) {
        // Access science stats via the Science component
        int tablet = bot.getScience().getTablet();
        int compass = bot.getScience().getCompass();
        int wheel = bot.getScience().getWheel();
        int any = bot.getScience().getAnyScience();

        switch (symbol) {
            case TABLET -> tablet++;
            case COMPASS -> compass++;
            case WHEEL -> wheel++;
            case ANY -> any++;
        }

        // Delegate calculation to the Service
        return ScoreCalculator.calculateScienceScore(tablet, compass, wheel, any);
    }

    /**
     * Checks whether a card belongs to the science category.
     */
    private boolean isScienceCard(Card card) {
        return card.getColor() == Card.Color.GREEN;
    }

    @Override
    public String getName() {
        return "Science";
    }
}
