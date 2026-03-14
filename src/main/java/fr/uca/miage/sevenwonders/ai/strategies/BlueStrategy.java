package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.*;

import java.util.List;

/**
 * A strategy focused on maximizing Victory Points by prioritizing Civilian
 * (Blue) Structures.
 * <p>
 * This strategy follows a specific hierarchy of decisions to maximize the
 * player's score: Primary Goal is to build the buildable Blue card that offers
 * the highest immediate Victory Points. Fallback is to discard a card to gain
 * coins.
 * </p>
 */
public class BlueStrategy implements Strategy {

    /**
     * Executes the strategy logic to determine the best card and action for the
     * current turn.
     *
     * @param bot
     *            The bot instance executing this strategy.
     * @param bank
     *            The bank instance (unused directly in logic but required by
     *            interface).
     * @return An integer encoding the action (0=discard, 1=build, 2=wonder) and the
     *         card index.
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();

        int bestBlueIndex = -1;
        int maxPoints = -1;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card == null)
                continue;

            if (card.getColor() == Card.Color.BLUE && bot.canBuild(card).isPresent()) {

                int currentPoints = 0;

                if (card.getEffect()instanceof Effect.VictoryPoints vpEffect) {
                    currentPoints = vpEffect.points();
                }

                if (currentPoints > maxPoints) {
                    maxPoints = currentPoints;
                    bestBlueIndex = i;
                }
            }
        }

        if (bestBlueIndex != -1) {
            return encode(1, bestBlueIndex);
        }

        return encode(0, 0);
    }

    /**
     * Encodes the action and card index into a single integer for the Session to
     * interpret.
     *
     * @param action
     *            The action type: 0 for discard, 1 for build structure, 2 for build
     *            wonder.
     * @param index
     *            The index of the card in the hand to be used/discarded.
     * @return The encoded integer value (action * 10 + index).
     */
    private int encode(int action, int index) {
        return action * 10 + index;
    }

    @Override
    public String getName() {
        return "Blue";
    }
}
