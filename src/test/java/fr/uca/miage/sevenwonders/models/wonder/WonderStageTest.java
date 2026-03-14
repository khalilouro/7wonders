package fr.uca.miage.sevenwonders.models.wonder;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WonderStageTest {

    // We mock concrete implementations because Cost and Effect are sealed
    // interfaces
    @Mock
    private Cost.Gold mockCost;
    @Mock
    private Effect.VictoryPoints mockEffect;
    @Mock
    private Player mockPlayer;

    @Test
    @DisplayName("Constructor should store costs and effects correctly")
    void testConstructorWithEffects() {
        Effect[] effects = new Effect[]{mockEffect};

        // Act
        WonderStage stage = new WonderStage(mockCost, effects);

        // Assert
        assertEquals(mockCost, stage.getCosts(), "Cost should match the provided object");
        assertNotNull(stage.getEffects(), "Effects array should not be null");
        assertArrayEquals(effects, stage.getEffects(), "Effects should match the provided array");
        assertEquals(1, stage.getEffects().length);
    }

    @Test
    @DisplayName("Constructor should handle null effects array by creating empty array")
    void testConstructorWithNullEffects() {
        // Act: Pass null for effects
        WonderStage stage = new WonderStage(mockCost, null);

        // Assert
        assertEquals(mockCost, stage.getCosts());
        assertNotNull(stage.getEffects(), "Effects array should default to empty, not null");
        assertEquals(0, stage.getEffects().length, "Effects array should be empty");
    }

    @Test
    @DisplayName("Secondary constructor (Cost only) should initialize with empty effects")
    void testSecondaryConstructor() {
        // Act
        WonderStage stage = new WonderStage(mockCost);

        // Assert
        assertEquals(mockCost, stage.getCosts());
        assertNotNull(stage.getEffects());
        assertEquals(0, stage.getEffects().length);
    }

    @Test
    @DisplayName("canBuild returns false when player is null")
    void testCanBuild_NullPlayer() {
        WonderStage stage = new WonderStage(mockCost);

        // Act & Assert
        assertFalse(stage.canBuild(null), "Should return false if player is null");
    }

    @Test
    @DisplayName("canBuild returns true for valid player (Default implementation)")
    void testCanBuild_ValidPlayer() {
        WonderStage stage = new WonderStage(mockCost);

        // Act & Assert
        // Current implementation is a stub returning true for any non-null player
        assertTrue(stage.canBuild(mockPlayer), "Should return true for a valid player instance");
    }
}
