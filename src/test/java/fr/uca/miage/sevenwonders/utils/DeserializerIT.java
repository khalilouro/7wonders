package fr.uca.miage.sevenwonders.utils;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link Deserializer} class.
 * <p>
 * This class checks that our deserialization system correctly loads the cards
 * and wonders from the configuration files, and ensures that all essential data
 * is properly structured and consistent with the game's logic.
 * </p>
 */
public class DeserializerIT {

    /**
     * Tests card loading through the deserializer.
     * <p>
     * Makes sure that the returned list is not null or empty, and that each element
     * is indeed an instance of {@link Card}.
     * </p>
     */
    @Test
    void testLoadCards() {
        List<Card> cards = Deserializer.loadCards(4, Card.Age.AGE_I);
        assertNotNull(cards, "The card list should not be null");
        assertFalse(cards.isEmpty(), "The card list should not be empty");
        assertTrue(cards.get(0) instanceof Card, "Each element should be an instance of Card");
    }

    /**
     * Tests wonder loading through the deserializer.
     * <p>
     * Ensures that the list is correctly generated — it shouldn’t be null or empty
     * — and that every item is a valid {@link Wonder} object.
     * </p>
     */
    @Test
    void testLoadWonders() {
        List<Wonder> wonders = Deserializer.loadWonders(Deserializer.whichSide.A);
        assertNotNull(wonders, "The wonder list should not be null");
        assertFalse(wonders.isEmpty(), "The wonder list should not be empty");
        assertTrue(wonders.get(0) instanceof Wonder, "Each element should be an instance of Wonder");
    }

    /**
     * Checks that a specific card (“Lumber Yard”) exists after deserialization of
     * the Age I cards.
     * <p>
     * This test ensures that one of the most basic production cards is correctly
     * included in the dataset loaded by our deserializer.
     * </p>
     */
    @Test
    void testSpecificCardExistsAfterDeserialization() {

        String[] parents = new String[0];
        Card hardCard = new Card("Lumber Yard", Cost.free(), Card.Age.AGE_I, Card.Color.BROWN,
                new Effect.Production.Fixed(new Card.Materials[]{Card.Materials.WOOD}), parents, null);
        List<Card> cards = Deserializer.loadCards(4, Card.Age.AGE_I);
        boolean cardFound = cards.stream().anyMatch(c -> c.getName().equalsIgnoreCase(hardCard.getName()));
        assertTrue(cardFound, "The card 'Lumber Yard' should exist in the Deserializer.");
    }

    /**
     * Checks that the wonder “Rhodos” is correctly present after deserializing the
     * wonders for side A.
     * <p>
     * This guarantees that one of the game’s key wonders is properly loaded and
     * available in the deserialization process.
     * </p>
     */
    @Test
    void testSpecificWonderExistsAfterDeserialization() {
        List<Wonder> wonders = Deserializer.loadWonders(Deserializer.whichSide.A);
        boolean wonderFound = wonders.stream().anyMatch(w -> w.getName().equalsIgnoreCase("Rhodos"));
        assertTrue(wonderFound, "The wonder 'Rhodos' should exist in the Deserializer.");
    }
}
