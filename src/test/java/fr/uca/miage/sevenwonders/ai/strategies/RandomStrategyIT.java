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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration Test for RandomStrategy.
 * <p>
 * Since RandomStrategy is non-deterministic, these tests use statistical
 * probability (running loops) or constrained scenarios (impossible actions) to
 * verify behavior.
 * </p>
 */
class RandomStrategyIT {

    private RandomStrategy strategy;
    private Bank bank;

    // Action Constants
    private static final int ACTION_DISCARD = 0;
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    @BeforeEach
    void setUp() {
        strategy = new RandomStrategy();
        bank = Bank.getInstance();
        bank.reset();
    }

    @Test
    void testStrategyFallsBackToDiscardIfNothingBuildable() {
        // Setup: Bot has 0 gold and no resources.
        // Hand contains expensive cards that cannot be built.
        // Wonder is also too expensive.

        Bot bot = createBotWithExpensiveWonder();
        bot.getResources().setGold(0);

        Card expensiveCard = createCard("Palace", new Cost.Gold(100)); // Unaffordable
        bot.getHand().add(expensiveCard);

        // Run multiple times to ensure it consistently falls back to discard
        // The strategy retries 10 times then defaults to discard
        for (int i = 0; i < 50; i++) {
            int result = strategy.applyStrategy(bot, bank);
            int action = result / 10;

            assertEquals(ACTION_DISCARD, action, "Should always discard if Build and Wonder are impossible");
        }
    }

    @Test
    void testStrategyEventuallyBuildsCardIfAffordable() {
        // Setup: Bot has plenty of gold.
        // Hand contains cheap cards.
        // We want to verify that the random strategy *can* and *does* choose to build.

        Bot bot = createBot(); // Has default 20 gold
        Card cheapCard = createCard("Altar", new Cost.Gold(0));
        bot.getHand().add(cheapCard);

        boolean builtAtLeastOnce = false;

        // Run a loop. Statistically, it should pick action 1 (Build) within 100 tries.
        for (int i = 0; i < 100; i++) {
            int result = strategy.applyStrategy(bot, bank);
            int action = result / 10;

            if (action == ACTION_BUILD) {
                builtAtLeastOnce = true;
                break;
            }
        }

        assertTrue(builtAtLeastOnce, "Random strategy should eventually choose to Build (Action 1) when possible");
    }

    @Test
    void testStrategyEventuallyBuildsWonderIfAffordable() {
        // Setup: Bot has plenty of gold and an affordable Wonder stage.

        Bot bot = createBotWithAffordableWonder();
        Card card = createCard("Filler", new Cost.Gold(0));
        bot.getHand().add(card);

        boolean builtWonderAtLeastOnce = false;

        // Run a loop. Statistically, it should pick action 2 (Wonder) within 100 tries.
        for (int i = 0; i < 100; i++) {
            int result = strategy.applyStrategy(bot, bank);
            int action = result / 10;

            if (action == ACTION_WONDER) {
                builtWonderAtLeastOnce = true;
                break;
            }
        }

        assertTrue(builtWonderAtLeastOnce,
                "Random strategy should eventually choose to Build Wonder (Action 2) when possible");
    }

    @Test
    void testValidReturnStructure() {
        // Verify the return format is strictly action * 10 + index
        Bot bot = createBot();
        bot.getHand().add(createCard("C1", new Cost.Gold(0)));
        bot.getHand().add(createCard("C2", new Cost.Gold(0)));

        int result = strategy.applyStrategy(bot, bank);

        int action = result / 10;
        int index = result % 10;

        assertTrue(action >= 0 && action <= 2, "Action must be 0, 1, or 2");
        assertTrue(index >= 0 && index < bot.getHand().size(), "Index must be within hand bounds");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Bot createBot() {
        Bot bot = new Bot("RandomBot", strategy);
        bot.getHand().clear();
        bot.getResources().setGold(20);
        return bot;
    }

    private Bot createBotWithAffordableWonder() {
        Bot bot = createBot();
        // Affordable Stage (Cost 0)
        WonderStage stage = new WonderStage(new Cost.Gold(0), new Effect[]{new Effect.VictoryPoints(3)});
        Wonder wonder = new Wonder("AffordableWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Bot createBotWithExpensiveWonder() {
        Bot bot = createBot();
        // Expensive Stage (100 Gold)
        WonderStage stage = new WonderStage(new Cost.Gold(100), new Effect[]{new Effect.VictoryPoints(10)});
        Wonder wonder = new Wonder("ExpensiveWonder", Card.Materials.STONE, new WonderStage[]{stage}, Wonder.Side.A);
        bot.setWonderplayer(wonder);
        return bot;
    }

    private Card createCard(String name, Cost cost) {
        return new Card(name, cost, Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(1), new String[]{},
                new String[]{});
    }
}
