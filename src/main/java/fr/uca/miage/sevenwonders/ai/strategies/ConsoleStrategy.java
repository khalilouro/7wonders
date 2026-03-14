package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.ai.Strategy;

import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.player.Player;

import fr.uca.miage.sevenwonders.io.CardPrinter;
import fr.uca.miage.sevenwonders.io.WonderPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Strategy implementation for a human player interacting via the console.
 */
public class ConsoleStrategy implements Strategy {

    private final Scanner scanner;

    public ConsoleStrategy() {
        this(System.in);
    }

    public ConsoleStrategy(java.io.InputStream input) {
        this.scanner = new Scanner(input);
    }

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("--- It's your turn, " + bot.getName() + "! ---");
        System.out.println("=".repeat(80));

        // Display information about all players
        displayAllPlayersInfo(bot);

        System.out.println("\n" + "-".repeat(80));
        System.out.println("YOUR RESOURCES:");
        System.out.println("-".repeat(80));
        // FIX: Use getters for resources
        System.out.println("Gold: " + bot.getResources().getGold() + " | Silver: " + bot.getResources().getSilver());
        System.out.println("Military Strength: " + bot.getMilitaryStrength());
        // FIX: Use getTotalVictoryPoints()
        System.out.println("Victory Points: " + bot.getTotalVictoryPoints());

        // Display Wonder
        System.out.println("\n" + "-".repeat(80));
        System.out.println("YOUR WONDER:");
        System.out.println("-".repeat(80));
        WonderPrinter.printWonder(bot.getWonder());

        System.out.println("\n" + "-".repeat(80));
        System.out.println("YOUR HAND:");
        System.out.println("-".repeat(80));
        CardPrinter.printHandWithPlayability(bot.getHand(), bot);

        System.out.println("\n" + "-".repeat(80));
        System.out.println("Choose an action:");
        System.out.println("0: Discard a card for 3 coins");
        System.out.println("1: Build a card");
        System.out.println("2: Build a Wonder stage");
        System.out.println("-".repeat(80));

        int action = -1;
        while (action < 0 || action > 2) {
            System.out.print("Enter action (0-2): ");
            if (scanner.hasNextInt()) {
                action = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
            } else {
                scanner.next(); // Consume invalid input
            }
        }

        int cardIndex = -1;
        while (cardIndex < 0 || cardIndex >= bot.getHand().size()) {
            System.out.print("Enter card index (0-" + (bot.getHand().size() - 1) + "): ");
            if (scanner.hasNextInt()) {
                cardIndex = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
            } else {
                scanner.next(); // Consume invalid input
            }
        }

        // Return encoded action: m * 10 + n
        // m = action (0, 1, 2)
        // n = card index
        return action * 10 + cardIndex;
    }

    /**
     * Displays information about all players in the game.
     */
    private void displayAllPlayersInfo(Bot currentPlayer) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("GAME STATE - ALL PLAYERS");
        System.out.println("=".repeat(80));

        Player[] allPlayers = getAllPlayers(currentPlayer);

        for (Player player : allPlayers) {
            boolean isYou = player.getName().equals(currentPlayer.getName());
            String marker = isYou ? " (YOU)" : "";

            System.out.println(
                    "\n" + (isYou ? ">>>" : "---") + " " + player.getName() + marker + " " + (isYou ? "<<<" : "---"));
            System.out.println("  Wonder: " + (player.getWonder() != null ? player.getWonder().getName() : "None"));
            // FIX: Use getters
            System.out.println("  Resources: Gold=" + player.getResources().getGold() + ", Silver="
                    + player.getResources().getSilver());
            System.out.println("  Military: " + player.getMilitaryStrength() + " | Victory Points: "
                    + player.getTotalVictoryPoints());

            // Show played cards count by color
            if (player.getBoardElement() != null) {
                System.out.print("  Cards: ");
                System.out.print("Brown=" + player.getBoardElement().getOrDefault("BROWN", 0) + " ");
                System.out.print("Grey=" + player.getBoardElement().getOrDefault("GREY", 0) + " ");
                System.out.print("Blue=" + player.getBoardElement().getOrDefault("BLUE", 0) + " ");
                System.out.print("Green=" + player.getBoardElement().getOrDefault("GREEN", 0) + " ");
                System.out.print("Red=" + player.getBoardElement().getOrDefault("RED", 0) + " ");
                System.out.print("Golden=" + player.getBoardElement().getOrDefault("GOLDEN", 0) + " ");
                System.out.print("Purple=" + player.getBoardElement().getOrDefault("PURPLE", 0));
                System.out.println();
            }
        }
        System.out.println("=".repeat(80));
    }

    /**
     * Gets all players in the game including neighbors.
     */
    private Player[] getAllPlayers(Bot currentPlayer) {
        // Get neighbors to find all players
        Player[] neighborhood = currentPlayer.getNeighborhood();
        if (neighborhood == null || neighborhood.length < 2) {
            return new Player[]{currentPlayer};
        }

        Player left = neighborhood[0];
        Player right = neighborhood[1];

        // In a 4-player game: left, current, right, and one more
        // We can traverse from current player
        List<Player> players = new ArrayList<>();
        players.add(currentPlayer);

        // Add left neighbor
        if (left != null) {
            players.add(left);
        }

        // Add right neighbor
        if (right != null) {
            players.add(right);
        }

        // Add the player opposite to current (left's left or right's right)
        if (left != null && left.getNeighborhood() != null && left.getNeighborhood().length >= 2) {
            Player leftOfLeft = left.getNeighborhood()[0];
            if (leftOfLeft != null && !players.contains(leftOfLeft)) {
                players.add(leftOfLeft);
            }
        }

        return players.toArray(new Player[0]);
    }

    @Override
    public String getName() {
        return "Console";
    }
}
