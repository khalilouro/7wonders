package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test class for the {@link Cost} sealed interface and its records.
 * This test focuses on the {@code Cost.Trading} and {@code Cost.Compound}
 * records to verify they correctly encapsulate and expose the underlying cost
 * components and trading information.
 */
class CostIT {

    private Player neighbor;
    private Card.Materials wood;
    private Card.Materials stone;
    private NeighborTrading tradingInfo;

    @BeforeEach
    void setUp() {
        neighbor = new Player("The Neighbor");

        wood = Card.Materials.WOOD;
        stone = Card.Materials.STONE;

        // Initialize a NeighborTrading instance
        tradingInfo = new NeighborTrading(2);
        // Add a trade: 1 wood from neighbor at base cost 2
        tradingInfo.addTrade(neighbor, wood);
        // Apply a neighbor discount to test cost modification
        tradingInfo.applyNeighborDiscount(neighbor, 1); // Cost becomes 1
    }

    /**
     * Tests the {@code Cost.Trading} record to ensure it correctly holds the base
     * cost and the integrated trading information.
     */
    @Test
    void testTradingCost_Encapsulation() {
        // Base cost: 3 Gold
        Cost baseCost = Cost.gold(3);

        // Create the Trading cost
        Cost.Trading tradingCost = (Cost.Trading) Cost.withTrading(baseCost, tradingInfo);

        // Verify encapsulation
        assertSame(baseCost, tradingCost.baseCost(), "Base cost should be correctly encapsulated.");
        assertSame(tradingInfo, tradingCost.tradingInfo(), "Trading info should be correctly encapsulated.");

        // Verify TradingInfo reflects the calculated cost (1 trade at cost 1)
        assertEquals(1, tradingCost.tradingInfo().getTrades().size());
        assertEquals(1, tradingCost.tradingInfo().getTotalTradingCost(), "Trading cost should be 1 after discount.");
    }

    /**
     * Tests {@code Cost.Trading} when wrapping a {@code Cost.Materials} cost. This
     * simulates how a Player's {@code canBuild} method returns a
     * {@code Cost.Trading} when materials are missing and must be bought.
     */
    @Test
    void testTradingCost_WrappingMaterialsCost() {
        // Base cost: 1 Wood
        Cost baseCost = Cost.materials(wood);

        // Create a trading cost based on the pre-configured NeighborTrading
        Cost.Trading tradingCost = (Cost.Trading) Cost.withTrading(baseCost, tradingInfo);

        // Verify base cost is still Materials(Wood)
        assertTrue(tradingCost.baseCost() instanceof Cost.Materials);
        assertEquals(wood, ((Cost.Materials) tradingCost.baseCost()).materials()[0]);

        // Verify the cost of the trade record is correct after discount
        NeighborTrading.Trade trade = tradingCost.tradingInfo().getTrades().get(0);
        assertEquals(1, trade.cost(), "The trade cost recorded in TradingInfo must reflect discounts.");
        assertSame(neighbor, trade.neighbor());
    }

    /**
     * Tests a simple {@code Cost.Compound} containing Gold and Materials.
     */
    @Test
    void testCompoundCost_Simple() {
        Cost gold = Cost.gold(5);
        Cost materials = Cost.materials(stone, stone);

        Cost.Compound compound = (Cost.Compound) Cost.compound(gold, materials);

        List<Cost> costs = compound.costs();
        assertEquals(2, costs.size());
        assertTrue(costs.get(0) instanceof Cost.Gold);
        assertTrue(costs.get(1) instanceof Cost.Materials);
    }

    /**
     * Tests a complex {@code Cost.Compound} that includes a {@code Cost.Trading}
     * component, simulating a card that requires both gold and a trade.
     */
    @Test
    void testCompoundCost_WithTradingComponent() {
        // 1. Gold Cost (local)
        Cost localGoldCost = Cost.gold(2);

        // 2. Trading Cost (1 Wood from neighbor @ cost 1)
        Cost tradingCost = Cost.withTrading(Cost.materials(wood), tradingInfo);

        // 3. Compound Cost: Gold + Trading
        Cost.Compound complexCost = (Cost.Compound) Cost.compound(localGoldCost, tradingCost);

        // Verification
        assertEquals(2, complexCost.costs().size());
        assertTrue(complexCost.costs().get(0) instanceof Cost.Gold);
        assertTrue(complexCost.costs().get(1) instanceof Cost.Trading);

        // Verify the Trading component is correctly preserved
        Cost.Trading embeddedTrading = (Cost.Trading) complexCost.costs().get(1);
        assertEquals(1, embeddedTrading.tradingInfo().getTotalTradingCost(), "Embedded trading cost must be correct.");
    }

    /**
     * Tests a compound cost containing mixed components, ensuring the structure is
     * maintained.
     */
    @Test
    void testCompoundCost_MultipleMixedComponents() {
        Cost c1 = Cost.free();
        Cost c2 = Cost.gold(1);
        Cost c3 = Cost.materials(wood);

        Cost.Compound compound = (Cost.Compound) Cost.compound(c1, c2, c3);

        assertEquals(3, compound.costs().size());
        assertTrue(compound.costs().get(0) instanceof Cost.Free);
        assertTrue(compound.costs().get(1) instanceof Cost.Gold);
        assertTrue(compound.costs().get(2) instanceof Cost.Materials);
    }
}
