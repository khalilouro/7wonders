package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.player.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link Bank} class.
 * <p>
 * These tests verify the interaction between a real Bank instance and a real
 * Player instance.
 * </p>
 */
class BankIT {

    private Bank bank;
    private Player player;

    @BeforeEach
    void setUp() throws Exception {
        // Reset the Singleton instance using reflection for isolated testing
        Field instanceField = Bank.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        bank = Bank.getInstance();
        player = new Player("BOT");

        player.getResources().setSilver(10);
        player.getResources().setGold(5);
    }

    @Test
    void testBankConstructorInitialValues() throws Exception {
        assertNotNull(bank, "Bank object should not be null.");
    }

    @Test
    void testBankSingletonInstance() {
        Bank bank1 = Bank.getInstance();
        Bank bank2 = Bank.getInstance();

        assertSame(bank1, bank2, "Both instances should be the same object (singleton).");
        assertTrue(bank1 instanceof Bank, "Bank instance should be of type Bank.");
    }

    @Test
    void testWithdrawSilverSuccess() throws Exception {
        boolean result = bank.WithdrawSilver(10, player);

        assertTrue(result, "Withdrawal should be successful.");
        assertEquals(20, player.getResources().getSilver(),
                "Player silver should increase (10 initial + 10 withdrawn).");
    }

    @Test
    void testWithdrawGoldSuccess() throws Exception {
        boolean result = bank.WithdrawGold(5, player);

        assertTrue(result, "Withdrawal should be successful.");
        assertEquals(10, player.getResources().getGold(), "Player gold should increase (5 initial + 5 withdrawn).");
    }

    @Test
    void testWithdrawWithConversionSuccess() throws Exception {
        // Amount = 7 silver equivalent
        // Bank initial: 20 gold, 30 silver
        // Player initial: 5 gold, 10 silver
        boolean result = bank.withdraw(7, player);

        assertTrue(result, "Withdrawal with conversion should be successful.");
        // Player assertions
        // Player receives 7 silver. Total silver = 17.
        // 17 Silver -> 5 Gold (15s) + 2 Silver rem.
        // Total Gold = 5 (initial) + 5 (converted) = 10.
        // Total Silver = 2.

        assertEquals(10, player.getResources().getGold(), "Player gold should increase by 5 due to silver conversion.");
        assertEquals(2, player.getResources().getSilver(), "Player silver should be 2 after conversion.");
    }

    @Test
    void testChangeSuccess() throws Exception {
        // Player gives 1 Gold, receives 3 Silver
        boolean result = bank.Change(player);

        assertTrue(result, "Change operation should be successful.");
        assertEquals(13, player.getResources().getSilver(), "Player silver should increase by 3 (10 + 3).");
        assertEquals(4, player.getResources().getGold(), "Player gold should decrease by 1 (5 - 1).");
    }

    @Test
    void testDeposit() throws Exception {
        bank.Deposit(2, 5, player);

        assertEquals(3, player.getResources().getGold(), "Player gold should decrease by 2 (5 - 2).");
        assertEquals(5, player.getResources().getSilver(), "Player silver should decrease by 5 (10 - 5).");
    }

    @Test
    void testPaySuccessWithGoldAndSilver() throws Exception {
        player.getResources().setGold(2); // 6 val
        player.getResources().setSilver(2); // 2 val (Total 8)

        boolean result = bank.Pay(player, 7); // Pay 7

        assertTrue(result, "Payment should be successful.");

        assertEquals(0, player.getResources().getGold(), "Player should have 0 gold left.");
        assertEquals(1, player.getResources().getSilver(), "Player should have 1 silver left.");
    }

    @Test
    void testPaySuccessWithChange() throws Exception {
        player.getResources().setGold(2); // 6 val
        player.getResources().setSilver(0); // 0 val (Total 6)

        boolean result = bank.Pay(player, 5); // Pay 5

        assertTrue(result, "Payment with change should be successful.");

        assertEquals(0, player.getResources().getGold(), "Player should have 0 gold left.");
        assertEquals(1, player.getResources().getSilver(), "Player should have 1 silver left.");
    }

    // --- Reflection Helpers ---

    private int getBankSilver() throws Exception {
        Field silverField = Bank.class.getDeclaredField("silver");
        silverField.setAccessible(true);
        return (int) silverField.get(bank);
    }

    private int getBankGold() throws Exception {
        Field goldField = Bank.class.getDeclaredField("gold");
        goldField.setAccessible(true);
        return (int) goldField.get(bank);
    }

    private void setBankSilver(int value) throws Exception {
        Field silverField = Bank.class.getDeclaredField("silver");
        silverField.setAccessible(true);
        silverField.set(bank, value);
    }
}
