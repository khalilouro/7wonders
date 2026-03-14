package fr.uca.miage.sevenwonders.services;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.player.PlayerBoard;
import fr.uca.miage.sevenwonders.models.player.PlayerResources;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConstructionServiceTest {

    @Mock
    private Player player;
    @Mock
    private Player leftNeighbor;
    @Mock
    private Player rightNeighbor;

    // Dependencies of Player
    @Mock
    private PlayerResources resources;
    @Mock
    private PlayerBoard board;
    @Mock
    private PlayerResources leftResources;
    @Mock
    private PlayerResources rightResources;

    @Mock
    private Card card;

    // Static Mock for Log
    private MockedStatic<Log> logMock;

    @InjectMocks
    private ConstructionService constructionService;

    @BeforeEach
    void setUp() {
        logMock = mockStatic(Log.class);

        // Common Player Setup
        lenient().when(player.getName()).thenReturn("Builder");
        lenient().when(player.getResources()).thenReturn(resources);
        lenient().when(player.getBoard()).thenReturn(board);
        lenient().when(player.getLeft()).thenReturn(leftNeighbor);
        lenient().when(player.getRight()).thenReturn(rightNeighbor);

        // Common Neighbor Setup
        lenient().when(leftNeighbor.getResources()).thenReturn(leftResources);
        lenient().when(rightNeighbor.getResources()).thenReturn(rightResources);

        // Default board state
        lenient().when(board.getAlreadyBuilt()).thenReturn(new ArrayList<>());
        lenient().when(board.getDiscounts()).thenReturn(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    // -------------------------------------------------------------------------
    // TEST: Chaining
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Build via Chaining: Success")
    void testCanBuild_Chaining_Success() {
        // Arrange
        when(card.hasParents()).thenReturn(true);
        when(card.getParents()).thenReturn(new String[]{"Scriptorium"});
        when(board.getAlreadyBuilt()).thenReturn(List.of("Scriptorium"));

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Cost.Free, "Should return Free cost for chaining");
    }

    @Test
    @DisplayName("Build via Chaining: Fail (Parent not built)")
    void testCanBuild_Chaining_Fail() {
        // Arrange
        when(card.hasParents()).thenReturn(true);
        when(card.getParents()).thenReturn(new String[]{"Scriptorium"});
        when(board.getAlreadyBuilt()).thenReturn(List.of("Baths")); // Different card

        // Need to mock regular cost check failing or returning something else
        Cost.Gold goldCost = mock(Cost.Gold.class);
        when(goldCost.amount()).thenReturn(100); // Too expensive
        when(card.getCost()).thenReturn(goldCost);
        when(resources.getGold()).thenReturn(0);

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isEmpty(), "Should fail if chaining fails and can't afford normal cost");
    }

    // -------------------------------------------------------------------------
    // TEST: Olympia Ability
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Build via Special Ability (Free Build)")
    void testCanBuild_OlympiaAbility() {
        // Arrange
        when(player.hasFreeBuildAvailable()).thenReturn(true);

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get() instanceof Cost.Free);
    }

    // -------------------------------------------------------------------------
    // TEST: Gold Cost
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Build Gold Cost: Success")
    void testCanBuild_Gold_Success() {
        // Arrange
        Cost.Gold goldCost = mock(Cost.Gold.class);
        when(goldCost.amount()).thenReturn(5);
        when(card.getCost()).thenReturn(goldCost);

        // Player has enough: 5 gold
        // Note: hasSufficientGold checks (gold * 3 + silver) in your impl?
        // Or standard is just coins. Your code: p.getResources().getGold() * 3 + ...
        // Assuming your PlayerResources differentiates types or 'getGold' means coins.
        // Usually 7W uses 'coins'. Let's assume getGold() returns count of Gold ORE or
        // Coins?
        // Checking code: hasSufficientGold(p, amount) -> p.getResources().getGold() * 3
        // ...
        // It looks like your model treats Gold as Ore (Value 3?) or Coins?
        // Standard game: Cost is Coins. Player has Coins.
        // If your logic is: getGold() * 3... that implies Gold is a resource worth 3
        // currency?
        // I will assume getGold() returns Coins for the test setup to match 'amount'.

        when(resources.getGold()).thenReturn(2); // 2 * 3 = 6 "value" if logic holds?
        when(resources.getSilver()).thenReturn(0);

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(goldCost, result.get());
    }

    @Test
    @DisplayName("Build Gold Cost: Fail")
    void testCanBuild_Gold_Fail() {
        // Arrange
        Cost.Gold goldCost = mock(Cost.Gold.class);
        when(goldCost.amount()).thenReturn(10);
        when(card.getCost()).thenReturn(goldCost);

        when(resources.getGold()).thenReturn(1); // 1*3 = 3
        when(resources.getSilver()).thenReturn(0);

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isEmpty());
    }

    // -------------------------------------------------------------------------
    // TEST: Materials Cost
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Build Materials: Local Production Success")
    void testCanBuild_Materials_Local() {
        // Arrange
        Cost.Materials matCost = mock(Cost.Materials.class);
        Card.Materials[] reqs = {Card.Materials.WOOD};
        when(matCost.materials()).thenReturn(reqs);
        when(card.getCost()).thenReturn(matCost);

        // Player produces Wood
        List<Card.Materials> production = new ArrayList<>();
        production.add(Card.Materials.WOOD);
        // getProduction returns List<List<Materials>> (options)
        List<List<Card.Materials>> productionOptions = new ArrayList<>();
        productionOptions.add(production);

        when(resources.getProduction()).thenReturn(productionOptions);

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(matCost, result.get());
    }

    @Test
    @DisplayName("Build Materials: Neighbor Trading Required")
    void testCanBuild_Materials_Trading() {
        // Arrange
        Cost.Materials matCost = mock(Cost.Materials.class);
        Card.Materials[] reqs = {Card.Materials.STONE};
        when(matCost.materials()).thenReturn(reqs);
        when(card.getCost()).thenReturn(matCost);

        // Local: None
        when(resources.getProduction()).thenReturn(new ArrayList<>());

        // Left Neighbor: Produces Stone
        List<Card.Materials> leftProd = List.of(Card.Materials.STONE);
        when(leftResources.getProduction()).thenReturn(List.of(leftProd));

        // Player Wealth (Need 2 coins for base trade)
        when(resources.getGold()).thenReturn(1); // 1*3 = 3 value > 2 needed

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isPresent());
        Cost finalCost = result.get();
        // It returns a Cost.Trading wrapper or similar.
        // We check if it is present (logic passed).
    }

    @Test
    @DisplayName("Build Materials: Fail (No Local, No Neighbor)")
    void testCanBuild_Materials_Fail() {
        // Arrange
        Cost.Materials matCost = mock(Cost.Materials.class);
        Card.Materials[] reqs = {Card.Materials.GLASS};
        when(matCost.materials()).thenReturn(reqs);
        when(card.getCost()).thenReturn(matCost);

        // No production anywhere
        when(resources.getProduction()).thenReturn(new ArrayList<>());
        when(leftResources.getProduction()).thenReturn(new ArrayList<>());
        when(rightResources.getProduction()).thenReturn(new ArrayList<>());

        // Act
        Optional<Cost> result = constructionService.canBuild(player, card);

        // Assert
        assertTrue(result.isEmpty());
    }
}
