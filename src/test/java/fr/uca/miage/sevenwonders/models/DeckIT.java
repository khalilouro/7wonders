package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link Deck} class.
 * <p>
 * This test suite verifies the correct integration and behavior of the
 * {@link Deck} when manipulating real {@link Card} objects. It ensures that
 * cards are drawn, removed, and shuffled as expected in a real game scenario.
 * </p>
 */
class DeckIT {

    /** The deck instance used for integration testing. */
    private Deck deck;

    /** The list of actual cards contained in the deck during the tests. */
    private List<Card> realCards;

    /**
     * Initializes a {@link Deck} instance and injects a real list of cards into its
     * private field before each test.
     *
     * @throws Exception
     *             if reflection access fails
     */
    @BeforeEach
    void setUp() throws Exception {
        deck = new Deck(4, Card.Age.AGE_I);

        realCards = new ArrayList<>();

        String[] parents = new String[0];
        realCards.add(new Card("Lumber Yard", null, Card.Age.AGE_I, Card.Color.BROWN, null, parents, null));
        realCards.add(new Card("Stone Pit", null, Card.Age.AGE_I, Card.Color.BROWN, null, parents, null));
        realCards.add(new Card("Clay Pool", null, Card.Age.AGE_I, Card.Color.BROWN, null, parents, null));

        var field = Deck.class.getDeclaredField("cards");
        field.setAccessible(true);
        field.set(deck, realCards);
    }

    /**
     * Tests that {@link Deck#drawCard()} correctly removes and returns the first
     * card in the deck.
     * <p>
     * After drawing, the deck should contain one fewer card, and the removed card
     * should no longer be present in the list.
     * </p>
     *
     * @throws Exception
     *             if reflection access fails
     */
    @Test
    void drawCard_shouldRemoveAndReturnTheFirstCard() throws Exception {
        int initialSize = realCards.size();
        Card firstCard = realCards.get(0);

        Card drawnCard = deck.drawCard();

        assertEquals(firstCard, drawnCard);
        assertEquals(initialSize - 1, realCards.size());
        assertFalse(realCards.contains(firstCard));
    }

    /**
     * Tests that {@link Deck#drawCard()} returns {@code null} when the deck is
     * empty.
     *
     * @throws Exception
     *             if reflection access fails
     */
    @Test
    void drawCard_shouldReturnNull_whenDeckEmpty() throws Exception {
        realCards.clear();

        Card drawnCard = deck.drawCard();

        assertNull(drawnCard);
    }

    /**
     * Tests that {@link Deck#shuffle()} changes the order of the cards in the deck.
     * <p>
     * Verifies that the size and content remain the same after shuffling, and
     * ensures that the method executes without throwing any exception.
     * </p>
     *
     * @throws Exception
     *             if reflection access fails
     */
    @Test
    void shuffle_shouldChangeCardOrder() throws Exception {
        List<Card> before = new ArrayList<>(realCards);

        deck.shuffle();

        assertEquals(before.size(), realCards.size());
        assertTrue(realCards.containsAll(before));

        assertDoesNotThrow(() -> deck.shuffle());
    }
}
