package fr.uca.miage.sevenwonders.stats;

import java.util.HashMap;
import java.util.Map;

public class StatisticsAnalyst {

    // Inner class to hold running totals for a specific player
    public static class PlayerAggregate {
        // CHANGED: Added 'public' to all fields so ConsoleReporter can access them
        public String name;
        public int gamesPlayed = 0;
        public int wins = 0;
        public long sumScore = 0;
        public int minScore = Integer.MAX_VALUE;
        public int maxScore = Integer.MIN_VALUE;

        // Sums for categories
        public long sumGold = 0;
        public long sumConflict = 0;
        public long sumScience = 0;
        public long sumVictory = 0;
        public long sumWonder = 0;
        public long sumCivilian = 0;
        public long sumCommercial = 0;
        public long sumGuild = 0;
        public long sumMilitary = 0;
        public long sumTreasury = 0;
        public long sumBlue = 0;
        public long sumGreen = 0;
        public long sumRed = 0;
        public long sumPurple = 0;

        public PlayerAggregate(String name) {
            this.name = name;
        }

        public void addGame(PlayerResult r, boolean isWinner) {
            gamesPlayed++;
            if (isWinner)
                wins++;

            sumScore += r.totalScore;
            minScore = Math.min(minScore, r.totalScore);
            maxScore = Math.max(maxScore, r.totalScore);

            sumGold += r.gold;
            sumConflict += r.conflict;
            sumScience += r.science;
            sumVictory += r.victory;
            sumWonder += r.wonderPoints;
            sumCivilian += r.civilianPoints;
            sumCommercial += r.commercialPoints;
            sumGuild += r.guildPoints;
            sumMilitary += r.militaryPoints;
            sumTreasury += r.treasuryPoints;
            sumBlue += r.blueCards;
            sumPurple += r.purpleCards;
            sumGreen += r.greenCards;
            sumRed += r.redCards;
        }

        public double getWinRate() {
            return (double) wins / gamesPlayed * 100.0;
        }

        public double getAvgScore() {
            return (double) sumScore / gamesPlayed;
        }

        public double getAvgScience() {
            return (double) sumScience / gamesPlayed;
        }

        public double getAvgConflict() {
            return (double) sumConflict / gamesPlayed;
        }
    }

    private final Map<String, PlayerAggregate> aggregates = new HashMap<>();
    private int totalGames = 0;

    public synchronized void recordGame(GameResult result) {
        totalGames++;
        String winner = result.getWinnerName();

        for (PlayerResult pr : result.getResults()) {
            aggregates.putIfAbsent(pr.playerName, new PlayerAggregate(pr.playerName));
            boolean isWinner = pr.playerName.equals(winner);
            aggregates.get(pr.playerName).addGame(pr, isWinner);
        }
    }

    public Map<String, PlayerAggregate> getAggregates() {
        return aggregates;
    }

    public int getTotalGames() {
        return totalGames;
    }
}
