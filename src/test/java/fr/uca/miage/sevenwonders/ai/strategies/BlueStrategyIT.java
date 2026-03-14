package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for BlueStrategy using real objects. Verifies that the bot
 * prioritizes Blue (Civilian) cards and correctly falls back when no valid Blue
 * cards are available.
 */
class BlueStrategyIT {

    private Bot bot;
    private Bank bank;
    private BlueStrategy strategy;

    @BeforeEach
    void setUp() {
        // 1. Initialize real dependencies
        bank = Bank.getInstance();
        bank.reset();
        strategy = new BlueStrategy();

        // 2. Initialize Bot with the strategy
        bot = new Bot("CivilianBot", strategy);

        // 3. Assign a Wonder (required for Bot structure)
        Wonder wonder = new Wonder("Babylon", Wonder.Side.A, Card.Materials.CLAY);
        bot.setWonderplayer(wonder);

        // 4. Reset state
        bot.getResources().setGold(0);
        bot.setHand(new ArrayList<>());
    }

    @Test
    void testPrioritizesBlueCardOverOthers() {
        // Context: Hand contains a Blue card and a Green card. Both are free.

        Card blueCard = new Card("Altar", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(2),
                null, null);

        Card greenCard = new Card("Scriptorium", Cost.free(), Card.Age.AGE_I, Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.TABLET), null, null);

        // Blue card at index 0
        bot.setHand(List.of(blueCard, greenCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int action = actionCode / 10;
        int cardIndex = actionCode % 10;

        // Expect Build (1) of the Blue Card (0)
        assertEquals(1, action, "Should build the card.");
        assertEquals(0, cardIndex, "Should select the Blue card over the Green one.");
    }

    @Test
    void testPrioritizesHighestVPBlueCard() {
        // Context: Hand contains two Blue cards with different VP values.

        // Card 1: 2 VP
        Card lowVPCard = new Card("Theater", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(2),
                null, null);

        // Card 2: 5 VP (Higher value)
        Card highVPCard = new Card("Gardens", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(5),
                null, null);

        // Put high VP card at index 1
        bot.setHand(List.of(lowVPCard, highVPCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int action = actionCode / 10;
        int cardIndex = actionCode % 10;

        // Expect Build (1) of the High VP Card (1)
        assertEquals(1, action);
        assertEquals(1, cardIndex, "Should prioritize the Blue card with higher Victory Points.");
    }

    @Test
    void testFallsBackToDiscardIfNoBlueCards() {
        // Context: Hand has no Blue cards (only Red and Green).

        Card redCard = new Card("Barracks", Cost.free(), Card.Age.AGE_I, Card.Color.RED, new Effect.Military(1), null,
                null);

        Card greenCard = new Card("Market", Cost.free(), Card.Age.AGE_I, Card.Color.GREEN,
                new Effect.Science(Effect.Science.ScienceSymbol.COMPASS), null, null);

        bot.setHand(List.of(redCard, greenCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int action = actionCode / 10;
        int cardIndex = actionCode % 10;

        // Expect Discard (0)
        assertEquals(0, action, "Should fallback to discard action if no Blue cards are available.");
        // By default logic, it usually discards the first card (index 0) if forced to
        // fallback
        assertEquals(0, cardIndex);
    }

    @Test
    void testFallsBackToDiscardIfBlueCardTooExpensive() {
        // Context: Hand has a Blue card, but it is too expensive to build.

        // Cost: 100 Gold (Bot has 0)
        Card expensiveBlue = new Card("Palace", new Cost.Gold(100), Card.Age.AGE_III, Card.Color.BLUE,
                new Effect.VictoryPoints(8), null, null);

        // Another distraction card
        Card otherCard = new Card("Lumber Yard", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.WOOD}), null, null);

        bot.setHand(List.of(expensiveBlue, otherCard));

        int actionCode = strategy.applyStrategy(bot, bank);
        int action = actionCode / 10;

        // Expect Discard (0) because the only Blue card is unaffordable
        assertEquals(0, action, "Should fallback to discard if the Blue card cannot be afforded.");
    }
}
