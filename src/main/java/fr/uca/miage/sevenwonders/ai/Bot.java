package fr.uca.miage.sevenwonders.ai;

import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.Player;

import java.util.List;
import java.util.Map;

/**
 * Represents an automated player (bot) in the Seven Wonders game.
 * <p>
 * A bot extends the {@link Player} class and uses a specific {@link Strategy}
 * to determine which card to play and what action to take during its turn.
 * </p>
 */
import fr.uca.miage.sevenwonders.utils.Config;

// ...
public class Bot extends Player implements Strategy {

    /** The strategy used by this bot to decide its actions. */
    private Strategy strategy;

    /**
     * Creates a new bot with a given name and playing strategy.
     *
     * @param name
     *            the name of the bot
     * @param strategy
     *            the strategy that defines how the bot plays
     */
    public Bot(String name, Strategy strategy) {
        super(name);
        this.strategy = strategy;
    }

    /**
     * Executes the bot’s current strategy to determine its next move.
     *
     * @param bot
     *            the bot performing the action (usually {@code this})
     * @param bank
     *            the game's bank, used for transactions or resource checks
     * @return an integer encoding both the chosen action and the selected card
     *         index, as defined by the active strategy
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        if (strategy == null)
            throw new NullPointerException("Strategy cannot be null");

        return strategy.applyStrategy(this, bank); // Call strategy once and return result
    }

    /**
     * Updates the bot’s strategy dynamically during the game.
     *
     * @param newStrategy
     *            the new strategy to assign to this bot
     */
    public void setStrategy(Strategy newStrategy) {
        this.strategy = newStrategy;
    }

    public void chooseAndPlayFromDiscard(Session session, boolean verbose) {
        // Access method from Player facade
        if (!canPlayFromDiscard() || session.getDiscardPile().isEmpty()) {
            return;
        }

        List<Card> discardPile = session.getDiscardPile();
        Card bestCard = null;
        int maxVp = -1;

        // Find the card with the most victory points
        for (Card card : discardPile) {
            if (card.getEffect()instanceof Effect.VictoryPoints pointsEffect) {
                if (pointsEffect.points() > maxVp) {
                    maxVp = pointsEffect.points();
                    bestCard = card;
                }
            }
        }

        // If no card with victory points is found, pick the first one.
        if (bestCard == null && !discardPile.isEmpty()) {
            bestCard = discardPile.get(0);
        }

        // Build the card for free
        if (bestCard != null) {
            if (verbose) {
                String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());
                String reset = Config.getInstance().getValueANSI("reset");
                System.out.println(accent + getName() + " plays " + bestCard.getName()
                        + " from the discard pile for free." + reset);
            }

            // Update board elements
            // FIX: Access the map via the Board component if getBoardElement() is missing
            // on Player
            Map<String, Integer> boardElements;
            try {
                // Try direct getter first (Facade)
                boardElements = this.getBoardElement();
            } catch (Error | Exception e) {
                // Fallback to component access
                boardElements = this.getBoard().getBoardElements();
            }

            if (boardElements != null) {
                String colorKey = bestCard.getColor().toString();
                boardElements.put(colorKey, boardElements.getOrDefault(colorKey, 0) + 1);
            }

            // Add to built cards and apply effect
            addAlreadyBuilt(bestCard);
            bestCard.getEffect().apply(this);

            // Update session state
            session.removeFromDiscardPile(bestCard);
            usePlayFromDiscard();
        }
    }
}
