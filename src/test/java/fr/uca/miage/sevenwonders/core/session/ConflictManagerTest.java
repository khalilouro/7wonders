package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.models.player.PlayerMilitary;
import fr.uca.miage.sevenwonders.models.player.PlayerBoard;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConflictManagerTest {

    private ConflictManager conflictManager;

    @Mock
    private Player player;
    @Mock
    private Player leftNeighbor;
    @Mock
    private Player rightNeighbor;

    @Mock
    private PlayerBoard mockBoard;
    @Mock
    private PlayerMilitary mockMilitary;

    // Static Mock for Logging to keep console clean
    private MockedStatic<Log> logMock;

    @BeforeEach
    void setUp() {
        conflictManager = new ConflictManager();
        logMock = mockStatic(Log.class);

        // Common setup for the main player
        lenient().when(player.getName()).thenReturn("Hero");

        lenient().when(player.getBoard()).thenReturn(mockBoard);
        lenient().when(player.getMilitary()).thenReturn(mockMilitary);

        // Setup Neighborhood (Index 0 = Left, Index 1 = Right)
        Player[] neighbors = new Player[]{leftNeighbor, rightNeighbor};
        lenient().when(player.getNeighborhood()).thenReturn(neighbors);
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    @Test
    @DisplayName("Age I: Victory against both neighbors (Strength 2 vs 1)")
    void testResolveConflicts_AgeI_Victory() {
        // Arrange
        Card.Age currentAge = Card.Age.AGE_I; // Value = 1

        // Player is stronger than both
        when(player.getMilitaryStrength()).thenReturn(2);
        when(leftNeighbor.getMilitaryStrength()).thenReturn(1);
        when(rightNeighbor.getMilitaryStrength()).thenReturn(1);

        Player[] players = new Player[]{player};

        // Act
        conflictManager.resolveConflicts(players, currentAge);

        // Assert
        // Age I: 2*1 - 1 = 1 point per victory.
        // 2 Victories = 2 calls to addConflictPoints(1)
        verify(player, times(2)).addConflictPoints(1);

        // Ensure no defeat logic was triggered
        verify(player, never()).addConflictPoints(-1);
        verify(mockBoard, never()).updateBoardElement(eq("DEFEAT_TOKEN"), anyInt());
    }

    @Test
    @DisplayName("Age II: Victory against one, Tie against other")
    void testResolveConflicts_AgeII_Mixed() {
        // Arrange
        Card.Age currentAge = Card.Age.AGE_II; // Value = 2

        // Player (Strength 5) > Left (3) -> WIN
        // Player (Strength 5) == Right (5) -> TIE
        when(player.getMilitaryStrength()).thenReturn(5);
        when(leftNeighbor.getMilitaryStrength()).thenReturn(3);
        when(rightNeighbor.getMilitaryStrength()).thenReturn(5);

        Player[] players = new Player[]{player};

        // Act
        conflictManager.resolveConflicts(players, currentAge);

        // Assert
        // Win Calculation: Age II (2) -> 2*2 - 1 = 3 points.
        verify(player).addConflictPoints(3);

        // Tie Logic: Should verify NO points added/removed for the tie
        verify(player, times(1)).addConflictPoints(anyInt());
    }

    @Test
    @DisplayName("Age III: Defeat against both neighbors")
    void testResolveConflicts_AgeIII_Defeat() {
        // Arrange
        Card.Age currentAge = Card.Age.AGE_III; // Value = 3

        // Player (Strength 0) < Neighbors (Strength 5)
        when(player.getMilitaryStrength()).thenReturn(0);
        when(leftNeighbor.getMilitaryStrength()).thenReturn(5);
        when(rightNeighbor.getMilitaryStrength()).thenReturn(5);

        Player[] players = new Player[]{player};

        // Act
        conflictManager.resolveConflicts(players, currentAge);

        // Assert
        // Defeat Logic: -1 point per loss (x2)
        verify(player, times(2)).addConflictPoints(-1);

        // Verify Board Visual Update
        verify(mockBoard, times(2)).updateBoardElement("DEFEAT_TOKEN", 1);

        // Verify Military Component Update
        verify(mockMilitary, times(2)).addDefeatToken();
    }

    @Test
    @DisplayName("Complex Scenario: Win Left, Lose Right (Age I)")
    void testResolveConflicts_WinLose() {
        // Arrange
        Card.Age currentAge = Card.Age.AGE_I; // Value = 1

        // Player (2) > Left (1) -> WIN (+1 pt)
        // Player (2) < Right (3) -> LOSE (-1 pt)
        when(player.getMilitaryStrength()).thenReturn(2);
        when(leftNeighbor.getMilitaryStrength()).thenReturn(1);
        when(rightNeighbor.getMilitaryStrength()).thenReturn(3);

        Player[] players = new Player[]{player};

        // Act
        conflictManager.resolveConflicts(players, currentAge);

        // Assert
        verify(player).addConflictPoints(1); // Win
        verify(player).addConflictPoints(-1); // Loss
        verify(mockMilitary, times(1)).addDefeatToken(); // Only 1 defeat token
    }

    @Test
    @DisplayName("Ensure Log events are triggered")
    void testLogging() {
        // Arrange
        when(player.getMilitaryStrength()).thenReturn(5);
        when(leftNeighbor.getMilitaryStrength()).thenReturn(1); // Win
        when(rightNeighbor.getMilitaryStrength()).thenReturn(1); // Win
        Player[] players = new Player[]{player};

        // Act
        conflictManager.resolveConflicts(players, Card.Age.AGE_I);

        // Assert
        logMock.verify(() -> Log.logEvent(contains("Military conflicts")));
        logMock.verify(() -> Log.logEvent(contains("wins a military conflict")), times(2));
    }
}
