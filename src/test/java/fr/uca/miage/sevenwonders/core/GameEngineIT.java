package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.stats.GameResult;
import fr.uca.miage.sevenwonders.stats.PlayerResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test for GameEngine.
 * <p>
 * This test runs a full game using real domain objects (no mocks). It verifies
 * that the game engine correctly orchestrates the 3 Ages, manages turns, and
 * produces a final score.
 * </p>
 */
class GameEngineIT {

    @Test
    void testRunFullGame() {
        // Create the engine
        GameEngine engine = new GameEngine();

        // Run a game with ID 1. Verbose set to false to keep test logs clean.
        // This will trigger the full game loop: 3 Ages * 6 Rounds.
        GameResult result = engine.runGame(1, false);

        // 1. Verify Game Result Existence
        assertNotNull(result, "The game engine should return a valid GameResult object");

        assertEquals(1, result.getGameID(), "The GameResult should match the provided Game ID");

        // 2. Verify Players
        // Assuming the field name is 'ranking' or 'results' based on usage.
        List<PlayerResult> playerResults = result.getResults();

        assertNotNull(playerResults, "Player rankings should not be null");
        assertFalse(playerResults.isEmpty(), "There should be players in the result");

        // Standard game usually has at least 3 players (Bot + Human or similar config).
        assertTrue(playerResults.size() >= 3, "A standard game usually has at least 3 players");

        // 3. Verify Scoring Consistency
        for (PlayerResult pr : playerResults) {
            // In 7 Wonders, players can have a negative total score if they accumulate
            // Military Defeat tokens (-1 VP each) and do not gain enough positive VPs.
            // assertTrue(pr.totalScore >= 0, "Player " + pr.playerName + " should have a
            // non-negative total score");

            assertNotNull(pr.playerName, "Player name should not be null");
        }
    }
}
