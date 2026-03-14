package fr.uca.miage.sevenwonders.models.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMilitaryTest {

    private PlayerMilitary military;

    @BeforeEach
    void setUp() {
        military = new PlayerMilitary();
    }

    @Test
    @DisplayName("Initial state should be zero")
    void testInitialState() {
        assertEquals(0, military.getStrength(), "Initial military strength should be 0");
        assertEquals(0, military.getConflictPoints(), "Initial conflict points should be 0");
        assertEquals(0, military.getDefeatTokens(), "Initial defeat tokens should be 0");
    }

    @Test
    @DisplayName("addStrength correctly updates military strength")
    void testAddStrength() {
        // Add 1 shield
        military.addStrength(1);
        assertEquals(1, military.getStrength());

        // Add 2 more shields
        military.addStrength(2);
        assertEquals(3, military.getStrength());
    }

    @Test
    @DisplayName("addConflictPoints correctly updates victory points from conflict")
    void testAddConflictPoints() {
        // Win Age I conflict (1 point)
        military.addConflictPoints(1);
        assertEquals(1, military.getConflictPoints());

        // Win Age II conflict (3 points)
        military.addConflictPoints(3);
        assertEquals(4, military.getConflictPoints());

        // Handle negative points (Defeat usually adds tokens, but some variants might
        // subtract points directly)
        military.addConflictPoints(-1);
        assertEquals(3, military.getConflictPoints());
    }

    @Test
    @DisplayName("addDefeatToken increments the defeat token counter")
    void testAddDefeatToken() {
        military.addDefeatToken();
        assertEquals(1, military.getDefeatTokens());

        military.addDefeatToken();
        assertEquals(2, military.getDefeatTokens());
    }
}
