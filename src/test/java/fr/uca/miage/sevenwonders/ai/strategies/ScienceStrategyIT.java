package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Test for ScienceStrategy.
 * <p>
 * Verifies that the bot: 1. Prioritizes Green (Science) cards over others. 2.
 * Chooses the specific Science card that maximizes the potential score (e.g.,
 * completing sets). 3. Falls back to building any card if no Science card is
 * available. 4. Discards if nothing is buildable.
 * </p>
 */
class ScienceStrategyIT {

    private ScienceStrategy strategy;
    private Bank bank;

    // Action Constants
    private static final int ACTION_DISCARD = 0;
    private static final int ACTION_BUILD = 1;

    @BeforeEach
    void setUp() {
        strategy = new ScienceStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testPrioritizeGreenCardOverOthers() {
        // Setup: Hand has a Blue card (Index 0) and a Green card (Index 1).
        // Both are free/affordable.
        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Effect.VictoryPoints(2));

        // Green Card: Tablet
        Card greenCard = createCard("Scriptorium", Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.TABLET));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(greenCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1 (Green)
        // "Check if it is a science card... if potentialScore > maxScienceScore"
        assertEquals(encode(ACTION_BUILD, 1), result, "Should prioritize Green (Science) card over Blue");
    }

    @Test
    void testMaximizeScienceScore_ChooseSetOverDuplicate() {
        // Setup: Bot already has 1 Tablet and 1 Compass.
        // Hand has:
        // 0. Tablet (Would make 2 Tablets, 1 Compass -> Score: 4 + 1 = 5)
        // 1. Wheel (Would make 1 Tablet, 1 Compass, 1 Wheel -> Score: 1 + 1 + 1 + 7 =
        // 10)

        Bot bot = createBot();
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.TABLET);
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.COMPASS);

        Card tabletCard = createCard("School", Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.TABLET));

        Card wheelCard = createCard("Laboratory", Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.WHEEL));

        bot.getHand().add(tabletCard); // Index 0
        bot.getHand().add(wheelCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1 (Wheel) because 10 > 5.
        // "if (potentialScore > maxScienceScore) ... bestCardIndex = i"
        assertEquals(encode(ACTION_BUILD, 1), result, "Should choose the symbol that completes a set (max score)");
    }

    @Test
    void testFallbackToNonScienceIfNoScienceAvailable() {
        // Setup: Hand has only Blue and Red cards. No Green.
        // Logic: "Buildable non-science card"

        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Effect.VictoryPoints(2));
        Card redCard = createCard("Barracks", Card.Color.RED, new Effect.Military(1));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(redCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 0.
        // Strategy iterates 0..N for non-science cards and picks the first one.
        // "return 1 * 10 + i; // build"
        assertEquals(encode(ACTION_BUILD, 0), result, "Should fallback to first buildable non-science card");
    }

    @Test
    void testDiscardIfNothingBuildable() {
        // Setup: Hand contains expensive cards. Bot has 0 gold.

        Bot bot = createBot();
        bot.getResources().setGold(0);

        Card expensiveCard = createCard("Palace", Card.Color.BLUE, new Cost.Gold(100), new Effect.VictoryPoints(10));
        bot.getHand().add(expensiveCard); // Index 0

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Discard (0) at Index 0
        // "return 0; // action = 0 (discard)"
        assertEquals(encode(ACTION_DISCARD, 0), result, "Should discard if no card is buildable");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Bot createBot() {
        Bot bot = new Bot("ScienceBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20); // Default gold to avoid cost issues
        return bot;
    }

    private Card createCard(String name, Card.Color color, Effect effect) {
        return createCard(name, color, new Cost.Gold(0), effect);
    }

    private Card createCard(String name, Card.Color color, Cost cost, Effect effect) {
        return new Card(name, cost, Card.Age.AGE_I, color, effect, new String[]{}, new String[]{});
    }

    private int encode(int action, int index) {
        return action * 10 + index;
    }
}
