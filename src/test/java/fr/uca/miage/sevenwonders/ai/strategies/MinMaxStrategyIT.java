package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Test for MinMaxStrategy.
 * <p>
 * This test verifies the complex decision-making logic of the MinMax strategy
 * by setting up specific game states (Military conflicts, Science scoring,
 * Resource needs) and checking if the bot maximizes its heuristic score.
 * </p>
 */
class MinMaxStrategyIT {

    private MinMaxStrategy strategy;
    private Bank bank;

    // Action Constants: 0=Sell, 1=Build, 2=Wonder
    private static final int ACTION_SELL = 0;
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    @BeforeEach
    void setUp() {
        strategy = new MinMaxStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testPrioritizeScienceWhenItMaximizesScore() {
        // Setup: Bot has two cards:
        // 1. Blue Card (2 VP)
        // 2. Green Card (Science) -> High heuristic growth

        Bot bot = createBotWithUnbuildableWonder();

        // Add existing science symbols (Tablet & Compass)
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.TABLET);
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.COMPASS);

        Card blueCard = createCard("Altar", Card.Color.BLUE, new Effect.VictoryPoints(2));
        Card greenCard = createCard("Scriptorium", Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.TABLET));

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(greenCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1 (Green)
        assertEquals(encode(ACTION_BUILD, 1), result, "Should prioritize Science card due to high heuristic growth");
    }

    @Test
    void testPrioritizeMilitaryWhenCatchingUp() {
        // Setup: Bot is weaker than neighbor.

        Bot bot = createBotWithUnbuildableWonder();

        Player leftNeighbor = new Player("Left");
        leftNeighbor.getMilitary().addStrength(1);

        bot.setNeighborhood(leftNeighbor, new Player("Right"));

        Card blueCard = createCard("Statue", Card.Color.BLUE, new Effect.VictoryPoints(2));
        Card redCard = createCard("Barracks", Card.Color.RED, new Effect.Military(2)); // +2 Shields

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(redCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1 (Red)
        assertEquals(encode(ACTION_BUILD, 1), result, "Should prioritize Military to overtake neighbor");
    }

    @Test
    void testPrioritizeResourceNeededForWonder() {
        // Setup: Bot needs Wood for Wonder Stage.
        // Hand: Wood Card vs Clay Card.

        Bot bot = createBotWithWonderContainingWoodCost();

        // Using Effect.Production.Choice to match List structure
        Card clayCard = createCard("ClayPit", Card.Color.BROWN,
                new Effect.Production.Choice(List.of(List.of(Card.Materials.CLAY))));

        Card woodCard = createCard("LumberYard", Card.Color.BROWN,
                new Effect.Production.Choice(List.of(List.of(Card.Materials.WOOD))));

        bot.getHand().add(clayCard); // Index 0
        bot.getHand().add(woodCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Build (1) at Index 1 (Wood)
        assertEquals(encode(ACTION_BUILD, 1), result, "Should prioritize resource needed for next Wonder stage");
    }

    @Test
    void testDenialStrategy() {
        // Setup: Bot has a card that is valuable to neighbor (Science) but not self.

        Bot bot = createBotWithUnbuildableWonder();

        Player leftNeighbor = new Player("Left");
        leftNeighbor.getScience().addSymbol(Effect.Science.ScienceSymbol.TABLET);
        bot.setNeighborhood(leftNeighbor, new Player("Right"));

        Card scienceCard = createCard("Scriptorium", Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.TABLET));
        Card vpCard = createCard("Altar", Card.Color.BLUE, new Effect.VictoryPoints(2));

        bot.getHand().add(vpCard); // Index 0
        bot.getHand().add(scienceCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Verify valid action (Build)
        assertEquals(ACTION_BUILD, result / 10, "Strategy should choose to build a card");
    }

    @Test
    void testWonderConstructionWhenCardIsWeak() {
        // Setup: Hand contains weak cards. Wonder gives High VP.
        Bot bot = createBotWithHighValueWonderStage();

        Card weakCard = createCard("Weak", Card.Color.BLUE, new Effect.VictoryPoints(1));
        bot.getHand().add(weakCard); // Index 0

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Wonder (2) using Index 0
        assertEquals(encode(ACTION_WONDER, 0), result, "Should build Wonder when stage value exceeds card value");
    }

    @Test
    void testSellIfNothingBuildable() {
        // Setup: Hand contains a card that is too expensive. Bot has 0 gold.

        Bot bot = createBotWithUnbuildableWonder();
        bot.getResources().setGold(0);

        Card expensiveCard = createCard("Palace", Card.Color.BLUE, new Cost.Gold(100), new Effect.VictoryPoints(10));
        bot.getHand().add(expensiveCard);

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Sell (0) at Index 0
        assertEquals(encode(ACTION_SELL, 0), result, "Should sell card if it cannot be built");
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    /**
     * Creates a bot with a Wonder that is impossibly expensive to build. This
     * ensures the bot will focus on Card actions (Build/Sell) in tests.
     */
    private Bot createBotWithUnbuildableWonder() {
        Bot bot = new Bot("MinMaxBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20);

        // Wonder requires materials the bot doesn't have (GLASS)
        Cost cost = new Cost.Materials(new Card.Materials[]{Card.Materials.GLASS});
        WonderStage stage = new WonderStage(cost, new Effect[]{new Effect.VictoryPoints(10)});
        Wonder wonder = new Wonder("Unbuildable", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);

        bot.setWonderplayer(wonder);
        return bot;
    }

    /**
     * Creates a bot with a Wonder that is free to build. Use this for tests where
     * the Wonder is the intended target.
     */
    private Bot createBotWithHighValueWonderStage() {
        Bot bot = new Bot("MinMaxBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20);

        // Wonder Stage giving 10 VP, Cost 0
        WonderStage stage = new WonderStage(new Cost.Gold(0), new Effect[]{new Effect.VictoryPoints(10)});
        Wonder wonder = new Wonder("Giza", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBotWithWonderContainingWoodCost() {
        Bot bot = new Bot("MinMaxBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20);

        // Cost requires WOOD
        Cost cost = new Cost.Materials(new Card.Materials[]{Card.Materials.WOOD});

        WonderStage stage = new WonderStage(cost, new Effect[]{new Effect.VictoryPoints(3)});
        Wonder wonder = new Wonder("Rhodos", Card.Materials.ORE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
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
