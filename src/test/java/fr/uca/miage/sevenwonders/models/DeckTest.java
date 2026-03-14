package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link Deck} class.
 * <p>
 * This test suite verifies that the {@link Deck} correctly handles card drawing
 * and shuffling operations.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class DeckTest {

    /** Mocked list representing the internal collection of cards in the deck. */
    @Mock
    private List<Card> mockCards;

    /** The deck instance under test. */
    private Deck deck;

    /**
     * Initializes a new {@link Deck} instance before each test and injects the
     * mocked list into its private field.
     *
     * @throws Exception
     *             if reflection access fails
     */
    @BeforeEach
    void setUp() throws Exception {
        deck = new Deck(4, Card.Age.AGE_I);

        var field = Deck.class.getDeclaredField("cards");
        field.setAccessible(true);
        field.set(deck, mockCards);
    }

    /**
     * Tests that {@link Deck#drawCard()} returns a card when the deck is not empty.
     * Verifies that the internal list's {@code isEmpty()} and {@code remove(0)}
     * methods are called.
     */
    @Test
    void drawCard_shouldReturnCard_whenNotEmpty() {
        Card fakeCard = mock(Card.class);

        when(mockCards.isEmpty()).thenReturn(false);
        when(mockCards.remove(0)).thenReturn(fakeCard);

        Card result = deck.drawCard();

        assertEquals(fakeCard, result);
        verify(mockCards).isEmpty();
        verify(mockCards).remove(0);
    }

    /**
     * Tests that {@link Deck#drawCard()} returns {@code null} when the deck is
     * empty and does not attempt to remove any card.
     */
    @Test
    void drawCard_shouldReturnNull_whenEmpty() {
        when(mockCards.isEmpty()).thenReturn(true);

        Card result = deck.drawCard();

        assertNull(result);
        verify(mockCards).isEmpty();
        verify(mockCards, never()).remove(anyInt());
    }

    /**
     * Verifies that {@link Deck#shuffle()} executes without throwing any exception.
     */
    @Test
    void shuffle_shouldCallCollectionsShuffle() {
        assertDoesNotThrow(() -> deck.shuffle());
    }
}
