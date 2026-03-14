package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerResourcesTest {

    private PlayerResources resources;

    @BeforeEach
    void setUp() {
        resources = new PlayerResources();
    }

    @Test
    @DisplayName("Constructor initializes empty resources")
    void testConstructor() {
        assertEquals(0, resources.getGold());
        assertEquals(0, resources.getSilver());
        assertNotNull(resources.getProduction());
        assertTrue(resources.getProduction().isEmpty());
    }

    // -------------------------------------------------------------------------
    // GOLD TESTS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Gold management (Set, Add, Remove)")
    void testGoldManagement() {
        // Set
        resources.setGold(10);
        assertEquals(10, resources.getGold());

        // Add
        resources.addGold(5);
        assertEquals(15, resources.getGold());

        // Remove
        resources.removeGold(5);
        assertEquals(10, resources.getGold());
    }

    @Test
    @DisplayName("removeGold should not result in negative values")
    void testRemoveGold_NegativeCheck() {
        resources.setGold(5);

        // Remove more than available
        resources.removeGold(10);

        // Should be 0, not -5
        assertEquals(0, resources.getGold());
    }

    // -------------------------------------------------------------------------
    // SILVER TESTS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Silver management (Set, Add)")
    void testSilverManagement() {
        // Set
        resources.setSilver(5);
        assertEquals(5, resources.getSilver());

        // Add
        resources.addSilver(3);
        assertEquals(8, resources.getSilver());
    }

    // -------------------------------------------------------------------------
    // PRODUCTION MATERIALS TESTS
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addProductionMaterial adds a single resource option")
    void testAddProductionMaterial() {
        Card.Materials mat = Card.Materials.WOOD;

        resources.addProductionMaterial(mat);

        List<List<Card.Materials>> prod = resources.getProduction();
        assertEquals(1, prod.size());
        assertEquals(1, prod.get(0).size());
        assertEquals(mat, prod.get(0).get(0));
    }

    @Test
    @DisplayName("addProductionMaterials adds multiple complex options")
    void testAddProductionMaterials_Complex() {
        // Create a hybrid choice (e.g., Clay OR Ore)
        List<Card.Materials> option1 = new ArrayList<>();
        option1.add(Card.Materials.CLAY);

        List<Card.Materials> option2 = new ArrayList<>();
        option2.add(Card.Materials.ORE);

        List<List<Card.Materials>> newOptions = new ArrayList<>();
        newOptions.add(option1);
        newOptions.add(option2);

        resources.addProductionMaterials(newOptions);

        List<List<Card.Materials>> prod = resources.getProduction();
        assertEquals(2, prod.size());
        assertTrue(prod.contains(option1));
        assertTrue(prod.contains(option2));
    }
}
