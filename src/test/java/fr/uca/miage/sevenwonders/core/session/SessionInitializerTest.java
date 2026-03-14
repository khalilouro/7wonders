package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.utils.Config;
import fr.uca.miage.sevenwonders.utils.Deserializer;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionInitializerTest {

    // Main object to be updated
    @Mock
    private Session session;

    // Singleton Mocks
    @Mock
    private Config config;
    @Mock
    private Bank bank;

    // Static Mock Controllers
    private MockedStatic<Deserializer> deserializerMock;
    private MockedStatic<Config> configMock;
    private MockedStatic<Bank> bankMock;
    private MockedStatic<Log> logMock;

    @BeforeEach
    void setUp() {
        // Initialize Static Mocks
        deserializerMock = mockStatic(Deserializer.class);
        configMock = mockStatic(Config.class);
        bankMock = mockStatic(Bank.class);
        logMock = mockStatic(Log.class); // Suppress logs

        // Configure Singleton Accessors
        configMock.when(Config::getInstance).thenReturn(config);
        bankMock.when(Bank::getInstance).thenReturn(bank);
    }

    @AfterEach
    void tearDown() {
        // Close Static Mocks to avoid leaking into other tests
        deserializerMock.close();
        configMock.close();
        bankMock.close();
        logMock.close();
    }

    @Test
    @DisplayName("Initialize Session with 3 Players")
    void testInitialize() {
        // --------------------------------------------------------
        // 1. ARRANGE: Configure Mocks and Data
        // --------------------------------------------------------
        int playerCount = 3;

        // Config Behavior
        when(config.getNumberOfPlayers()).thenReturn(playerCount);
        when(config.getWonderSidesToUse()).thenReturn("a"); // Deterministic side

        // NEW: Mock getBots() to return a list of player mocks
        List<Player> mockPlayers = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            mockPlayers.add(mock(Bot.class));
        }
        when(config.getBots()).thenReturn(mockPlayers);

        // Deserializer Behavior (Wonders)
        List<Wonder> mockWonders = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            Wonder w = mock(Wonder.class);
            when(w.getStartingResource()).thenReturn(Card.Materials.WOOD);
            mockWonders.add(w);
        }
        deserializerMock.when(() -> Deserializer.loadWonders(any())).thenReturn(mockWonders);

        // Session Behavior
        when(session.getBank()).thenReturn(bank);

        // Capture the players array when setPlayers is called
        final Player[][] playersCapture = new Player[1][];
        doAnswer(invocation -> {
            playersCapture[0] = invocation.getArgument(0);
            return null;
        }).when(session).setPlayers(any());

        // Return the captured array when getPlayers is called
        when(session.getPlayers()).thenAnswer(invocation -> playersCapture[0]);

        // --------------------------------------------------------
        // 2. ACT: Run initialize
        // --------------------------------------------------------
        try (MockedConstruction<Deck> deckCtor = mockConstruction(Deck.class)) {
            SessionInitializer.initialize(session);

            // --------------------------------------------------------
            // 3. ASSERT: Verify Logic
            // --------------------------------------------------------

            // A. Verify Global Setup
            deserializerMock.verify(Deserializer::loadAllCards);
            verify(bank).reset();

            // B. Verify Session State Updates
            verify(session).setPlayers(argThat(players -> players.length == playerCount));
            verify(session).setAge(Card.Age.AGE_I);
            verify(session).setDeck(any(Deck.class));
            verify(session).setCurrentTurn(1);
            verify(session).setWonders(anyList());

            // C. Verify Player Setup on the mock players from getBots()
            for (Player p : mockPlayers) {
                Bot b = (Bot) p;

                // Verify Wonder assignment
                verify(b).setWonderplayer(any(Wonder.class));
                verify(b).addProductionMaterial(any(Card.Materials.class));

                // Verify Bank Withdrawal (3 coins)
                verify(bank).WithdrawSilver(3, b);

                // Verify Neighborhood setup
                verify(b).setNeighborhood(any(Player.class), any(Player.class));
            }
        }
    }
}
