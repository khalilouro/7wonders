package fr.uca.miage.sevenwonders.models.wonder;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WonderTest {

    @Mock
    private Player player;
    @Mock
    private WonderStage stage1;
    @Mock
    private WonderStage stage2;

    private Wonder wonder;
    private WonderStage[] stages;

    @BeforeEach
    void setUp() {
        stages = new WonderStage[]{stage1, stage2};
        // Create a Wonder: "Giza", Starts with STONE, 2 Stages, Side A
        wonder = new Wonder("Giza", Card.Materials.STONE, stages, Wonder.Side.A);
    }

    @Test
    @DisplayName("Constructor initializes attributes correctly")
    void testConstructor() {
        assertEquals("Giza", wonder.getName());
        assertEquals(Card.Materials.STONE, wonder.getStartingResource());
        assertEquals(Wonder.Side.A, wonder.getCurrentSide());
        assertEquals(2, wonder.getTotalStages());
        assertEquals(0, wonder.getStageIndex(), "Wonder should start at stage index 0");
        assertFalse(wonder.isCompleted(), "Wonder should not be completed initially");
    }

    @Test
    @DisplayName("canBuildStage delegates to the current WonderStage")
    void testCanBuildStage() {
        // Arrange: Stage 1 allows build
        when(stage1.canBuild(player)).thenReturn(true);

        // Act & Assert
        assertTrue(wonder.canBuildStage(player), "Should return true if current stage allows building");
        verify(stage1).canBuild(player);
    }

    @Test
    @DisplayName("canBuildStage returns false if Wonder is already completed")
    void testCanBuildStage_WhenCompleted() {
        // Arrange: Manually advance to completion using package-private method
        wonder.advanceStage(); // Index 0 -> 1
        wonder.advanceStage(); // Index 1 -> 2 (Completed)

        // Act & Assert
        assertTrue(wonder.isCompleted());
        assertFalse(wonder.canBuildStage(player), "Should return false if wonder is completed");
    }

    @Test
    @DisplayName("getNextStageToBuild returns correct stage")
    void testGetNextStageToBuild() {
        // Initially Stage 1 (index 0)
        assertEquals(stage1, wonder.getNextStageToBuild());
        assertEquals(stage1, wonder.getCurrentStage());

        // Advance
        wonder.advanceStage();

        // Now Stage 2 (index 1)
        assertEquals(stage2, wonder.getNextStageToBuild());
        assertEquals(stage2, wonder.getCurrentStage());

        // Advance to finish
        wonder.advanceStage();

        // Completed -> null
        assertNull(wonder.getNextStageToBuild());
        assertNull(wonder.getCurrentStage());
    }

    @Test
    @DisplayName("advanceStage increments index and updates completion status")
    void testAdvanceStage() {
        // Start: Index 0
        assertEquals(0, wonder.getStageIndex());

        // Advance 1
        wonder.advanceStage();
        assertEquals(1, wonder.getStageIndex());
        assertFalse(wonder.isCompleted());

        // Advance 2 (Max)
        wonder.advanceStage();
        assertEquals(2, wonder.getStageIndex());
        assertTrue(wonder.isCompleted());

        // Advance again (Should ignore or handle gracefully)
        wonder.advanceStage();
        assertEquals(2, wonder.getStageIndex(), "Should not advance beyond max stages");
    }

    @Test
    @DisplayName("buildStage delegates to WonderBuilder")
    void testBuildStage_Delegation() {
        // We need to mock the construction of WonderBuilder since it is instantiated
        // inside the method
        try (MockedConstruction<WonderBuilder> mockedBuilder = mockConstruction(WonderBuilder.class,
                (mock, context) -> {
                    // Configure the mock that will be created
                    when(mock.buildStage(wonder, player)).thenReturn(true);
                })) {

            // Act
            boolean result = wonder.buildStage(player);

            // Assert
            assertTrue(result);

            // Verify that the mocked WonderBuilder was used
            assertEquals(1, mockedBuilder.constructed().size());
            WonderBuilder mock = mockedBuilder.constructed().get(0);
            verify(mock).buildStage(wonder, player);
        }
    }

    @Test
    @DisplayName("Side Management (Getter/Setter)")
    void testSideManagement() {
        assertEquals(Wonder.Side.A, wonder.getCurrentSide());

        wonder.setCurrentSide(Wonder.Side.B);
        assertEquals(Wonder.Side.B, wonder.getCurrentSide());
    }
}
