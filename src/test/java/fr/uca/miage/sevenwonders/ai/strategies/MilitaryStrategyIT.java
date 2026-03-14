package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration Test for MilitaryStrategy.
 * <p>
 * Verifies that the bot prioritizes building Red (Military) cards when they are
 * affordable. Uses real domain objects (Bot, Card, Bank).
 * </p>
 */
class MilitaryStrategyIT {

    private MilitaryStrategy strategy;
    private Bank bank;

    // Action constant for Build (as used in MilitaryStrategy returns: 1 * 10 +
    // index)
    private static final int ACTION_BUILD = 1;

    @BeforeEach
    void setUp() {
        strategy = new MilitaryStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testPrioritizeBuildableMilitaryCard() {
        // Setup: Bot has a Blue card (Index 0) and a Red card (Index 1).
        // Both are free/affordable.
        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));
        Card redCard = createCard("Barracks", Card.Color.RED, new Cost.Gold(0));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(redCard); // Index 1

        // Strategy logic: Iterate hand. If Red && CanBuild -> Return Build(1) + Index.
        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1
        int expected = ACTION_BUILD * 10 + 1;
        assertEquals(expected, result, "Should prioritize the Red (Military) card");
    }

    @Test
    void testPickFirstBuildableMilitaryCard() {
        // Setup: Bot has two Red cards. Both affordable.
        // Logic iterates 0..N. Should pick the first one found.
        Bot bot = createBot();

        Card redCard1 = createCard("Stockade", Card.Color.RED, new Cost.Gold(0));
        Card redCard2 = createCard("GuardTower", Card.Color.RED, new Cost.Gold(0));

        bot.getHand().add(redCard1); // Index 0
        bot.getHand().add(redCard2); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 0
        int expected = ACTION_BUILD * 10 + 0;
        assertEquals(expected, result, "Should pick the first buildable Military card found in the hand");
    }

    @Test
    void testSkipUnbuildableMilitaryCard() {
        // Setup: Bot has a Red card (Index 0) that is too expensive.
        // Bot has a Blue card (Index 1) that is affordable.
        Bot bot = createBot();
        bot.getResources().setGold(0); // Bot has 0 Gold

        Card expensiveRed = createCard("Fortification", Card.Color.RED, new Cost.Gold(10)); // Unaffordable
        Card cheapBlue = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0)); // Affordable

        bot.getHand().add(expensiveRed); // Index 0
        bot.getHand().add(cheapBlue); // Index 1

        // The strategy checks 'isMilitaryCard(card) && bot.canBuild(card).isPresent()'.
        // Index 0 fails 'canBuild'.
        // The loop continues. Index 1 is not Red.
        // It falls back to RandomStrategy.

        int result = strategy.applyStrategy(bot, bank);

        // We cannot predict exactly what RandomStrategy does (it's random),
        // but we know MilitaryStrategy should NOT have returned 'Build Index 0'.
        int avoidAction = ACTION_BUILD * 10 + 0;

        assertNotEquals(avoidAction, result, "Should NOT try to build the Red card if it is unbuildable");

        // Ensure it returns a valid action (not -1 for non-empty hand)
        assertTrue(result >= 0, "Should return a valid action from fallback strategy");
    }

    @Test
    void testFallbackIfNoMilitaryCard() {
        // Setup: Hand has only Blue and Green cards. No Red cards.
        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));
        Card greenCard = createCard("Scriptorium", Card.Color.GREEN, new Cost.Gold(0));

        bot.getHand().add(blueCard);
        bot.getHand().add(greenCard);

        int result = strategy.applyStrategy(bot, bank);

        // Strategy loop finishes without finding Red. Calls fallback.
        // Just assert a valid result is returned.
        assertTrue(result >= 0,
                "Should fall back to RandomStrategy (returning valid int) when no Military card exists");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Bot createBot() {
        Bot bot = new Bot("TestBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(10); // Default gold
        return bot;
    }

    private Card createCard(String name, Card.Color color, Cost cost) {
        // Using standard Card constructor based on project structure
        return new Card(name, cost, Card.Age.AGE_I, color, new Effect.VictoryPoints(1), // Dummy effect
                new String[]{}, new String[]{});
    }
}
