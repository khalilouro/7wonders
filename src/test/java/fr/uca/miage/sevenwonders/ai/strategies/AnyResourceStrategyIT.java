package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnyResourceStrategyIT {

    private Bot bot;
    private Bank bank;
    private AnyResourceStrategy strategy;

    @BeforeEach
    void setUp() {
        bank = Bank.getInstance();
        bank.reset();
        strategy = new AnyResourceStrategy();
        bot = new Bot("ResourceBot", strategy);

        // 1. Create Wonder
        Wonder wonder = new Wonder("Rhodes", Wonder.Side.A, Card.Materials.ORE);

        // 2. Manually inject a stage so the strategy knows we need WOOD
        Cost cost = new Cost.Materials(new Card.Materials[]{Card.Materials.WOOD, Card.Materials.WOOD});
        Effect effect = new Effect.VictoryPoints(3);
        WonderStage stage1 = new WonderStage(cost, new Effect[]{effect});

        // Initialize the stages array (simulating what Deserializer does)
        wonder.stages = new WonderStage[3];
        wonder.stages[0] = stage1;

        bot.setWonderplayer(wonder);
        bot.getResources().setGold(0);
        bot.setHand(new ArrayList<>());
    }

    @Test
    void testPrioritizesResourceNeededForWonder() {
        // Hand: Index 0=STONE, Index 1=WOOD
        Card stoneCard = new Card("Stone Pit", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.STONE}), null, null);

        Card woodCard = new Card("Lumber Yard", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.WOOD}), null, null);

        bot.setHand(List.of(stoneCard, woodCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int cardIndex = actionCode % 10;

        // Should pick Index 1 (WOOD) because the wonder stage needs it
        assertEquals(1, cardIndex, "Should prioritize WOOD because the wonder needs it.");
    }

    @Test
    void testBuildsAnyResourceIfNoneSpecificNeeded() {
        Card stoneCard = new Card("Stone Pit", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.STONE}), null, null);
        Card blueCard = new Card("Altar", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(2),
                null, null);

        bot.setHand(List.of(stoneCard, blueCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int cardIndex = actionCode % 10;

        assertEquals(0, cardIndex, "Should build resource card even if not strictly needed for wonder.");
    }

    @Test
    void testFallsBackToDiscardIfNoResourcesBuildable() {
        Card blueCard1 = new Card("Altar", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(2),
                null, null);
        Card blueCard2 = new Card("Theater", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(2),
                null, null);

        bot.setHand(List.of(blueCard1, blueCard2));

        int actionCode = strategy.applyStrategy(bot, bank);
        assertEquals(0, actionCode / 10, "Should discard if no resource cards are available.");
    }
}
