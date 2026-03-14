package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link NeighborTrading} class, verifying its functionality
 * for managing trades and applying discounts.
 */
class NeighborTradingTest {

    private Player mockPlayerLeft;
    private Player mockPlayerRight;
    private Card.Materials wood;
    private Card.Materials stone;

    /**
     * Set up mock dependencies for players and resources before each test.
     */
    @BeforeEach
    void setUp() {
        // Mock Player objects to represent neighbors
        mockPlayerLeft = mock(Player.class);
        mockPlayerRight = mock(Player.class);
        when(mockPlayerLeft.getName()).thenReturn("LeftNeighbor");
        when(mockPlayerRight.getName()).thenReturn("RightNeighbor");

        // Use real enum values for materials
        wood = Card.Materials.WOOD;
        stone = Card.Materials.STONE;
    }

    /**
     * Tests the default constructor sets the base cost to 2.
     */
    @Test
    void constructor_default_shouldSetBaseCostToTwo() {
        NeighborTrading trading = new NeighborTrading();

        // Use addTrade without explicit cost to check the default cost
        trading.addTrade(mockPlayerLeft, wood);

        assertEquals(2, trading.getTotalTradingCost(), "Default cost should be 2.");
    }

    /**
     * Tests the parameterized constructor sets the base cost correctly.
     */
    @Test
    void constructor_parameterized_shouldSetCustomBaseCost() {
        NeighborTrading trading = new NeighborTrading(3); // Custom base cost of 3

        // Use addTrade without explicit cost to check the custom base cost
        trading.addTrade(mockPlayerRight, stone);

        assertEquals(3, trading.getTotalTradingCost(), "Custom base cost should be 3.");
    }

    /**
     * Tests adding a trade with a custom cost.
     */
    @Test
    void addTrade_withCustomCost_shouldUseCustomCost() {
        NeighborTrading trading = new NeighborTrading();
        int customCost = 5;

        trading.addTrade(mockPlayerLeft, wood, customCost);

        List<NeighborTrading.Trade> trades = trading.getTrades();
        assertEquals(1, trades.size());
        assertEquals(customCost, trades.get(0).cost());
    }

    /**
     * Tests adding a trade that uses the current default base cost.
     */
    @Test
    void addTrade_withDefaultCost_shouldUseBaseCost() {
        NeighborTrading trading = new NeighborTrading(4); // Base cost 4

        trading.addTrade(mockPlayerRight, stone);

        assertEquals(4, trading.getTotalTradingCost());
    }

    /**
     * Tests the total cost calculation for multiple trades.
     */
    @Test
    void getTotalTradingCost_shouldSumAllTradeCosts() {
        NeighborTrading trading = new NeighborTrading();
        trading.addTrade(mockPlayerLeft, wood, 2);
        trading.addTrade(mockPlayerRight, stone, 3);
        trading.addTrade(mockPlayerLeft, wood, 1);

        assertEquals(2 + 3 + 1, trading.getTotalTradingCost(), "Total cost should be the sum of all trade costs.");
    }

    /**
     * Tests the total cost calculation for zero trades.
     */
    @Test
    void getTotalTradingCost_shouldReturnZero_whenNoTrades() {
        NeighborTrading trading = new NeighborTrading();

        assertEquals(0, trading.getTotalTradingCost(), "Total cost should be 0 when no trades exist.");
    }

    /**
     * Tests applying a global discount and recalculating existing trades.
     */
    @Test
    void applyDiscount_shouldReduceBaseCostAndRecalculateTrades() {
        NeighborTrading trading = new NeighborTrading(3); // Initial base cost 3

        // Add two trades that initially cost 3 each
        trading.addTrade(mockPlayerLeft, wood);
        trading.addTrade(mockPlayerRight, stone);
        assertEquals(6, trading.getTotalTradingCost());

        // Apply a discount of 1
        trading.applyDiscount(1); // New base cost should be 3 - 1 = 2

        // Verify total cost is recalculated: 2 + 2 = 4
        assertEquals(4, trading.getTotalTradingCost(), "Total cost should reflect the new discounted base cost.");

        // Verify new trades use the new base cost
        trading.addTrade(mockPlayerLeft, stone);
        assertEquals(4 + 2, trading.getTotalTradingCost());
    }

    /**
     * Tests that the base cost floor is 1, even with a large discount.
     */
    @Test
    void applyDiscount_shouldFloorBaseCostAtOne() {
        NeighborTrading trading = new NeighborTrading(2); // Initial base cost 2
        trading.addTrade(mockPlayerLeft, wood);

        // Apply a large discount
        trading.applyDiscount(10); // New base cost should be Math.max(1, 2 - 10) = 1

        // Verify base cost is 1
        assertEquals(1, trading.getTrades().get(0).cost(), "Trade cost should not go below 1.");

        // Verify new trades use the floor cost of 1
        trading.addTrade(mockPlayerRight, stone);
        assertEquals(2, trading.getTotalTradingCost()); // 1 + 1 = 2
    }

    /**
     * Tests applying a discount specifically to one neighbor's trades.
     */
    @Test
    void applyNeighborDiscount_shouldOnlyAffectSpecifiedNeighbor() {
        NeighborTrading trading = new NeighborTrading(2);

        // Trade with Left at cost 2
        trading.addTrade(mockPlayerLeft, wood, 2);
        // Trade with Right at cost 2
        trading.addTrade(mockPlayerRight, stone, 2);

        assertEquals(4, trading.getTotalTradingCost());

        // Apply discount to Left neighbor's trades
        trading.applyNeighborDiscount(mockPlayerLeft, 1); // Cost should become 2 - 1 = 1

        List<NeighborTrading.Trade> trades = trading.getTrades();

        assertEquals(1, trades.get(0).cost(), "Left neighbor's trade cost should be reduced to 1.");
        assertEquals(2, trades.get(1).cost(), "Right neighbor's trade cost should remain 2.");
        assertEquals(3, trading.getTotalTradingCost(), "Total cost should be 1 + 2 = 3.");
    }

    /**
     * Tests that a neighbor-specific trade cost floor is 1.
     */
    @Test
    void applyNeighborDiscount_shouldFloorTradeCostAtOne() {
        NeighborTrading trading = new NeighborTrading(2);
        trading.addTrade(mockPlayerLeft, wood, 2); // Initial cost 2

        // Apply a large discount
        trading.applyNeighborDiscount(mockPlayerLeft, 5); // Cost should become Math.max(1, 2 - 5) = 1

        assertEquals(1, trading.getTrades().get(0).cost(), "Trade cost should not go below 1.");
    }

    /**
     * Tests that neighbor-specific discounts don't affect trades with other
     * players.
     */
    @Test
    void applyNeighborDiscount_shouldNotAffectOtherTrades() {
        NeighborTrading trading = new NeighborTrading(2);
        trading.addTrade(mockPlayerRight, stone, 2); // Cost 2

        // Apply discount to Left (who has no trades yet)
        trading.applyNeighborDiscount(mockPlayerLeft, 1);

        assertEquals(2, trading.getTotalTradingCost(), "Cost of Right's trade should remain 2.");
    }

    /**
     * Tests that newly added trades after a neighbor discount are not affected by
     * the previous specific discount (since it modifies existing trades only).
     */
    @Test
    void applyNeighborDiscount_shouldNotAffectFutureTrades() {
        NeighborTrading trading = new NeighborTrading(2);
        trading.addTrade(mockPlayerLeft, wood, 2); // Trade 1 cost 2

        trading.applyNeighborDiscount(mockPlayerLeft, 1); // Trade 1 cost 1

        // Add a new trade with Left. It should use the initial cost of 2,
        // as applyNeighborDiscount only updates existing trades.
        trading.addTrade(mockPlayerLeft, stone, 2); // Trade 2 cost 2

        List<NeighborTrading.Trade> trades = trading.getTrades();
        assertEquals(1, trades.get(0).cost(), "Trade 1 must be 1 (discounted).");
        assertEquals(2, trades.get(1).cost(), "Trade 2 must be 2 (not discounted).");
        assertEquals(3, trading.getTotalTradingCost(), "Total cost should be 1 + 2 = 3.");
    }
}
