package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Effect sealed interface and its record
 * implementations. Tests the interaction of Effect with Player and Wonder
 * classes using real instances.
 */
class EffectIT {

    private Player player;
    private Wonder wonder;

    /**
     * Setup method to initialize Player and Wonder before each test.
     */
    @BeforeEach
    void setUp() {
        Bank.getInstance().reset();

        wonder = new Wonder("Rhodes", Wonder.Side.A, Card.Materials.WOOD);
        player = new Player("TestPlayer");
        player.setWonderplayer(wonder);

        // RESET starting gold for clean slate in integration tests.
        player.getResources().setGold(0);
        player.getResources().setSilver(0);
    }

    /**
     * Tests applying a VictoryPoints effect to a player.
     */
    @Test
    void testVictoryPointsIntegration() {
        Effect.VictoryPoints victoryEffect = new Effect.VictoryPoints(5);
        int initialVictoryPoints = player.getTotalVictoryPoints();

        victoryEffect.apply(player);

        assertEquals(initialVictoryPoints + 5, player.getTotalVictoryPoints(),
                "The victory points effect should correctly increase player's victory points");
    }

    /**
     * Tests applying a Production effect to a player.
     */
    @Test
    void testProductionMaterialsIntegration() {
        Card.Materials[] materials = {Card.Materials.WOOD};
        Effect.Production productionEffect = new Effect.Production.Fixed(materials);

        productionEffect.apply(player);

        Effect.Production secondEffect = new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.STONE});
        secondEffect.apply(player);

        assertTrue(player.getResources().getProduction().stream().anyMatch(list -> list.contains(Card.Materials.WOOD)),
                "Player should have WOOD production.");
        assertTrue(player.getResources().getProduction().stream().anyMatch(list -> list.contains(Card.Materials.STONE)),
                "Player should have STONE production.");

        // Production should not affect the final score/VP until final calculation
        assertEquals(0, player.getTotalVictoryPoints(), "Production should not affect victory points directly");
    }

    /**
     * Tests applying multiple effects to a player.
     */
    @Test
    void testMultipleEffectsIntegration() {
        Effect.VictoryPoints victoryEffect = new Effect.VictoryPoints(3);
        Effect.Production productionEffect = new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.WOOD});

        int initialVictoryPoints = player.getTotalVictoryPoints();

        victoryEffect.apply(player);
        productionEffect.apply(player);

        assertEquals(initialVictoryPoints + 3, player.getTotalVictoryPoints(),
                "Only victoryPoints should affect the victory points total");
        assertTrue(player.getResources().getProduction().stream().anyMatch(list -> list.contains(Card.Materials.WOOD)));
    }

    /**
     * Tests that applying Military, Gold, and Science effects alter player state
     * correctly.
     */
    @Test
    void testAppliedEffectsIntegration() {
        // Initial state (from setUp): G=0, S=0 (Total Value = 0 silver)
        int initialMilitary = player.getMilitaryStrength();

        new Effect.Military(3).apply(player); // Military effect
        new Effect.Gold(5).apply(player); // Gold effect (adds 5 silver value)
        new Effect.Science(Effect.Science.ScienceSymbol.COMPASS).apply(player); // Science effect

        // 1. Military Check
        assertEquals(initialMilitary + 3, player.getMilitaryStrength(), "Military effect should increase strength");

        // 2. Gold Check:
        // Initial Value = 0. Added Value = 5. Final Value = 5.
        // Conversion: 5 silver value -> 1 gold (3 value) + 2 silver (2 value).
        assertEquals(1, player.getResources().getGold(),
                "Player's gold should convert to 1 after receiving 5 silver value (5 silver = 1 gold + 2 silver).");
        assertEquals(2, player.getResources().getSilver(), "Player's silver should be 2 after conversion.");

        // 3. Science Check
        player.computeFinalScore();
        // Player starts with 0 science. Adds 1 COMPASS. Score should be 1*1 = 1.
        assertEquals(1, player.calculateScienceScore(), "Science effect should increase science points");
    }

    /**
     * Tests that applying a null player will result in an IllegalArgumentException.
     */
    @Test
    void testNullPlayerThrowsException() {
        Effect.VictoryPoints effect = new Effect.VictoryPoints(5);

        assertThrows(IllegalArgumentException.class, () -> effect.apply(null),
                "Null player should throw IllegalArgumentException as per the defensive check in Effect.VictoryPoints.");
    }
}
