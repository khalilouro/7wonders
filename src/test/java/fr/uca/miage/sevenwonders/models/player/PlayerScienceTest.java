package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.services.ScoreCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerScienceTest {

    private PlayerScience playerScience;
    private MockedStatic<ScoreCalculator> scoreCalculatorMock;

    @BeforeEach
    void setUp() {
        playerScience = new PlayerScience();
        // Mock the static calculator to isolate PlayerScience logic
        scoreCalculatorMock = mockStatic(ScoreCalculator.class);
    }

    @AfterEach
    void tearDown() {
        // Always close static mocks to avoid memory leaks or interfering with other
        // tests
        scoreCalculatorMock.close();
    }

    @Test
    @DisplayName("Initial state should be empty (0 for all symbols)")
    void testInitialState() {
        assertEquals(0, playerScience.getTablet());
        assertEquals(0, playerScience.getCompass());
        assertEquals(0, playerScience.getWheel());
        assertEquals(0, playerScience.getAnyScience());
    }

    @Test
    @DisplayName("addSymbol should increment the correct counters")
    void testAddSymbol() {
        // Add one of each
        playerScience.addSymbol(Effect.Science.ScienceSymbol.TABLET);
        playerScience.addSymbol(Effect.Science.ScienceSymbol.COMPASS);
        playerScience.addSymbol(Effect.Science.ScienceSymbol.WHEEL);
        playerScience.addSymbol(Effect.Science.ScienceSymbol.ANY);

        // Add a second Tablet
        playerScience.addSymbol(Effect.Science.ScienceSymbol.TABLET);

        assertEquals(2, playerScience.getTablet());
        assertEquals(1, playerScience.getCompass());
        assertEquals(1, playerScience.getWheel());
        assertEquals(1, playerScience.getAnyScience());
    }

    @Test
    @DisplayName("addSymbol with null should do nothing")
    void testAddSymbol_Null() {
        playerScience.addSymbol(null);

        assertEquals(0, playerScience.getTablet());
        assertEquals(0, playerScience.getCompass());
        assertEquals(0, playerScience.getWheel());
        assertEquals(0, playerScience.getAnyScience());
    }

    @Test
    @DisplayName("calculateScore should delegate to ScoreCalculator with correct values")
    void testCalculateScore_Delegation() {
        // Arrange
        playerScience.addSymbol(Effect.Science.ScienceSymbol.TABLET);
        playerScience.addSymbol(Effect.Science.ScienceSymbol.TABLET); // 2
        playerScience.addSymbol(Effect.Science.ScienceSymbol.COMPASS); // 1
        playerScience.addSymbol(Effect.Science.ScienceSymbol.ANY); // 1
        // Wheel = 0

        // Define expected return from the utility
        int expectedScore = 42;
        scoreCalculatorMock.when(() -> ScoreCalculator.calculateScienceScore(2, 1, 0, 1)).thenReturn(expectedScore);

        // Act
        int result = playerScience.calculateScore();

        // Assert
        assertEquals(expectedScore, result);

        // Verify the static method was called with exactly the counts we stored
        scoreCalculatorMock.verify(() -> ScoreCalculator.calculateScienceScore(2, 1, 0, 1));
    }
}
