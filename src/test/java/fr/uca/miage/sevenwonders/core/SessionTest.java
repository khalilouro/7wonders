package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.core.session.*;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionTest {

    @Test
    void testConstructor_InitializesManagersAndCallsInitializer() {
        // We mock the construction of all managers instantiated inside Session
        // constructor
        try (MockedConstruction<TransactionManager> mockTransaction = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> mockActionExecutor = mockConstruction(ActionExecutor.class);
                MockedConstruction<TurnManager> mockTurnManager = mockConstruction(TurnManager.class);
                MockedConstruction<ConflictManager> mockConflictManager = mockConstruction(ConflictManager.class);
                MockedStatic<SessionInitializer> mockInitializer = mockStatic(SessionInitializer.class)) {

            Session session = new Session();

            // Verify Managers were instantiated
            assertEquals(1, mockTransaction.constructed().size());
            assertEquals(1, mockActionExecutor.constructed().size());
            assertEquals(1, mockTurnManager.constructed().size());
            assertEquals(1, mockConflictManager.constructed().size());

            // Verify SessionInitializer.initialize(session) was called
            mockInitializer.verify(() -> SessionInitializer.initialize(session));

            // Verify Bank and Discard Pile initialization
            assertNotNull(session.getBank(), "Bank should be initialized");
            assertNotNull(session.getDiscardPile(), "Discard pile should be initialized");
            assertTrue(session.getDiscardPile().isEmpty(), "Discard pile should be empty initially");
        }
    }

    @Test
    void testDistributeCards_DelegatesToTurnManager() {
        try (MockedConstruction<TurnManager> mockTurnManager = mockConstruction(TurnManager.class);
                // Mock others to avoid errors during instantiation
                MockedConstruction<TransactionManager> tm = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> ae = mockConstruction(ActionExecutor.class);
                MockedConstruction<ConflictManager> cm = mockConstruction(ConflictManager.class);
                MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class)) {

            Session session = new Session();
            session.distrebutsCards();

            // Verify delegation
            TurnManager turnManager = mockTurnManager.constructed().get(0);
            verify(turnManager).distributeCards();
        }
    }

    @Test
    void testPrepareNextAge_DelegatesToTurnManager() {
        try (MockedConstruction<TurnManager> mockTurnManager = mockConstruction(TurnManager.class);
                MockedConstruction<TransactionManager> tm = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> ae = mockConstruction(ActionExecutor.class);
                MockedConstruction<ConflictManager> cm = mockConstruction(ConflictManager.class);
                MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class)) {

            Session session = new Session();
            session.prepareNextAge();

            // Verify delegation
            TurnManager turnManager = mockTurnManager.constructed().get(0);
            verify(turnManager).prepareNextAge();
        }
    }

    @Test
    void testTradeHands_DelegatesToTurnManager() {
        try (MockedConstruction<TurnManager> mockTurnManager = mockConstruction(TurnManager.class);
                MockedConstruction<TransactionManager> tm = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> ae = mockConstruction(ActionExecutor.class);
                MockedConstruction<ConflictManager> cm = mockConstruction(ConflictManager.class);
                MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class)) {

            Session session = new Session();
            session.tradeHands();

            // Verify delegation
            TurnManager turnManager = mockTurnManager.constructed().get(0);
            verify(turnManager).tradeHands();
        }
    }

    @Test
    void testPlayerPlaysCard_DelegatesToActionExecutor() {
        try (MockedConstruction<ActionExecutor> mockActionExecutor = mockConstruction(ActionExecutor.class);
                MockedConstruction<TurnManager> tm = mockConstruction(TurnManager.class);
                MockedConstruction<TransactionManager> tr = mockConstruction(TransactionManager.class);
                MockedConstruction<ConflictManager> cm = mockConstruction(ConflictManager.class);
                MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class)) {

            Session session = new Session();
            int playerIndex = 1;

            session.playerPlaysCard(playerIndex);

            // Verify delegation
            ActionExecutor actionExecutor = mockActionExecutor.constructed().get(0);
            verify(actionExecutor).executeTurn(playerIndex);
        }
    }

    @Test
    void testConflictResolution_DelegatesToConflictManager() {
        try (MockedConstruction<ConflictManager> mockConflictManager = mockConstruction(ConflictManager.class);
                MockedConstruction<TurnManager> tm = mockConstruction(TurnManager.class);
                MockedConstruction<TransactionManager> tr = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> ae = mockConstruction(ActionExecutor.class);
                MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class)) {

            Session session = new Session();
            Player[] players = new Player[]{mock(Player.class)};
            Card.Age age = Card.Age.AGE_I;

            session.conflictResolution(players, age);

            // Verify delegation
            ConflictManager conflictManager = mockConflictManager.constructed().get(0);
            verify(conflictManager).resolveConflicts(players, age);
        }
    }

    @Test
    void testDiscardPileOperations() {
        // We don't need to mock managers deeply here, just enough to construct Session
        try (MockedStatic<SessionInitializer> init = mockStatic(SessionInitializer.class);
                MockedConstruction<TurnManager> tm = mockConstruction(TurnManager.class);
                MockedConstruction<TransactionManager> tr = mockConstruction(TransactionManager.class);
                MockedConstruction<ActionExecutor> ae = mockConstruction(ActionExecutor.class);
                MockedConstruction<ConflictManager> cm = mockConstruction(ConflictManager.class)) {

            Session session = new Session();
            Card mockCard = mock(Card.class);

            // Test Add
            session.addToDiscardPile(mockCard);
            List<Card> pile = session.getDiscardPile();
            assertEquals(1, pile.size());
            assertTrue(pile.contains(mockCard));

            // Test Remove
            session.removeFromDiscardPile(mockCard);
            assertTrue(session.getDiscardPile().isEmpty());
        }
    }
}
