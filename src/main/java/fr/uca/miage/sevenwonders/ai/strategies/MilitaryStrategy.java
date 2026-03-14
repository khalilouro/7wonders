package fr.uca.miage.sevenwonders.ai.strategies;

import java.util.List;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.Card;

/**
 * Strategy focused on military strength.
 * <p>
 * This bot plays in a very straightforward way: it always tries to build a red
 * (military) card first. The idea is simple — if the bot can increase its
 * military power during the Age, it will do so immediately.
 * </p>
 *
 * <p>
 * If no military card is available or buildable, the strategy falls back to a
 * {@link RandomStrategy} so the bot can still make a valid move instead of
 * doing nothing. This keeps the behaviour consistent with the rest of the game.
 * </p>
 */
public class MilitaryStrategy implements Strategy {

    /** Fallback strategy used when no military option is possible. */
    private final Strategy fallback = new RandomStrategy();

    /**
     * @param bot
     *            the bot whose turn is being played
     * @param bank
     *            the game's bank, used for transactions or resources
     * @return an integer encoding both the chosen action and the selected card
     *         index: {@code action * 10 + cardIndex}
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();

        if (hand.isEmpty()) {
            return -1;
        }

        // Look for a buildable military card
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            if (isMilitaryCard(card) && bot.canBuild(card).isPresent()) {
                // Play/build the card (action = 1)
                return 1 * 10 + i;
            }
        }

        // No valid military card found → use fallback strategy
        return fallback.applyStrategy(bot, bank);
    }

    /**
     * This helper makes the code easier to read and keeps the logic in one place.
     *
     * @param card
     *            the card to check
     * @return true if the card is red, false otherwise
     */
    private boolean isMilitaryCard(Card card) {
        return card.getColor() == Card.Color.RED;
    }

    @Override
    public String getName() {
        return "Military";
    }
}
