package fr.uca.miage.sevenwonders.ai;

import fr.uca.miage.sevenwonders.core.Session;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Bot class using real dependencies (no Mockito).
 */
class BotIT {

    private Bot bot;
    private Session session;
    private Bank bank;

    @BeforeEach
    void setUp() {
        // 1. Reset Bank (Singleton)
        bank = Bank.getInstance();
        bank.reset();

        // 2. Initialize a real Session
        // This triggers the full initialization logic (loading cards, wonders, etc.)
        session = new Session();

        // 3. Create a Bot with a deterministic strategy for testing
        // Strategy: Always action 0 (Discard) on the first card (index 0) -> return 0
        Strategy deterministicStrategy = new Strategy() {
            @Override
            public int applyStrategy(Bot bot, Bank bank) {
                return 0;
            }

            @Override
            public String getName() {
                return "Deterministic";
            }
        };

        bot = new Bot("IntegrationBot", deterministicStrategy);

        // Give the bot a clean state for testing (reset hand, resources, etc.)
        bot.setHand(new ArrayList<>());
        // Give bot some initial gold/silver for transactions if needed
        bot.getResources().setGold(5);
    }

    @Test
    void testApplyStrategyIntegration() {
        // Arrange
        Card dummyCard = new Card("TestCard", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(3),
                null, null);
        bot.setHand(List.of(dummyCard));

        // Act
        // Uses the real strategy defined in setUp (returns 0 => Discard index 0)
        int actionCode = bot.applyStrategy(bot, bank);

        // Assert
        assertEquals(0, actionCode, "Strategy should return 0 (Discard first card)");
    }

    @Test
    void testChooseAndPlayFromDiscard_RealInteraction() {
        // This test verifies the Halikarnassus/Manneken Pis effect logic with real
        // objects.

        // 1. Enable the ability on the bot
        bot.addPlayFromDiscard(1);
        assertTrue(bot.canPlayFromDiscard(), "Bot should have permission to play from discard.");

        // 2. Create a real card with Victory Points
        Card vpCard = new Card("HighVPCard", Cost.free(), Card.Age.AGE_I, Card.Color.BLUE, new Effect.VictoryPoints(10),
                null, null);

        // 3. Create a distraction card (low value)
        Card lowCard = new Card("LowVPCard", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN, new Effect.VictoryPoints(1),
                null, null);

        // 4. Add cards to the Session's discard pile
        session.addToDiscardPile(lowCard);
        session.addToDiscardPile(vpCard);

        assertEquals(2, session.getDiscardPile().size(), "Session discard pile should have 2 cards.");

        // Act
        bot.chooseAndPlayFromDiscard(session, false);

        // Assert
        // A. Verify the correct card was chosen (The logic prioritizes highest VP)
        assertTrue(bot.getAlreadyBuilt().contains("HighVPCard"), "Bot should have built the High VP card.");
        assertFalse(bot.getAlreadyBuilt().contains("LowVPCard"), "Bot should NOT have built the Low VP card.");

        // B. Verify Session state updated
        assertFalse(session.getDiscardPile().contains(vpCard), "Chosen card should be removed from session discard.");
        assertTrue(session.getDiscardPile().contains(lowCard), "Unchosen card should remain in session discard.");

        // C. Verify Bot ability consumed
        assertFalse(bot.canPlayFromDiscard(), "Play from discard ability should be consumed.");

        // D. Verify Effect applied (Score calculation)
        bot.computeFinalScore();
        // Bot started with 0 VP. Built card gives 10.
        // Note: computeFinalScore() adds treasury points too.
        // 5 Gold = 5 VP. Total should be 15.
        // Let's verify specifically the category or just the sum.
        assertEquals(10, bot.getPointsByCategory(Effect.Category.CIVILIAN),
                "Bot should have 10 Civilian points from the card.");
    }

    @Test
    void testBotInheritsPlayerProperties() {
        // Verify that the Bot correctly integrates with the underlying Player systems
        // (Resources, Board)

        // 1. Modify resources
        bot.getResources().addGold(10);
        assertEquals(15, bot.getResources().getGold(), "Bot should inherit resource management from Player.");

        // 2. Modify board
        bot.getBoard().updateBoardElement("RED", 1);
        assertEquals(1, bot.getBoard().getBoardElements().get("RED"),
                "Bot should inherit board management from Player.");
    }
}
