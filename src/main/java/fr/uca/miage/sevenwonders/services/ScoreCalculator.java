package fr.uca.miage.sevenwonders.services;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.Player;

public class ScoreCalculator {

    /**
     * Calculates the recursive science score based on symbols.
     */
    public static int calculateScienceScore(int tablet, int compass, int wheel, int any) {
        if (any == 0) {
            int sumOfSquares = (tablet * tablet) + (compass * compass) + (wheel * wheel);
            int setsOfThree = Math.min(tablet, Math.min(compass, wheel));
            return sumOfSquares + (setsOfThree * 7);
        } else {
            // Recursive optimization for wildcards
            int withTablet = calculateScienceScore(tablet + 1, compass, wheel, any - 1);
            int withCompass = calculateScienceScore(tablet, compass + 1, wheel, any - 1);
            int withWheel = calculateScienceScore(tablet, compass, wheel + 1, any - 1);
            return Math.max(withTablet, Math.max(withCompass, withWheel));
        }
    }

    /**
     * Computes the final score for a player and updates their category map.
     */
    public int computeFinalScore(Player player) {
        int totalScore = 0;

        // 1. Treasury
        int treasuryPoints = player.getResources().getGold() + (player.getResources().getSilver() / 3);
        player.updateVictoryPoints(treasuryPoints, Effect.Category.TREASURY);

        // 2. Science
        int sciencePoints = player.getScience().calculateScore();
        player.updateVictoryPoints(sciencePoints, Effect.Category.SCIENCE);

        // 3. Military
        player.updateVictoryPoints(player.getMilitary().getConflictPoints(), Effect.Category.MILITARY);

        // 4. Summation (Civilian, Guild, Wonder points should already be in
        // victoryPoints accumulator)
        // We iterate categories or use the main accumulator dependent on implementation
        // preference.
        // Assuming Player accumulates generic points in `victoryPoints`:

        totalScore = player.getTotalVictoryPoints(); // This should sum the categories or the tracked integer
        return totalScore;
    }
}
