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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Test for MarStrategy.
 * <p>
 * Tests the heuristic decision making across different Ages (I, II, III) using
 * real Bot, Player, and Card objects.
 * </p>
 */
class MarStrategyIT {

    private MarStrategy strategy;
    private Bank bank;

    // Action constants
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    @BeforeEach
    void setUp() {
        strategy = new MarStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testAge1_PrioritizeResources_WhenLowOnResources() {
        // AGE I Strategy:
        // 1. If resources < 3, prioritize Brown/Grey.

        Bot bot = createBot();

        Card blueCard = createCard("Altar", Card.Color.BLUE, Card.Age.AGE_I);
        Card brownCard = createCard("LumberYard", Card.Color.BROWN, Card.Age.AGE_I);

        // Put Blue at 0 and Brown at 1.
        // Strategy prioritizes Brown over Blue in Age 1 if resources are low.
        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(brownCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Brown Card)
        assertEquals(encode(ACTION_BUILD, 1), result, "Age I: Should prioritize Brown card when resources are low");
    }

    @Test
    void testAge1_PrioritizeMilitary_WhenNotAhead() {
        // AGE I Strategy:
        // 2. If resources >= 3 (or none found), check Military.

        Bot bot = createBot();
        setupNeighbors(bot, 2, 0); // Left neighbor has 2 shields, Bot has 0.

        Card blueCard = createCard("Altar", Card.Color.BLUE, Card.Age.AGE_I);
        Card redCard = createCard("Barracks", Card.Color.RED, Card.Age.AGE_I);

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(redCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Red Card)
        assertEquals(encode(ACTION_BUILD, 1), result,
                "Age I: Should prioritize Red card when not largely ahead militarily");
    }

    @Test
    void testAge2_PrioritizeMilitary_WhenBehind() {
        // AGE II Strategy:
        // 1. If MyStr < MaxNeighborStr, prioritize Red.

        Bot bot = createBot();
        setupNeighbors(bot, 5, 1); // Left has 5, Right has 1. Bot has 0.

        Card blueCard = createCard("Aqueduct", Card.Color.BLUE, Card.Age.AGE_II);
        Card redCard = createCard("Walls", Card.Color.RED, Card.Age.AGE_II);

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(redCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Red Card)
        assertEquals(encode(ACTION_BUILD, 1), result,
                "Age II: Should prioritize Red card when strictly weaker than neighbor");
    }

    @Test
    void testAge2_PrioritizeGreen_WhenScienceWellUnderway() {
        // AGE II Strategy:
        // Priority Order: Red -> Blue -> Green (if science >= 2) -> Resources.
        // To test Green, we must ensure Red and Blue are NOT present or needed.

        Bot bot = createBot();
        setupNeighbors(bot, 0, 0); // Safe militarily (No Red needed)

        // Add science symbols to satisfy the "Science >= 2" condition
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.TABLET);
        bot.getScience().addSymbol(Effect.Science.ScienceSymbol.COMPASS);

        // Setup Hand:
        // We use a Brown card as the alternative. In Age 2, Brown is lower priority
        // than Green.
        // If we used Blue, the strategy would pick Blue (see MarStrategy.java lines
        // 90-95).
        Card brownCard = createCard("BrickYard", Card.Color.BROWN, Card.Age.AGE_II);
        Card greenCard = createCard("Library", Card.Color.GREEN, Card.Age.AGE_II);

        bot.getHand().add(brownCard); // Index 0
        bot.getHand().add(greenCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Green Card)
        assertEquals(encode(ACTION_BUILD, 1), result,
                "Age II: Should prioritize Green card when science count >= 2 and no Blue/Red cards override it");
    }

    @Test
    void testAge3_PrioritizePurple() {
        // AGE III Strategy:
        // Priority Order: Red (if weak) -> Purple -> Blue.

        Bot bot = createBot();
        setupNeighbors(bot, 0, 0); // Safe militarily

        Card blueCard = createCard("Pantheon", Card.Color.BLUE, Card.Age.AGE_III);
        Card purpleCard = createCard("Guild", Card.Color.PURPLE, Card.Age.AGE_III);

        bot.getHand().add(blueCard); // Index 0
        bot.getHand().add(purpleCard); // Index 1

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action BUILD (1) on Index 1 (Purple Card)
        // Code checks Purple before Blue.
        assertEquals(encode(ACTION_BUILD, 1), result, "Age III: Should prioritize Purple card over Blue");
    }

    @Test
    void testAge3_FallbackToWonder() {
        // AGE III Strategy:
        // If no Red/Purple/Blue found, check Wonder.

        Bot bot = createBotWithWonder();
        setupNeighbors(bot, 0, 0);

        // Hand contains only Grey (no priority in Age 3)
        Card greyCard = createCard("Lodge", Card.Color.GREY, Card.Age.AGE_III);

        bot.getHand().add(greyCard); // Index 0

        int result = strategy.applyStrategy(bot, bank);

        // Expect: Action WONDER (2) on Index 0
        assertEquals(encode(ACTION_WONDER, 0), result,
                "Age III: Should fallback to Wonder construction if no priority cards found");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Bot createBot() {
        Bot bot = new Bot("TestBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20); // Give gold so costs aren't an issue
        return bot;
    }

    private Bot createBotWithWonder() {
        Bot bot = createBot();
        // Create a Wonder with an affordable stage (Cost 0)
        WonderStage stage = new WonderStage(new Cost.Gold(0), new Effect[]{new Effect.VictoryPoints(3)});
        Wonder wonder = new Wonder("Giza", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private void setupNeighbors(Bot bot, int leftStr, int rightStr) {
        Player left = new Player("Left");
        left.getMilitary().addStrength(leftStr);

        Player right = new Player("Right");
        right.getMilitary().addStrength(rightStr);

        bot.setNeighborhood(left, right);
    }

    private Card createCard(String name, Card.Color color, Card.Age age) {
        // Simplest constructor available
        return new Card(name, new Cost.Gold(0), // Free
                age, color, new Effect.VictoryPoints(1), new String[]{}, new String[]{});
    }

    private int encode(int action, int index) {
        return action * 10 + index;
    }
}
