package fr.uca.miage.sevenwonders.stats;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.*;
import java.util.Map;

public class PlayerResult {
    public final String playerName;
    public final int totalScore;
    public final int gold;
    public final int conflict;
    public final int victory;
    public final int science;
    public final int purplePoints;

    // Detailed score breakdown
    public final int wonderPoints;
    public final int civilianPoints;
    public final int commercialPoints;
    public final int guildPoints;
    public final int militaryPoints;
    public final int treasuryPoints;

    // Card counts
    public final int wonderStages;
    public final int blueCards;
    public final int greenCards;
    public final int redCards;
    public final int brownCards;
    public final int greyCards;
    public final int purpleCards;
    public final int yellowCards;

    public PlayerResult(Player player, int purplePointsCalc) {
        this.playerName = player.getName();
        this.totalScore = player.getScore();
        this.gold = player.getGoldPoints();
        this.conflict = player.getConflictPoints();

        this.victory = player.getTotalVictoryPoints();

        this.science = player.calculateScienceScore();

        this.purplePoints = purplePointsCalc;

        this.wonderPoints = player.getPointsByCategory(Effect.Category.WONDER);
        this.civilianPoints = player.getPointsByCategory(Effect.Category.CIVILIAN);
        this.commercialPoints = player.getPointsByCategory(Effect.Category.COMMERCIAL);
        this.guildPoints = player.getPointsByCategory(Effect.Category.GUILD);
        this.militaryPoints = player.getPointsByCategory(Effect.Category.MILITARY);
        this.treasuryPoints = player.getPointsByCategory(Effect.Category.TREASURY);

        Map<String, Integer> board = player.getBoardElement();
        this.wonderStages = board.getOrDefault("BUILT_WONDER_STAGES", 0);
        this.blueCards = board.getOrDefault("BLUE", 0);
        this.greenCards = board.getOrDefault("GREEN", 0);
        this.redCards = board.getOrDefault("RED", 0);
        this.brownCards = board.getOrDefault("BROWN", 0);
        this.greyCards = board.getOrDefault("GREY", 0);
        this.purpleCards = board.getOrDefault("PURPLE", 0);
        this.yellowCards = board.getOrDefault("GOLDEN", 0);
    }
}
