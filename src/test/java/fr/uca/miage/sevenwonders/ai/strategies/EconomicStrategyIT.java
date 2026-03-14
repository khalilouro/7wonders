package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Test for EconomicStrategy.
 * <p>
 * This test verifies the bot's decision-making logic using real domain objects.
 * It checks prioritization of Gold cards, Gold effects, Wonder construction,
 * and cost penalties.
 * </p>
 */
class EconomicStrategyIT {

    private EconomicStrategy strategy;
    private Bank bank;

    // Action constants matching EconomicStrategy
    private static final int ACTION_DISCARD = 0;
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    @BeforeEach
    void setUp() {
        strategy = new EconomicStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testStrategyReturnsNegativeOneForEmptyHand() {
        Bot bot = new Bot("TestBot", strategy);
        bot.getHand().clear();

        int result = strategy.applyStrategy(bot, bank);

        assertEquals(-1, result, "Strategy should return -1 when the hand is empty.");
    }

    @Test
    void testPrioritizeGoldenCardOverRegularCard() {
        // Setup: Bot has a Blue card and a Golden card. Both are free.
        // Blue Score: 50
        // Golden Score: 100
        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0), new Effect.VictoryPoints(2));
        Card goldCard = createCard("Market", Card.Color.GOLDEN, new Cost.Gold(0), new Effect.Gold(1));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(goldCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1
        int expected = ACTION_BUILD * 10 + 1;
        assertEquals(expected, result, "Should prioritize building the Golden card (Score 100) over Blue (Score 50)");
    }

    @Test
    void testPrioritizeCardWithGoldEffect() {
        // Setup: Bot has a Blue card and a Grey card that gives Gold.
        // Blue Score: 50
        // Grey w/ Gold Score: 100 + 5 = 105
        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0), new Effect.VictoryPoints(2));
        Card richCard = createCard("RichGrey", Card.Color.GREY, new Cost.Gold(0), new Effect.Gold(5));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(richCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1
        int expected = ACTION_BUILD * 10 + 1;
        assertEquals(expected, result, "Should prioritize card with Gold Effect (Score 105) over Blue (Score 50)");
    }

    @Test
    void testPrioritizeWonderOverRegularCard() {
        // Setup: Bot has a Blue card at Index 0.
        // Strategy only checks Wonder for the card at Index 0.
        // Blue Score: 50
        // Wonder Score: 60
        Bot bot = createBotWithWonder();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0), new Effect.VictoryPoints(2));
        bot.getHand().add(blueCard);

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Wonder (2) at Index 0
        int expected = ACTION_WONDER * 10 + 0;
        assertEquals(expected, result, "Should prioritize Wonder (Score 60) over regular Blue card (Score 50)");
    }

    @Test
    void testPrioritizeGoldenCardOverWonder() {
        // Setup: Bot has a Golden card at Index 0.
        // Golden Score: 100
        // Wonder Score: 60
        Bot bot = createBotWithWonder();

        Card goldCard = createCard("Market", Card.Color.GOLDEN, new Cost.Gold(0), new Effect.Gold(1));
        bot.getHand().add(goldCard);

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 0
        int expected = ACTION_BUILD * 10 + 0;
        assertEquals(expected, result, "Should prioritize Golden card (Score 100) over Wonder (Score 60)");
    }

    @Test
    void testCostPenaltyAffectsDecision() {
        // Setup: Two Blue cards.
        // Card 0: Cost 0 -> Score 50
        // Card 1: Cost 4 -> Score 50 - (4*5) = 30
        Bot bot = createBot();
        // Give bot plenty of gold so it *can* build the expensive one, but chooses not
        // to.
        bot.getResources().setGold(20);

        Card cheapCard = createCard("Cheap", Card.Color.BLUE, new Cost.Gold(0), new Effect.VictoryPoints(2));
        Card expensiveCard = createCard("Expensive", Card.Color.BLUE, new Cost.Gold(4), new Effect.VictoryPoints(5));

        bot.getHand().add(cheapCard); // Index 0
        bot.getHand().add(expensiveCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 0 (Cheap)
        int expected = ACTION_BUILD * 10 + 0;
        assertEquals(expected, result, "Should prioritize the cheaper card due to cost penalty");
    }

    @Test
    void testDiscardIfNothingAffordable() {
        // Setup: Bot has 0 Gold. Card costs 10 Gold.
        // canBuild should return empty.
        Bot bot = createBot();
        bot.getResources().setGold(0);

        Card expensiveCard = createCard("Palace", Card.Color.BLUE, new Cost.Gold(10), new Effect.VictoryPoints(10));
        bot.getHand().add(expensiveCard);

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Discard (0) at Index 0 (Fallback behavior)
        int expected = ACTION_DISCARD * 10 + 0;
        assertEquals(expected, result, "Should discard card if it cannot be afforded");
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Bot createBot() {
        Bot bot = new Bot("TestBot", strategy);
        // Ensure hand is initialized and empty
        bot.getHand().clear();
        // Give some default gold to avoid 'cannot afford' issues in standard tests
        bot.getResources().setGold(10);
        return bot;
    }

    private Bot createBotWithWonder() {
        Bot bot = createBot();

        WonderStage stage1 = new WonderStage(new Cost.Gold(0), new Effect[]{new Effect.VictoryPoints(3)});
        WonderStage[] stages = new WonderStage[]{stage1};

        // Create Wonder (using Side A)
        Wonder wonder = new Wonder("Giza", Card.Materials.STONE, stages, Wonder.Side.A);

        bot.setWonderplayer(wonder);
        return bot;
    }

    private Card createCard(String name, Card.Color color, Cost cost, Effect effect) {
        // Using Card constructor: Name, Cost, Age, Color, Effect, Parents, Children
        return new Card(name, cost, Card.Age.AGE_I, color, effect, new String[]{}, // No parents
                new String[]{} // No children
        );
    }
}
