package fr.uca.miage.sevenwonders.stats;

import java.util.ArrayList;
import java.util.List;

public class GameResult {
    private final int gameId;
    private final List<PlayerResult> playerResults = new ArrayList<>();
    private String winnerName;
    private int highestScore = -1;

    public GameResult(int gameId) {
        this.gameId = gameId;
    }

    public void addPlayerResult(PlayerResult result) {
        playerResults.add(result);
        if (result.totalScore > highestScore) {
            highestScore = result.totalScore;
            winnerName = result.playerName;
        }
    }

    public int getGameID() {
        return this.gameId;
    }

    public List<PlayerResult> getResults() {
        return playerResults;
    }

    public String getWinnerName() {
        return winnerName;
    }
}
