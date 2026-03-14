package fr.uca.miage.sevenwonders.services;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.player.PlayerMilitary;
import fr.uca.miage.sevenwonders.models.player.PlayerResources;
import fr.uca.miage.sevenwonders.models.player.PlayerScience;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreCalculatorTest {

    @InjectMocks
    private ScoreCalculator scoreCalculator;

    @Mock
    private Player player;
    @Mock
    private PlayerResources resources;
    @Mock
    private PlayerScience science;
    @Mock
    private PlayerMilitary military;

    @BeforeEach
    void setUp() {
        // Since computeFinalScore calls getters on Player, we stub them.
        // We only need these strict stubs for the "computeFinalScore" tests.
        // The science calculation tests are static and don't use these mocks.
    }

    // -------------------------------------------------------------------------
    // 1. Static Science Score Logic Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Science: Basic Set (1 of each) -> 1^2 + 1^2 + 1^2 + 7 = 10")
    void testCalculateScienceScore_BasicSet() {
        // 1 Tablet, 1 Compass, 1 Wheel, 0 Wildcards
        int score = ScoreCalculator.calculateScienceScore(1, 1, 1, 0);
        assertEquals(10, score);
    }

    @Test
    @DisplayName("Science: No Sets (2 Tablets) -> 2^2 = 4")
    void testCalculateScienceScore_NoSet() {
        int score = ScoreCalculator.calculateScienceScore(2, 0, 0, 0);
        assertEquals(4, score);
    }

    @Test
    @DisplayName("Science: Maximize Wildcard (1 T, 1 C, 0 W + 1 Wildcard)")
    void testCalculateScienceScore_WithWildcard() {
        // Current: 1 Tablet, 1 Compass, 0 Wheel.
        // Best use of wildcard is to be a Wheel -> completes a set.
        // Result: 1, 1, 1 -> 10 points.
        int score = ScoreCalculator.calculateScienceScore(1, 1, 0, 1);
        assertEquals(10, score);
    }

    @Test
    @DisplayName("Science: Complex Wildcard Optimization")
    void testCalculateScienceScore_Complex() {
        // 2 Tablets, 2 Compass, 1 Wheel + 1 Wildcard
        // Option A (Wildcard = Wheel): 2, 2, 2 -> 4+4+4 + (7*2) = 26 points
        // Option B (Wildcard = Tablet): 3, 2, 1 -> 9+4+1 + (7*1) = 21 points
        // Winner should be Option A
        int score = ScoreCalculator.calculateScienceScore(2, 2, 1, 1);
        assertEquals(26, score);
    }

    // -------------------------------------------------------------------------
    // 2. Player Integration Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Compute Final Score: Aggregates Treasury, Science, and Military")
    void testComputeFinalScore() {
        // --- ARRANGE ---

        // 1. Stub Component Accessors
        when(player.getResources()).thenReturn(resources);
        when(player.getScience()).thenReturn(science);
        when(player.getMilitary()).thenReturn(military);

        // 2. Stub Treasury Data
        // 3 Gold + 4 Silver.
        // Points = Gold(3) + Silver(4)/3 = 3 + 1 = 4 points.
        when(resources.getGold()).thenReturn(3);
        when(resources.getSilver()).thenReturn(4);

        // 3. Stub Science Data
        when(science.calculateScore()).thenReturn(16);

        // 4. Stub Military Data
        when(military.getConflictPoints()).thenReturn(5);

        // 5. Stub Final Summation
        // The method calls player.getTotalVictoryPoints() at the end to return the
        // result.
        // We simulate that the player has accumulated 100 points total from all
        // sources.
        when(player.getTotalVictoryPoints()).thenReturn(100);

        // --- ACT ---
        int finalScore = scoreCalculator.computeFinalScore(player);

        // --- ASSERT ---

        // Verify updates were pushed to the player
        verify(player).updateVictoryPoints(4, Effect.Category.TREASURY); // 3 + 4/3 = 4
        verify(player).updateVictoryPoints(16, Effect.Category.SCIENCE);
        verify(player).updateVictoryPoints(5, Effect.Category.MILITARY);

        // Verify the return value matches what the player reports
        assertEquals(100, finalScore);
    }
}
