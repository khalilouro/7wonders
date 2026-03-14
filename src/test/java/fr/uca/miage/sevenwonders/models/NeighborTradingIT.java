package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test class for the {@link NeighborTrading} and its inner
 * {@link NeighborTrading.Trade} record.
 * <p>
 * This test suite verifies the functionality of NeighborTrading using real
 * Player objects, ensuring that trades are correctly recorded, costs are
 * accurately calculated, and discounts (both global and neighbor-specific) are
 * applied correctly.
 * </p>
 */
class NeighborTradingIT {

    private Player alice;
    private Player bob;
    private Player charlie;
    private Card.Materials wood;
    private Card.Materials stone;

    /**
     * Sets up real Player instances and resources before each test.
     */
    @BeforeEach
    void setUp() {
        alice = new Player("Alice");
        bob = new Player("Bob");
        charlie = new Player("Charlie");

        // Use real enum values for materials
        wood = Card.Materials.WOOD;
        stone = Card.Materials.STONE;
    }

    /**
     * Tests recording multiple trades and summing the total cost.
     */
    @Test
    void testTotalTradingCostIntegration() {
        // Initial base cost is 2
        NeighborTrading trading = new NeighborTrading();

        // Trade 1: Bob (default cost 2)
        trading.addTrade(bob, wood);
        // Trade 2: Alice (custom cost 5)
        trading.addTrade(alice, stone, 5);
        // Trade 3: Bob (default cost 2)
        trading.addTrade(bob, wood);

        // Expected total cost: 2 + 5 + 2 = 9
        assertEquals(9, trading.getTotalTradingCost(), "Total cost must be the sum of all recorded trades.");

        List<NeighborTrading.Trade> trades = trading.getTrades();
        assertEquals(3, trades.size());
        assertEquals(bob, trades.get(0).neighbor());
        assertEquals(wood, trades.get(0).resource());
        assertEquals(2, trades.get(0).cost());
    }

    /**
     * Tests applying a global discount and verifying it recalculates existing
     * trades and applies to new trades.
     */
    @Test
    void testGlobalDiscountRecalculationAndFloor() {
        // Start with a high base cost to test reduction
        NeighborTrading trading = new NeighborTrading(5);

        // Trade 1: Alice (cost 5)
        trading.addTrade(alice, wood);
        // Trade 2: Bob (cost 5)
        trading.addTrade(bob, stone);
        assertEquals(10, trading.getTotalTradingCost());

        // Apply discount of 3. New base cost should be 5 - 3 = 2.
        trading.applyDiscount(3);

        // Total cost should be recalculated: 2 + 2 = 4
        assertEquals(4, trading.getTotalTradingCost(), "Total cost must be recalculated after global discount.");

        // Check floor: Apply large discount (10). New base cost should be max(1, 2-10)
        // = 1.
        trading.applyDiscount(10);

        // Total cost should be recalculated: 1 + 1 = 2
        assertEquals(2, trading.getTotalTradingCost(), "Cost must not go below floor of 1.");

        // Add a new trade: it should use the new base cost of 1
        trading.addTrade(charlie, wood);
        assertEquals(3, trading.getTotalTradingCost());
    }

    /**
     * Tests applying a specific discount to one Player (Alice) and ensuring Bob's
     * and Charlie's trades are unaffected.
     */
    @Test
    void testNeighborDiscountOnlyAffectsTargetPlayer() {
        NeighborTrading trading = new NeighborTrading(3);

        // Trade 1: Alice (cost 3)
        trading.addTrade(alice, wood);
        // Trade 2: Bob (cost 3)
        trading.addTrade(bob, stone);
        // Trade 3: Alice (cost 3)
        trading.addTrade(alice, wood);

        assertEquals(9, trading.getTotalTradingCost());

        // Apply neighbor discount to Alice of 2. Alice's trades should become 3 - 2 =
        // 1.
        trading.applyNeighborDiscount(alice, 2);

        // Total cost should be: Alice(1) + Bob(3) + Alice(1) = 5
        assertEquals(5, trading.getTotalTradingCost(), "Discount should only apply to Alice's trades.");

        // Check individual costs
        List<NeighborTrading.Trade> trades = trading.getTrades();
        assertEquals(1, trades.get(0).cost(), "Alice's Trade 1 cost must be 1.");
        assertEquals(3, trades.get(1).cost(), "Bob's Trade cost must remain 3.");
        assertEquals(1, trades.get(2).cost(), "Alice's Trade 2 cost must be 1.");

        // Check floor for neighbor discount
        trading.applyNeighborDiscount(alice, 10);
        assertEquals(1, trades.get(0).cost(), "Cost must not go below floor of 1.");
    }
}
