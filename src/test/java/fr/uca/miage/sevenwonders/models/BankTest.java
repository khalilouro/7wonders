package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.player.PlayerResources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link Bank} class.
 * <p>
 * This test isolates the {@code Bank}'s logic by mocking the {@code Player} and
 * {@code PlayerResources} classes with Mockito.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class BankTest {

    private Bank bank;

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerResources mockResources;

    // Fields for reflection access to Bank private state
    private Field silverField;
    private Field goldField;

    @BeforeEach
    void setUp() throws Exception {
        // Reset Singleton Instance via Reflection
        Field instanceField = Bank.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        bank = Bank.getInstance();

        // Setup reflection for private fields
        silverField = Bank.class.getDeclaredField("silver");
        silverField.setAccessible(true);
        goldField = Bank.class.getDeclaredField("gold");
        goldField.setAccessible(true);
    }

    private void setupPlayerResources() {
        // Helper to stub the chain: player.getResources() -> mockResources
        // leniency allows us to call this even if the test doesn't use it
        lenient().when(mockPlayer.getResources()).thenReturn(mockResources);
    }

    @Test
    void testBankConstructorInitialValues() throws Exception {
        assertNotNull(bank, "Bank object should not be null.");
    }

    @Test
    void testGetInstanceReturnsBank() {
        assertNotNull(bank);
        assertInstanceOf(Bank.class, bank);
    }

    @Test
    void testGetInstanceReturnsSameInstance() {
        Bank bank1 = Bank.getInstance();
        Bank bank2 = Bank.getInstance();
        assertSame(bank1, bank2);
    }

    @Test
    void testWithdrawWithConversion() throws Exception {
        setupPlayerResources();

        boolean result = bank.withdraw(31, mockPlayer);

        assertTrue(result);

        // Verify player received the silver
        verify(mockResources).addSilver(31);
    }

    @Test
    void testWithdrawGoldSuccess() throws Exception {
        setupPlayerResources();

        boolean result = bank.WithdrawGold(5, mockPlayer);

        assertTrue(result, "Withdrawal should be successful.");
        verify(mockResources).addGold(5);
    }

    @Test
    void testChangeSuccess() throws Exception {
        setupPlayerResources();
        // Player needs 1 Gold to exchange for 3 Silver
        when(mockResources.getGold()).thenReturn(1);

        boolean result = bank.Change(mockPlayer);

        // Verify player updates
        verify(mockResources).removeGold(1);
        verify(mockResources).addSilver(3);
    }

    @Test
    void testDeposit() throws Exception {
        setupPlayerResources();

        // Stub getters to simulate player having funds
        when(mockResources.getGold()).thenReturn(10);
        when(mockResources.getSilver()).thenReturn(10);

        bank.Deposit(2, 5, mockPlayer);

        // Verify deduction from player
        verify(mockResources).setGold(8); // 10 - 2
        verify(mockResources).setSilver(5); // 10 - 5
    }

    // --- Reflection Helpers ---

    private int getBankSilver() throws Exception {
        return (int) silverField.get(bank);
    }

    private int getBankGold() throws Exception {
        return (int) goldField.get(bank);
    }

    private void setBankSilver(int value) throws Exception {
        silverField.set(bank, value);
    }
}
