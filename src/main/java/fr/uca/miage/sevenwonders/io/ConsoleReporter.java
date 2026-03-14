package fr.uca.miage.sevenwonders.io;

import fr.uca.miage.sevenwonders.stats.PlayerResult;
import fr.uca.miage.sevenwonders.stats.StatisticsAnalyst;
import java.util.Comparator;
import java.util.List;

public class ConsoleReporter {

    public void printSingleGameSummary(List<PlayerResult> results) {
        fr.uca.miage.sevenwonders.utils.Config config = fr.uca.miage.sevenwonders.utils.Config.getInstance();
        String main = config.getValueANSI(config.getMainColor());
        String secondary = config.getValueANSI(config.getSecondaryColor());
        String reset = config.getValueANSI("reset");

        System.out.println("\n" + secondary + "═".repeat(80) + reset);
        System.out.println(main + " SINGLE GAME RESULTS" + reset);
        System.out.println(secondary + "═".repeat(80) + reset);

        // Table 1: Scores
        System.out.println(main + " SCORES" + reset);
        // Top border
        System.out.println(secondary + "╔" + "═".repeat(14) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦"
                + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7)
                + "╦" + "═".repeat(7) + "╗" + reset);

        // Define colors for headers (re-fetching here or define at class level?
        // defining locally for safety)
        String red = config.getValueANSI("red");
        String yellow = config.getValueANSI("yellow");
        String blue = config.getValueANSI("blue");
        String green = config.getValueANSI("green");
        String purple = config.getValueANSI("purple");
        String cyan = config.getValueANSI("cyan");

        System.out.printf(secondary + "║ %-12s ║ %-5s ║ " + red + "%-5s" + secondary + " ║ " + yellow + "%-5s"
                + secondary + " ║ " + cyan + "%-5s" + secondary + " ║ " + blue + "%-5s" + secondary + " ║ " + green
                + "%-5s" + secondary + " ║ " + yellow + "%-5s" + secondary + " ║ " + purple + "%-5s" + secondary
                + " ║%n" + reset, "Player", "Total", "Mil", "Treas", "Wond", "Civ", "Sci", "Com", "Gld");

        // Middle border
        System.out.println(secondary + "╠" + "═".repeat(14) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬"
                + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7)
                + "╬" + "═".repeat(7) + "╣" + reset);

        for (PlayerResult p : results) {
            System.out.printf(
                    secondary + "║ " + main + "%-12s" + secondary + " ║ %-5d ║ " + "%-5d" + " ║ " + "%-5d" + secondary
                            + " ║ " + "%-5d" + " ║ " + "%-5d" + secondary + " ║ " + "%-5d" + " ║ " + "%-5d" + secondary
                            + " ║ " + "%-5d" + secondary + " ║%n" + reset,
                    p.playerName, p.totalScore, p.militaryPoints, p.treasuryPoints, p.wonderPoints, p.civilianPoints,
                    p.science, p.commercialPoints, p.guildPoints);
        }
        // Bottom border
        System.out.println(secondary + "╚" + "═".repeat(14) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩"
                + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7)
                + "╩" + "═".repeat(7) + "╝" + reset);
        System.out.println();

        // Table 2: Construction
        System.out.println(main + " CONSTRUCTION DETAILS" + reset);
        // Top border
        System.out.println(secondary + "╔" + "═".repeat(14) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦"
                + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7) + "╦" + "═".repeat(7)
                + "╦" + "═".repeat(7) + "╗" + reset);

        System.out.printf(secondary + "║ %-12s ║ %-5s ║ %-5s ║ %-5s ║ %-5s ║ %-5s ║ %-5s ║ %-5s ║ %-5s ║%n" + reset,
                "Player", "", "Red", "Brn/G", "Stgs", "Blu", "Grn", "Gol", "Pur");

        // Middle border
        System.out.println(secondary + "╠" + "═".repeat(14) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬"
                + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7) + "╬" + "═".repeat(7)
                + "╬" + "═".repeat(7) + "╣" + reset);

        for (PlayerResult p : results) {
            String resources = p.brownCards + "/" + p.greyCards;
            System.out.printf(
                    secondary + "║ " + main + "%-12s" + secondary
                            + " ║ %-5s ║ %-5d ║ %-5s ║ %-5d ║ %-5d ║ %-5d ║ %-5d ║ %-5d ║%n" + reset,
                    p.playerName, "", p.redCards, resources, p.wonderStages, p.blueCards, p.greenCards, p.yellowCards,
                    p.purpleCards);
        }
        // Bottom border
        System.out.println(secondary + "╚" + "═".repeat(14) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩"
                + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7) + "╩" + "═".repeat(7)
                + "╩" + "═".repeat(7) + "╝" + reset);
        System.out.println(secondary + "═".repeat(80) + reset);
    }

    public void printAggregateStats(StatisticsAnalyst stats) {
        fr.uca.miage.sevenwonders.utils.Config config = fr.uca.miage.sevenwonders.utils.Config.getInstance();
        String main = config.getValueANSI(config.getMainColor());
        String secondary = config.getValueANSI(config.getSecondaryColor());
        String reset = config.getValueANSI("reset");

        System.out.println("\n");
        System.out.println(secondary + "=".repeat(80) + reset);
        System.out.println(
                main + "                        AGGREGATE STATISTICS (" + stats.getTotalGames() + " Games)" + reset);
        System.out.println(secondary + "=".repeat(80) + reset);
        System.out.println(secondary + "═".repeat(80) + reset);

        // Top border
        System.out.println(secondary + "╔" + "═".repeat(14) + "╦" + "═".repeat(10) + "╦" + "═".repeat(10) + "╦"
                + "═".repeat(10) + "╦" + "═".repeat(14) + "╦" + "═".repeat(8) + "╦" + "═".repeat(8) + "╦"
                + "═".repeat(8) + "╦" + "═".repeat(8) + "╦" + "═".repeat(8) + "╦" + "═".repeat(8) + "╦" + "═".repeat(8)
                + "╗" + reset);

        // Define colors for headers
        String red = config.getValueANSI("red");
        String yellow = config.getValueANSI("yellow");
        String blue = config.getValueANSI("blue");
        String green = config.getValueANSI("green");
        String purple = config.getValueANSI("purple");
        String cyan = config.getValueANSI("cyan");

        String format = secondary + "║ " + main + "%-12s" + secondary + " ║ %-8s ║ %-8s ║ %-8s ║ %-12s ║ " + "%-6s"
                + " ║ " + "%-6s" + " ║ " + "%-6s" + " ║ " + "%-6s" + " ║ " + "%-6s" + " ║ " + "%-6s" + " ║ " + "%-6s"
                + " ║%n" + reset;

        System.out.format(
                secondary + "║ %-12s ║ %-8s ║ %-8s ║ %-8s ║ %-12s ║ " + red + "%-6s" + reset + " ║ " + yellow + "%-6s"
                        + secondary + " ║ " + cyan + "%-6s" + secondary + " ║ " + blue + "%-6s" + secondary + " ║ "
                        + green + "%-6s" + reset + " ║ " + yellow + "%-6s" + secondary + " ║ " + purple + "%-6s"
                        + secondary + " ║%n" + reset,
                "Player", "Win %", "Avg Pts", "Max Pts", "Score Range", "AvgMil", "AvgTrs", "AvgWnd", "AvgCiv",
                "AvgSci", "AvgCom", "AvgGld");

        // Middle border
        System.out.println(secondary + "╠" + "═".repeat(14) + "╬" + "═".repeat(10) + "╬" + "═".repeat(10) + "╬"
                + "═".repeat(10) + "╬" + "═".repeat(14) + "╬" + "═".repeat(8) + "╬" + "═".repeat(8) + "╬"
                + "═".repeat(8) + "╬" + "═".repeat(8) + "╬" + "═".repeat(8) + "╬" + "═".repeat(8) + "╬" + "═".repeat(8)
                + "╣" + reset);

        stats.getAggregates().values().stream()
                .sorted(Comparator.comparingDouble(StatisticsAnalyst.PlayerAggregate::getAvgScore).reversed())
                .forEach(agg -> {
                    String range = agg.minScore + " - " + agg.maxScore;
                    System.out.format(format, agg.name, String.format("%.1f%%", agg.getWinRate()),
                            String.format("%.1f", agg.getAvgScore()), String.valueOf(agg.maxScore), range,
                            String.format("%.1f", (double) agg.sumMilitary / agg.gamesPlayed),
                            String.format("%.1f", (double) agg.sumTreasury / agg.gamesPlayed),
                            String.format("%.1f", (double) agg.sumWonder / agg.gamesPlayed),
                            String.format("%.1f", (double) agg.sumCivilian / agg.gamesPlayed),
                            String.format("%.1f", agg.getAvgScience()),
                            String.format("%.1f", (double) agg.sumCommercial / agg.gamesPlayed),
                            String.format("%.1f", (double) agg.sumGuild / agg.gamesPlayed));
                });

        // Bottom border
        System.out.println(secondary + "╚" + "═".repeat(14) + "╩" + "═".repeat(10) + "╩" + "═".repeat(10) + "╩"
                + "═".repeat(10) + "╩" + "═".repeat(14) + "╩" + "═".repeat(8) + "╩" + "═".repeat(8) + "╩"
                + "═".repeat(8) + "╩" + "═".repeat(8) + "╩" + "═".repeat(8) + "╩" + "═".repeat(8) + "╩" + "═".repeat(8)
                + "╝" + reset);
    }
}
