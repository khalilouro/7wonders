package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.services.ConstructionService;
import fr.uca.miage.sevenwonders.services.ScoreCalculator;
import fr.uca.miage.sevenwonders.utils.Log;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerTest {

    private Player player;

    // Mocks for internal components
    @Mock
    private PlayerResources mockResources;
    @Mock
    private PlayerScience mockScience;
    @Mock
    private PlayerMilitary mockMilitary;
    @Mock
    private PlayerBoard mockBoard;

    // Mocks for services
    @Mock
    private ConstructionService mockConstructionService;
    @Mock
    private ScoreCalculator mockScoreCalculator;

    @Mock
    private Wonder mockWonder;
    @Mock
    private Card mockCard;

    private MockedStatic<Log> logMock;

    @BeforeEach
    void setUp() throws Exception {
        logMock = mockStatic(Log.class);

        // Initialize Player with a mock Wonder
        player = new Player("TestPlayer", mockWonder);

        // INJECTION: Use Reflection to replace real components with Mocks
        // This is necessary because Player instantiates them internally using 'new'.
        injectMock("resources", mockResources);
        injectMock("science", mockScience);
        injectMock("military", mockMilitary);
        injectMock("board", mockBoard);
        injectMock("constructionService", mockConstructionService);
        injectMock("scoreCalculator", mockScoreCalculator);
    }

    private void injectMock(String fieldName, Object mock) throws Exception {
        Field field = Player.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(player, mock);
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    // -------------------------------------------------------------------------
    // RESOURCES DELEGATION
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getGoldPoints delegates to resources (Gold + Silver/3)")
    void testGetGoldPoints() {
        when(mockResources.getGold()).thenReturn(5);
        when(mockResources.getSilver()).thenReturn(4);

        // 5 + (4/3) = 5 + 1 = 6
        int points = player.getGoldPoints();

        assertEquals(6, points);
        verify(mockResources).getGold();
        verify(mockResources).getSilver();
    }

    @Test
    @DisplayName("addProductionMaterial delegates to resources")
    void testAddProductionMaterial() {
        Card.Materials mat = Card.Materials.WOOD;
        player.addProductionMaterial(mat);
        verify(mockResources).addProductionMaterial(mat);
    }

    // -------------------------------------------------------------------------
    // BOARD DELEGATION
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Hand manipulation delegates to board")
    void testHandDelegation() {
        List<Card> hand = new ArrayList<>();
        when(mockBoard.getHand()).thenReturn(hand);

        // Test setHand
        player.setHand(hand);
        verify(mockBoard).setHand(hand);

        // Test getHand
        assertEquals(hand, player.getHand());

        // Test addCardToHand
        player.addCardToHand(mockCard, 0);
        verify(mockBoard).addCard(mockCard);
    }

    @Test
    @DisplayName("Neighborhood setters delegate to board")
    void testNeighborhoodDelegation() {
        Player left = mock(Player.class);
        Player right = mock(Player.class);

        player.setNeighborhood(left, right);
        verify(mockBoard).setNeighborhood(left, right);
    }

    // -------------------------------------------------------------------------
    // MILITARY DELEGATION
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Military methods delegate to military component")
    void testMilitaryDelegation() {
        // Test addConflictPoints
        player.addConflictPoints(3);
        verify(mockMilitary).addConflictPoints(3);

        // Test addMilitaryStrength
        player.addMilitaryStrength(1);
        verify(mockMilitary).addStrength(1);

        // Test getters
        when(mockMilitary.getStrength()).thenReturn(5);
        assertEquals(5, player.getMilitaryStrength());
    }

    // -------------------------------------------------------------------------
    // SERVICE DELEGATION
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("canBuild delegates to ConstructionService")
    void testCanBuildDelegation() {
        // FIX: Mock concrete class Cost.Gold instead of sealed interface Cost
        Optional<Cost> costOpt = Optional.of(mock(Cost.Gold.class));

        when(mockConstructionService.canBuild(player, mockCard)).thenReturn(costOpt);

        Optional<Cost> result = player.canBuild(mockCard);

        assertEquals(costOpt, result);
        verify(mockConstructionService).canBuild(player, mockCard);
    }

    @Test
    @DisplayName("computeFinalScore delegates to ScoreCalculator")
    void testComputeFinalScoreDelegation() {
        when(mockScoreCalculator.computeFinalScore(player)).thenReturn(42);

        player.computeFinalScore();

        assertEquals(42, player.getScore());
        verify(mockScoreCalculator).computeFinalScore(player);
    }

    // -------------------------------------------------------------------------
    // VICTORY POINTS LOGIC
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("updateVictoryPoints correctly updates categories")
    void testUpdateVictoryPoints() {
        // 1. Add points to CIVILIAN
        player.addVictoryPoints(10); // Defaults to CIVILIAN
        assertEquals(10, player.getPointsByCategory(Effect.Category.CIVILIAN));

        // 2. Add more points to CIVILIAN (should accumulate)
        player.addVictoryPoints(5, Effect.Category.CIVILIAN);
        assertEquals(15, player.getPointsByCategory(Effect.Category.CIVILIAN));

        // 3. Set points for SCIENCE (Should overwrite, based on logic `else { ... =
        // points }`)
        // Logic in Player.java: if (cat != TREASURY && cat != SCIENCE && cat !=
        // MILITARY) accumulate ELSE set
        // So Science should overwrite.
        player.updateVictoryPoints(20, Effect.Category.SCIENCE);
        assertEquals(20, player.getPointsByCategory(Effect.Category.SCIENCE));

        // 4. Update SCIENCE again (Overwrite)
        player.updateVictoryPoints(25, Effect.Category.SCIENCE);
        assertEquals(25, player.getPointsByCategory(Effect.Category.SCIENCE));

        // 5. Check Total
        // Total = Civil(15) + Science(25) = 40
        assertEquals(40, player.getTotalVictoryPoints());
    }

    @Test
    @DisplayName("discard removes card from board and logs event")
    void testDiscard() {
        when(mockBoard.removeCard(0)).thenReturn(mockCard);
        when(mockCard.getName()).thenReturn("DiscardedCard");

        Card result = player.discard(0);

        assertEquals(mockCard, result);
        verify(mockBoard).removeCard(0);

        // Verify Log
        logMock.verify(() -> Log.logEvent(contains("discards card")));
    }
}
