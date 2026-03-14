package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TurnManagerTest {

    @Mock
    private Session session;
    @Mock
    private Deck deck;

    // We mock static Log to prevent console clutter
    private MockedStatic<Log> logMock;

    @InjectMocks
    private TurnManager turnManager;

    @BeforeEach
    void setUp() {
        logMock = mockStatic(Log.class);
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    @Test
    @DisplayName("Distribute 7 cards to each player")
    void testDistributeCards() {
        // Arrange
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player[] players = new Player[]{p1, p2};

        when(session.getPlayers()).thenReturn(players);
        when(session.getDeck()).thenReturn(deck);
        when(session.getAge()).thenReturn(Card.Age.AGE_I);

        // Mock Deck to return cards
        Card mockCard = mock(Card.class);
        when(deck.drawCard()).thenReturn(mockCard);

        // Act
        turnManager.distributeCards();

        // Assert
        // Each player should receive a list of 7 cards
        verify(p1).setHand(argThat(list -> list.size() == 7));
        verify(p2).setHand(argThat(list -> list.size() == 7));

        // Deck should be drawn 14 times (2 players * 7 cards)
        verify(deck, times(14)).drawCard();
    }

    @Test
    @DisplayName("Prepare Next Age: I -> II")
    void testPrepareNextAge_I_to_II() {
        // Arrange
        when(session.getAge()).thenReturn(Card.Age.AGE_I);
        when(session.getPlayers()).thenReturn(new Player[0]); // Empty players for simplicity

        // Mock Deck construction to avoid IO
        try (MockedConstruction<Deck> mockedDeck = mockConstruction(Deck.class)) {
            // Act
            turnManager.prepareNextAge();

            // Assert
            verify(session).setAge(Card.Age.AGE_II);
            verify(session).setDeck(any(Deck.class));
            verify(session).setCurrentTurn(1);
        }
    }

    @Test
    @DisplayName("Prepare Next Age: II -> III")
    void testPrepareNextAge_II_to_III() {
        // Arrange
        when(session.getAge()).thenReturn(Card.Age.AGE_II);

        Player p1 = mock(Player.class);
        when(session.getPlayers()).thenReturn(new Player[]{p1});

        try (MockedConstruction<Deck> mockedDeck = mockConstruction(Deck.class)) {
            // Act
            turnManager.prepareNextAge();

            // Assert
            verify(session).setAge(Card.Age.AGE_III);
            verify(p1).resetPerAgeEffects(); // Verify reset logic
        }
    }

    @Test
    @DisplayName("Trade Hands: Clockwise (Age I)")
    void testTradeHands_Clockwise() {
        // Arrange
        Player p0 = mock(Player.class);
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player[] players = new Player[]{p0, p1, p2};

        List<Card> hand0 = new ArrayList<>(List.of(mock(Card.class))); // P0's hand
        List<Card> hand1 = new ArrayList<>(List.of(mock(Card.class))); // P1's hand
        List<Card> hand2 = new ArrayList<>(List.of(mock(Card.class))); // P2's hand

        when(p0.getHand()).thenReturn(hand0);
        when(p1.getHand()).thenReturn(hand1);
        when(p2.getHand()).thenReturn(hand2);

        when(session.getPlayers()).thenReturn(players);
        when(session.getAge()).thenReturn(Card.Age.AGE_I);
        when(session.getCurrentTurn()).thenReturn(1);

        // Act
        turnManager.tradeHands();

        // Assert - Based on TurnManager Logic:
        // Clockwise: i gets i+1. Last gets 0.
        // P0 gets P1's hand (hand1)
        // P1 gets P2's hand (hand2)
        // P2 gets P0's hand (hand0)
        verify(p0).setHand(hand1);
        verify(p1).setHand(hand2);
        verify(p2).setHand(hand0);

        verify(session).setCurrentTurn(2);
    }

    @Test
    @DisplayName("Trade Hands: Counter-Clockwise (Age II)")
    void testTradeHands_CounterClockwise() {
        // Arrange
        Player p0 = mock(Player.class);
        Player p1 = mock(Player.class);
        Player p2 = mock(Player.class);
        Player[] players = new Player[]{p0, p1, p2};

        List<Card> hand0 = new ArrayList<>(List.of(mock(Card.class)));
        List<Card> hand1 = new ArrayList<>(List.of(mock(Card.class)));
        List<Card> hand2 = new ArrayList<>(List.of(mock(Card.class)));

        when(p0.getHand()).thenReturn(hand0);
        when(p1.getHand()).thenReturn(hand1);
        when(p2.getHand()).thenReturn(hand2);

        when(session.getPlayers()).thenReturn(players);
        when(session.getAge()).thenReturn(Card.Age.AGE_II);
        when(session.getCurrentTurn()).thenReturn(3);

        // Act
        turnManager.tradeHands();

        // Assert - Based on TurnManager Logic:
        // Counter-Clockwise: i gets i-1. First gets Last.
        // P2 gets P1's hand (hand1)
        // P1 gets P0's hand (hand0)
        // P0 gets P2's hand (hand2)
        verify(p2).setHand(hand1);
        verify(p1).setHand(hand0);
        verify(p0).setHand(hand2);

        verify(session).setCurrentTurn(4);
    }
}
