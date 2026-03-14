package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.models.Card;
import fr.uca.miage.sevenwonders.models.Player;
import fr.uca.miage.sevenwonders.models.Session;
import fr.uca.miage.sevenwonders.stats.GameResult;
import fr.uca.miage.sevenwonders.stats.PlayerResult;

public class GameEngine {

    public GameResult runGame(int gameId, boolean verbose) {
        Session session = new Session();

        // --- Game Loop ---
        for (Card.Age currentAge : Card.Age.values()) {
            if (verbose)
                System.out.println("------ Starting " + currentAge + " ------");

            session.distrebutsCards();

            for (int round = 1; round <= 6; round++) {
                if (verbose)
                    System.out.println("--- " + currentAge + " : Round " + round + " ---");

                // Play Cards
                for (int i = 0; i < session.players.length; i++) {
                    playTurn(session, i, verbose);
                }

                // Trade Hands (except last round)
                if (round < 6)
                    session.tradeHands();
            }

            session.conflictResolution(session.players, session.age);
            session.prepareNextAge();
        }

        if (verbose)
            System.out.println("------ Game Over ------");

        return calculateResults(session, gameId);
    }

    private void playTurn(Session session, int playerIndex, boolean verbose) {
        Player p = session.players[playerIndex];
        if (p.getHand().isEmpty())
            return;

        // For bot players, show their hand in verbose mode
        // For human players (named "Human Player"), ConsoleStrategy will handle ALL the
        // display
        // (including hand, actions, game state, etc.) so we skip displaying here
        if (verbose && !p.getName().equals("Human Player")) {
            String hand = p.getHand().stream().map(c -> c.name).reduce((a, b) -> a + ", " + b).orElse("");
            System.out.printf("%-15s Hand: %s%n", p.getName(), hand);
        }
        session.playerPlaysCard(playerIndex);
    }

    private GameResult calculateResults(Session session, int gameId) {
        GameResult gameResult = new GameResult(gameId);

        for (Player p : session.players) {
            // Calculate Pre-Purple Score
            p.computeFinalScore();
            int scoreBefore = p.getScore();

            // Apply Purple & Finalize
            p.applyPurpleEffects();
            p.computeFinalScore();

            int purplePoints = p.getScore() - scoreBefore;
            gameResult.addPlayerResult(new PlayerResult(p, purplePoints));
        }
        return gameResult;
    }
}
