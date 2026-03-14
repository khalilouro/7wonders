package fr.uca.miage.sevenwonders.models.card;

import fr.uca.miage.sevenwonders.models.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Import Mockito for mocking

/**
 * Unit tests for the {@link Card} class. Ensures the Card acts correctly as a
 * data structure, isolating dependencies (Cost and Effect) using mocks.
 */
class CardTest {

    private Cost mockCost;
    private Effect mockEffect;

    /**
     * Sets up mock {@link Cost} and {@link Effect} objects before each test.
     */
    @BeforeEach
    void setUp() {
        // We use Cost.Gold.class, which is a record (a non-abstract final class).
        mockCost = mock(Cost.Gold.class);

        // Mock the sealed Effect interface (assuming a previous fix for Effect has
        // already
        // been applied or is handled by a similar workaround if needed, but the current
        // error points only to Cost). We will keep it as 'Effect.class' for now,
        // but if it fails again, it needs to mock a concrete Effect record.
        // For robustness, let's mock a concrete Effect record as well.
        mockEffect = mock(Effect.VictoryPoints.class);
    }

    /**
     * Tests the primary 5-argument constructor that includes a name, cost, age,
     * color, and effect. Verifies that all fields are correctly assigned.
     */
    @Test
    void testFullCardCreation() {
        // Setup
        String name = "Test Card";
        Card.Age age = Card.Age.AGE_I;
        Card.Color color = Card.Color.GREEN;

        // Action
        String[] parents = new String[0];
        Card card = new Card(name, mockCost, age, color, mockEffect, parents, null);

        // Assertion
        assertNotNull(card, "Card object should not be null.");
        assertEquals(name, card.getName(), "The 'name' field should be the one passed to the constructor.");
        // Use assertSame to verify the exact mock object was stored
        assertSame(mockCost, card.getCost(), "The 'cost' field should be the one passed to the constructor.");
        assertSame(age, card.getAge(), "The 'age' field should be the one passed to the constructor.");
        assertSame(color, card.getColor(), "The 'color' field should be the one passed to the constructor.");
        assertSame(mockEffect, card.getEffect(), "The 'effect' field should be the one passed to the constructor.");
    }

    /**
     * Tests the constructor for a card with no effect (passing null for the
     * effect).
     */
    @Test
    void testCardCreationWithNullEffect() {
        // Setup
        String name = "Basic Card";
        Card.Age age = Card.Age.AGE_II;
        Card.Color color = Card.Color.BROWN;

        // Action
        String[] parents = new String[0];
        Card card = new Card(name, mockCost, age, color, null, parents, null);

        // Assertion
        assertNotNull(card);
        assertSame(mockCost, card.getCost(), "The 'cost' field should be the one passed to the constructor.");
        assertSame(age, card.getAge(), "The 'age' field should be the one passed to the constructor.");
        assertSame(color, card.getColor(), "The 'color' field should be the one passed to the constructor.");
        assertNull(card.getEffect(), "The 'effect' field should be null.");
    }

    /**
     * Verifies that a {@link Card} can be successfully instantiated with a
     * {@code null} cost.
     */
    @Test
    void testCardCreationWithNullCost() {
        // Action

        String[] parents = new String[0];
        Card card = new Card("No Cost Card", null, Card.Age.AGE_I, Card.Color.BLUE, mockEffect, parents, null);

        // Assertion
        assertNotNull(card);
        assertNull(card.getCost(), "The 'cost' field should be null as passed to the constructor.");
        assertSame(mockEffect, card.getEffect(), "The 'effect' field should not be null.");
    }

    /**
     * Verifies that all nested enums (Age, Color, Materials) are accessible and
     * functional.
     */
    @Test
    void testEnumAccessibility() {
        // Access Age enum
        assertNotNull(Card.Age.AGE_I, "Age.AGE_I should not be null.");
        assertEquals(1, Card.Age.AGE_I.getValue(), "Age.AGE_I value should be 1.");

        // Access Color enum
        Card.Color brown = Card.Color.BROWN;
        assertNotNull(brown, "Color.BROWN should not be null.");
        assertEquals("BROWN", brown.toString(), "Color.BROWN toString() should return 'BROWN'.");

        // Access Materials enum
        assertNotNull(Card.Materials.WOOD, "Materials.WOOD should not be null.");
    }
}
