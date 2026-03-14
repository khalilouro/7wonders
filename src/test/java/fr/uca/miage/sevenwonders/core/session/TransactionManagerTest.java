package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.NeighborTrading;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionManagerTest {

    @Mock
    private Bank bank;
    @Mock
    private Player payer;
    @Mock
    private Player neighbor;

    // We mock static Log to prevent console clutter and verify logging interactions
    private MockedStatic<Log> logMock;

    @InjectMocks
    private TransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        logMock = mockStatic(Log.class);
        lenient().when(payer.getName()).thenReturn("PayerPlayer");
        lenient().when(neighbor.getName()).thenReturn("NeighborPlayer");
    }

    @AfterEach
    void tearDown() {
        logMock.close();
    }

    // -------------------------------------------------------------------------
    // TEST: payCost (Gold)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pay Gold Cost: Success")
    void testPayCost_Gold_Success() {
        // Arrange
        int amount = 5;
        Cost.Gold goldCost = mock(Cost.Gold.class);
        when(goldCost.amount()).thenReturn(amount);

        when(bank.Pay(payer, amount)).thenReturn(true);

        // Act
        transactionManager.payCost(goldCost, payer);

        // Assert
        verify(bank).Pay(payer, amount);
        logMock.verify(() -> Log.logEvent(contains("successfully paid " + amount)));
    }

    @Test
    @DisplayName("Pay Gold Cost: Fail (Insufficient Funds)")
    void testPayCost_Gold_Fail() {
        // Arrange
        int amount = 100;
        Cost.Gold goldCost = mock(Cost.Gold.class);
        when(goldCost.amount()).thenReturn(amount);

        when(bank.Pay(payer, amount)).thenReturn(false);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transactionManager.payCost(goldCost, payer);
        });

        assertTrue(exception.getMessage().contains("cannot afford"));
        verify(bank).Pay(payer, amount);
    }

    // -------------------------------------------------------------------------
    // TEST: payCost (Free)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pay Free Cost: Should just log")
    void testPayCost_Free() {
        // Arrange
        Cost.Free freeCost = mock(Cost.Free.class);

        // Act
        transactionManager.payCost(freeCost, payer);

        // Assert
        verifyNoInteractions(bank); // Bank should not be touched
        logMock.verify(() -> Log.logEvent(contains("pays nothing")));
    }

    // -------------------------------------------------------------------------
    // TEST: payCost (Materials)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pay Materials Cost: Should just log (Bank untouched)")
    void testPayCost_Materials() {
        // Arrange
        Cost.Materials materialCost = mock(Cost.Materials.class);
        // Assuming materials() returns an array/list of enums. Returning empty array
        // for simplicity.
        when(materialCost.materials()).thenReturn(new Card.Materials[]{});

        // Act
        transactionManager.payCost(materialCost, payer);

        // Assert
        verifyNoInteractions(bank);
        logMock.verify(() -> Log.logEvent(contains("uses materials")));
    }

    // -------------------------------------------------------------------------
    // TEST: payCost (Compound)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pay Compound Cost: Should recurse")
    void testPayCost_Compound() {
        // Arrange
        Cost.Compound compoundCost = mock(Cost.Compound.class);
        Cost.Gold subCost1 = mock(Cost.Gold.class);
        Cost.Free subCost2 = mock(Cost.Free.class);

        when(subCost1.amount()).thenReturn(2);
        when(bank.Pay(payer, 2)).thenReturn(true);

        // Setup list of sub-costs
        when(compoundCost.costs()).thenReturn(List.of(subCost1, subCost2));

        // Act
        transactionManager.payCost(compoundCost, payer);

        // Assert
        // Should have called pay for Gold
        verify(bank).Pay(payer, 2);
        // Should have logged for Free
        logMock.verify(() -> Log.logEvent(contains("free cost")));
    }

    // -------------------------------------------------------------------------
    // TEST: payCost (Trading)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Pay Trading Cost: Base Cost + Neighbor Transfer")
    void testPayCost_Trading_Success() {
        // Arrange
        Cost.Trading tradingCost = mock(Cost.Trading.class);
        Cost.Gold baseCost = mock(Cost.Gold.class); // Assuming trading usually has a gold base cost or free
        NeighborTrading neighborTrading = mock(NeighborTrading.class);

        // 1. Setup Base Cost (e.g., 0 gold if the card itself is free to build apart
        // from resources)
        when(tradingCost.baseCost()).thenReturn(baseCost);
        when(baseCost.amount()).thenReturn(0);
        when(bank.Pay(payer, 0)).thenReturn(true);

        // 2. Setup Neighbor Trade
        when(tradingCost.tradingInfo()).thenReturn(neighborTrading);

        // Create a Trade Record (using real record if possible, or mocking)
        // Since NeighborTrading.Trade is a record, we instantiate it.
        // Trade(Player neighbor, Card.Materials resource, int cost)
        NeighborTrading.Trade trade = new NeighborTrading.Trade(neighbor, null, 2);

        when(neighborTrading.getTrades()).thenReturn(Collections.singletonList(trade));

        // 3. Mock Bank Trade
        when(bank.trade(payer, neighbor, 2)).thenReturn(true);

        // Act
        transactionManager.payCost(tradingCost, payer);

        // Assert
        verify(bank).trade(payer, neighbor, 2);
        logMock.verify(() -> Log.logEvent(contains("buys null from NeighborPlayer")));
    }

    @Test
    @DisplayName("Pay Trading Cost: Fail on Neighbor Transfer")
    void testPayCost_Trading_Fail() {
        // Arrange
        Cost.Trading tradingCost = mock(Cost.Trading.class);
        Cost.Free baseCost = mock(Cost.Free.class);
        NeighborTrading neighborTrading = mock(NeighborTrading.class);

        when(tradingCost.baseCost()).thenReturn(baseCost);
        when(tradingCost.tradingInfo()).thenReturn(neighborTrading);

        NeighborTrading.Trade trade = new NeighborTrading.Trade(neighbor, null, 2);
        when(neighborTrading.getTrades()).thenReturn(Collections.singletonList(trade));

        // Mock Bank Trade Failure
        when(bank.trade(payer, neighbor, 2)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            transactionManager.payCost(tradingCost, payer);
        });

        // Verify we tried
        verify(bank).trade(payer, neighbor, 2);
    }

    // -------------------------------------------------------------------------
    // TEST: Direct methods (transferGold / payGoldCost)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("transferGold: Success")
    void testTransferGold() {
        when(bank.trade(payer, neighbor, 5)).thenReturn(true);

        boolean result = transactionManager.transferGold(payer, neighbor, 5);

        assertTrue(result);
        verify(bank).trade(payer, neighbor, 5);
        logMock.verify(() -> Log.logEvent(contains("transferred 5 coins")));
    }

    @Test
    @DisplayName("payGoldCost: Throws on failure")
    void testPayGoldCost_Throws() {
        when(bank.Pay(payer, 10)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> {
            transactionManager.payGoldCost(payer, 10);
        });
    }
}
