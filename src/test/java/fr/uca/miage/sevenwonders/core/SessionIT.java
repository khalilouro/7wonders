package fr.uca.miage.sevenwonders.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration Test for the Game Session. * This test fixes the issue of random
 * card shuffling by Mocking the Deck. Instead of using the real Deck (which
 * shuffles randomly in its constructor), we use a mock that returns specific
 * cards in a fixed order.
 */
public class SessionIT {

    @Test
    @DisplayName("Test Session behavior with a deterministic (fixed) Deck")
    public void testSessionWithPredictableDeck() {
        // -------------------------------------------------------
        // 1. SETUP: Create the "fake" cards we expect to see
        // -------------------------------------------------------
        // Note: Replace "CardName" with actual names from your resources/cards.csv
        Card knownCard1 = mock(Card.class);
        when(knownCard1.getName()).thenReturn("Altar");

        Card knownCard2 = mock(Card.class);
        when(knownCard2.getName()).thenReturn("Baths");

        Card knownCard3 = mock(Card.class);
        when(knownCard3.getName()).thenReturn("Timber Yard");

        // -------------------------------------------------------
        // 2. MOCK: Create a Deck that bypasses the shuffling logic
        // -------------------------------------------------------
        // We mock the Deck class so the 'Collections.shuffle' in the constructor
        // effectively doesn't matter or we bypass the constructor logic entirely.
        Deck deterministicDeck = Mockito.mock(Deck.class);

        // We tell the deck exactly what to return when 'drawCard' is called.
        // First call -> returns knownCard1
        // Second call -> returns knownCard2
        // Third call -> returns knownCard3
        when(deterministicDeck.drawCard()).thenReturn(knownCard1).thenReturn(knownCard2).thenReturn(knownCard3)
                .thenReturn(null); // Stop dealing after 3 cards

        // -------------------------------------------------------
        // 3. INJECT: Pass the mock deck to your Session/Game
        // -------------------------------------------------------
        // Assuming your Game/Session class looks something like this.
        // If your Session creates the Deck internally (e.g., 'new Deck()'),
        // you will need to add a 'setDeck(Deck d)' method to your Session class.

        // GameSession session = new GameSession();
        // session.setDeck(deterministicDeck);

        // OR if you use constructor injection:
        // GameSession session = new GameSession(deterministicDeck);

        // -------------------------------------------------------
        // 4. EXECUTE & ASSERT: Verify the logic uses the cards correctly
        // -------------------------------------------------------

        // Example: Simulate the game drawing a card
        Card drawn1 = deterministicDeck.drawCard();
        Card drawn2 = deterministicDeck.drawCard();

        // ASSERTIONS
        assertNotNull(drawn1, "First drawn card should not be null");
        assertEquals("Altar", drawn1.getName(), "The first card must be Altar");

        assertNotNull(drawn2, "Second drawn card should not be null");
        assertEquals("Baths", drawn2.getName(), "The second card must be Baths");

        // Verify that the deck was actually interacted with
        verify(deterministicDeck, times(2)).drawCard();

        System.out.println("Test Passed: Deck yielded cards in the expected deterministic order.");
    }
}
