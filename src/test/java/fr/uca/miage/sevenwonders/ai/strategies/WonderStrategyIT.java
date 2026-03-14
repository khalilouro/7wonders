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
 * Integration Test for WonderStrategy.
 * <p>
 * Verifies the specific priorities: 1. Build Wonder Stage (if affordable). 2.
 * Build Resource Card (if needed for next stage). 3. Sell Card (if coins needed
 * for next stage). 4. Build High Value Card (Blue > Green > Red...).
 * </p>
 */
class WonderStrategyIT {

    private WonderStrategy strategy;
    private Bank bank;

    // Action Constants
    private static final int ACTION_SELL = 0;
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    @BeforeEach
    void setUp() {
        strategy = new WonderStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testPriority1_BuildWonderStage_WhenAffordable() {
        // Setup: Wonder Stage is free (Cost 0).
        // Hand: Random cards.
        Bot bot = createBotWithAffordableWonder();

        // Add a card that is typically "best" to bury (e.g., one we can't build or just
        // generic)
        Card card = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));
        bot.getHand().add(card); // Index 0

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action WONDER (2) using Index 0
        // "1. First priority: build the next wonder stage if possible"
        assertEquals(encode(ACTION_WONDER, 0), result, "Should prioritize building the Wonder stage when affordable");
    }

    @Test
    void testPriority2_BuildNeededResource_WhenWonderRequiresIt() {
        // Setup: Wonder requires WOOD. Bot has no resources.
        // Hand: LumberYard (WOOD) and Altar (Blue).

        Bot bot = createBotWithWonderRequiringWood();

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));

        // Create Resource Card (Wood)
        // Using Effect.Production.Fixed/Choice based on Effect implementation
        Card woodCard = createCard("LumberYard", Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.WOOD}));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(woodCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Wood Card)
        // "2. Second priority: build cards that provide resources needed"
        assertEquals(encode(ACTION_BUILD, 1), result,
                "Should prioritize building the resource card needed for the Wonder");
    }

    @Test
    void testPriority3_SellCard_WhenCoinsNeededForWonder() {
        // Setup: Wonder requires 10 Gold. Bot has 0 Gold.
        // Hand: Random cards. Bot cannot build Wonder (lacks gold), has no resource
        // cards to fix it.
        // Strategy should decide to sell to get coins.

        Bot bot = createBotWithWonderRequiringGold();
        bot.getResources().setGold(0); // Ensure poverty

        Card card = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));
        bot.getHand().add(card); // Index 0

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action SELL (0) on Index 0
        // "3. Third priority: sell card if coins needed"
        assertEquals(encode(ACTION_SELL, 0), result, "Should sell card to accumulate coins for the Wonder cost");
    }

    @Test
    void testPriority4_BuildHighValueCard_WhenWonderBlockedOrDone() {
        // Setup: Wonder is IMPOSSIBLE to build (requires resource not in hand, e.g.,
        // GLASS).
        // Hand has Blue, Red, Green cards.
        // Strategy should fallback to "High Value": Blue > Green > Red > Golden.

        Bot bot = createBotWithImpossibleWonder();

        Card redCard = createCard("Barracks", Card.Color.RED, new Cost.Gold(0));
        Card blueCard = createCard("Altar", Card.Color.BLUE, new Cost.Gold(0));

        bot.getHand().add(redCard); // Index 0
        bot.getHand().add(blueCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Blue)
        // "findBestCardToBuild... priorities = {BLUE, GREEN, RED...}"
        assertEquals(encode(ACTION_BUILD, 1), result,
                "Should fallback to building Blue card when Wonder logic is exhausted");
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private Bot createBotWithAffordableWonder() {
        Bot bot = createBot();
        // Free stage
        WonderStage stage = new WonderStage(new Cost.Gold(0), new Effect[]{new Effect.VictoryPoints(3)});
        Wonder wonder = new Wonder("FreeWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBotWithWonderRequiringWood() {
        Bot bot = createBot();
        // Stage requires WOOD
        Cost cost = new Cost.Materials(new Card.Materials[]{Card.Materials.WOOD});
        WonderStage stage = new WonderStage(cost, new Effect[]{new Effect.VictoryPoints(3)});

        Wonder wonder = new Wonder("WoodWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBotWithWonderRequiringGold() {
        Bot bot = createBot();
        // Stage requires 10 Gold
        Cost cost = new Cost.Gold(10);
        WonderStage stage = new WonderStage(cost, new Effect[]{new Effect.VictoryPoints(3)});

        Wonder wonder = new Wonder("GoldWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBotWithImpossibleWonder() {
        Bot bot = createBot();
        // Stage requires GLASS (Bot has none, hand has none)
        Cost cost = new Cost.Materials(new Card.Materials[]{Card.Materials.GLASS});
        WonderStage stage = new WonderStage(cost, new Effect[]{new Effect.VictoryPoints(3)});

        Wonder wonder = new Wonder("GlassWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBot() {
        Bot bot = new Bot("WonderBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20);
        return bot;
    }

    private Card createCard(String name, Card.Color color, Cost cost) {
        return createCard(name, color, cost, new Effect.VictoryPoints(1));
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
