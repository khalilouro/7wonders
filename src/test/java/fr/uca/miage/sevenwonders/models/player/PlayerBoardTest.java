package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.PurpleEffect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerBoardTest {

    private PlayerBoard playerBoard;

    @Mock
    private Wonder mockWonder;
    @Mock
    private Card mockCard;
    @Mock
    private Player mockLeftNeighbor;
    @Mock
    private Player mockRightNeighbor;
    @Mock
    private Effect.Discount mockDiscount;
    @Mock
    private PurpleEffect mockPurpleEffect;

    @BeforeEach
    void setUp() {
        playerBoard = new PlayerBoard(mockWonder);
    }

    @Test
    @DisplayName("Constructor initializes board state correctly")
    void testConstructor() {
        // Verify Wonder is set
        assertEquals(mockWonder, playerBoard.getWonder());

        // Verify Hand is empty
        assertNotNull(playerBoard.getHand());
        assertTrue(playerBoard.getHand().isEmpty());

        // Verify Played Cards is empty
        assertNotNull(playerBoard.getAlreadyBuilt());
        assertTrue(playerBoard.getAlreadyBuilt().isEmpty());

        // Verify Board Elements initialized to 0
        Map<String, Integer> elements = playerBoard.getBoardElements();
        assertNotNull(elements);
        assertEquals(0, elements.get("BLUE"));
        assertEquals(0, elements.get("RED"));
        assertEquals(0, elements.get("DEFEAT_TOKEN"));
        assertEquals(0, elements.get("BUILT_WONDER_STAGES"));

        // Ensure all keys from init are present
        String[] expectedKeys = {"BROWN", "GREY", "BLUE", "GREEN", "RED", "GOLDEN", "PURPLE", "DEFEAT_TOKEN",
                "BUILT_WONDER_STAGES"};
        for (String key : expectedKeys) {
            assertTrue(elements.containsKey(key), "Map should contain key: " + key);
        }
    }

    @Test
    @DisplayName("Hand Management: Set, Add, Remove")
    void testHandManagement() {
        // 1. Set Hand
        List<Card> newHand = new ArrayList<>();
        newHand.add(mockCard);
        playerBoard.setHand(newHand);

        assertEquals(1, playerBoard.getHand().size());
        assertEquals(mockCard, playerBoard.getHand().get(0));

        // 2. Add Card
        Card anotherCard = mock(Card.class);
        playerBoard.addCard(anotherCard);
        assertEquals(2, playerBoard.getHand().size());

        // 3. Remove Card (Valid Index)
        Card removed = playerBoard.removeCard(0);
        assertEquals(mockCard, removed);
        assertEquals(1, playerBoard.getHand().size());
        assertEquals(anotherCard, playerBoard.getHand().get(0));

        // 4. Remove Card (Invalid Index)
        Card invalid = playerBoard.removeCard(99);
        assertNull(invalid);
        assertEquals(1, playerBoard.getHand().size()); // Size unchanged
    }

    @Test
    @DisplayName("Neighbors: Set and Get")
    void testNeighbors() {
        playerBoard.setNeighborhood(mockLeftNeighbor, mockRightNeighbor);

        assertEquals(mockLeftNeighbor, playerBoard.getLeft());
        assertEquals(mockRightNeighbor, playerBoard.getRight());
    }

    @Test
    @DisplayName("Played Cards: Add and Track Names")
    void testPlayedCards() {
        when(mockCard.getName()).thenReturn("Altar");

        playerBoard.addAlreadyBuilt(mockCard);

        List<String> built = playerBoard.getAlreadyBuilt();
        assertEquals(1, built.size());
        assertTrue(built.contains("Altar"));
    }

    @Test
    @DisplayName("Board Elements: Update values")
    void testUpdateBoardElement() {
        // Initial check
        assertEquals(0, playerBoard.getBoardElements().get("BLUE"));

        // Update existing key
        playerBoard.updateBoardElement("BLUE", 1);
        assertEquals(1, playerBoard.getBoardElements().get("BLUE"));

        // Accumulate
        playerBoard.updateBoardElement("BLUE", 2);
        assertEquals(3, playerBoard.getBoardElements().get("BLUE"));

        // Update new key (should be added)
        playerBoard.updateBoardElement("NEW_TOKEN", 5);
        assertEquals(5, playerBoard.getBoardElements().get("NEW_TOKEN"));
    }

    @Test
    @DisplayName("Discounts: Add and Get")
    void testDiscounts() {
        assertTrue(playerBoard.getDiscounts().isEmpty());

        playerBoard.addDiscount(mockDiscount);

        assertEquals(1, playerBoard.getDiscounts().size());
        assertEquals(mockDiscount, playerBoard.getDiscounts().get(0));
    }

    @Test
    @DisplayName("Purple Effects: Add and Get")
    void testPurpleEffects() {
        assertTrue(playerBoard.getPurpleEffects().isEmpty());

        playerBoard.addPurpleEffect(mockPurpleEffect);

        assertEquals(1, playerBoard.getPurpleEffects().size());
        assertEquals(mockPurpleEffect, playerBoard.getPurpleEffects().get(0));
    }

    @Test
    @DisplayName("Wonder: Setter updates reference")
    void testWonderSetter() {
        Wonder newWonder = mock(Wonder.class);
        playerBoard.setWonder(newWonder);

        assertEquals(newWonder, playerBoard.getWonder());
    }
}
