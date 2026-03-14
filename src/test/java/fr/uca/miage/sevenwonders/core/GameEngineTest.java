package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.stats.GameResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineTest {

    @Test
    void testRunGame_StandardLoop() {
        // We need to mock the construction of Session because GameEngine calls 'new
        // Session()'
        try (MockedConstruction<Session> sessionMockedConstruction = Mockito.mockConstruction(Session.class,
                (mockSession, context) -> {
                    // --- Configure the Mock Session ---

                    // Create dummy players
                    Player player1 = mock(Player.class);
                    Player player2 = mock(Player.class);
                    Player[] players = new Player[]{player1, player2};

                    // Stub getPlayers() to return our mocks
                    when(mockSession.getPlayers()).thenReturn(players);

                    // Stub getHand() to be empty initially or populated to allow loop entry
                    // GameEngine checks !p.getHand().isEmpty() in playTurn
                    // We mock the hand to ensure loop doesn't crash on stream operations
                    List<Card> mockHand = new ArrayList<>();
                    mockHand.add(mock(Card.class));
                    when(player1.getHand()).thenReturn(mockHand);
                    when(player2.getHand()).thenReturn(mockHand);

                    // Stub names for verbose output
                    when(player1.getName()).thenReturn("P1");
                    when(player2.getName()).thenReturn("P2");

                    // Default behaviors for simple booleans (Standard game, no special effects yet)
                    when(player1.canPlayFromDiscard()).thenReturn(false);
                    when(player2.canPlayFromDiscard()).thenReturn(false);
                    when(player1.canPlayLastCard()).thenReturn(false);
                    when(player2.canPlayLastCard()).thenReturn(false);
                    when(player1.canCopyGuild()).thenReturn(false);
                    when(player2.canCopyGuild()).thenReturn(false);
                })) {

            // --- Execute ---
            GameEngine engine = new GameEngine();
            GameResult result = engine.runGame(1, false);

            // --- Verification ---
            Session session = sessionMockedConstruction.constructed().get(0); // Get the intercepted mock

            // 1. Verify Card Distribution
            // Called once per Age (3 Ages)
            verify(session, times(3)).distrebutsCards();

            // 2. Verify Turns Played
            // 3 Ages * 6 Rounds * 2 Players = 36 turns
            verify(session, times(36)).playerPlaysCard(anyInt());

            // 3. Verify Hand Trading
            // Called rounds 1-5 (5 times) per Age (3 Ages) = 15 times
            verify(session, times(15)).tradeHands();

            // 4. Verify Conflict Resolution
            // Called once per Age (3 Ages)
            verify(session, times(3)).conflictResolution(any(), any());

            // 5. Verify Next Age Preparation
            // Called once per Age (3 Ages)
            verify(session, times(3)).prepareNextAge();

            // 6. Verify Score Calculation
            // computeFinalScore is called twice per player in calculateResults (once before
            // purple, once after)
            // 2 players * 2 calls = 4 calls total
            Player p1 = session.getPlayers()[0];
            verify(p1, atLeastOnce()).computeFinalScore();

            assertNotNull(result, "GameResult should be returned");
        }
    }

    @Test
    void testRunGame_WithHalikarnassusEffect() {
        try (MockedConstruction<Session> sessionMockedConstruction = Mockito.mockConstruction(Session.class,
                (mockSession, context) -> {
                    // Setup Bot player with Halikarnassus effect
                    Bot bot = mock(Bot.class);
                    Player[] players = new Player[]{bot};

                    when(mockSession.getPlayers()).thenReturn(players);
                    when(bot.getHand()).thenReturn(Collections.singletonList(mock(Card.class)));

                    // Enable Halikarnassus effect
                    when(bot.canPlayFromDiscard()).thenReturn(true);
                })) {

            GameEngine engine = new GameEngine();
            engine.runGame(1, false);

            Session session = sessionMockedConstruction.constructed().get(0);
            Bot bot = (Bot) session.getPlayers()[0];

            // Verify Bot specific method is called
            // 3 Ages * 6 Rounds = 18 checks. If it returns true every time, it's called 18
            // times.
            verify(bot, atLeast(1)).chooseAndPlayFromDiscard(eq(session), anyBoolean());
        }
    }

    @Test
    void testRunGame_WithBabylonEffect() {
        try (MockedConstruction<Session> sessionMockedConstruction = Mockito.mockConstruction(Session.class,
                (mockSession, context) -> {
                    // Setup Player with Babylon effect (can play last card)
                    Player p1 = mock(Player.class);
                    Player[] players = new Player[]{p1};

                    when(mockSession.getPlayers()).thenReturn(players);
                    when(p1.getHand()).thenReturn(Collections.singletonList(mock(Card.class))); // Hand not empty

                    // Enable Babylon effect
                    when(p1.canPlayLastCard()).thenReturn(true);
                })) {

            GameEngine engine = new GameEngine();
            engine.runGame(1, false);

            Session session = sessionMockedConstruction.constructed().get(0);

            // Normal turns: 3 Ages * 6 Rounds * 1 Player = 18 calls
            // Babylon turns: 1 extra call per Age (played at end of age) * 3 Ages = 3 calls
            // Total expected: 21 calls
            verify(session, times(21)).playerPlaysCard(0);
        }
    }
}
