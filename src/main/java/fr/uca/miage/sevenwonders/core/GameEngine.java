package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.ai.Bot;

import fr.uca.miage.sevenwonders.models.PurpleEffect;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import fr.uca.miage.sevenwonders.stats.GameResult;
import fr.uca.miage.sevenwonders.stats.PlayerResult;
import fr.uca.miage.sevenwonders.utils.Deserializer;
import fr.uca.miage.sevenwonders.utils.Config;

public class GameEngine {

    public GameResult runGame(int gameId, boolean verbose) {
        Session session = new Session();

        // --- Game Loop ---
        for (Card.Age currentAge : Card.Age.values()) {
            if (verbose) {
                String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
                String reset = Config.getInstance().getValueANSI("reset");
                System.out.println(main + "------ Starting " + currentAge + " ------" + reset);
            }

            session.distrebutsCards();

            for (int round = 1; round <= 6; round++) {
                if (verbose) {
                    String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
                    String reset = Config.getInstance().getValueANSI("reset");
                    System.out.println(main + "--- " + currentAge + " : Round " + round + " ---" + reset);
                }

                // Play Cards
                for (int i = 0; i < session.getPlayers().length; i++) {
                    playTurn(session, i, verbose);
                }

                // Handle Halikarnassus effect (play from discard)
                for (Player p : session.getPlayers()) {
                    if (p.canPlayFromDiscard()) {
                        if (verbose) {
                            String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());
                            String reset = Config.getInstance().getValueANSI("reset");
                            System.out.println(accent + p.getName() + " can play from discard pile." + reset);
                        }
                        if (p instanceof Bot) {
                            ((Bot) p).chooseAndPlayFromDiscard(session, verbose);
                        }
                    }
                }

                // Trade Hands (except last round)
                if (round < 6)
                    session.tradeHands();
            }

            // Handle Babylon effect (play last card)
            boolean anyPlayerHasBabylonEffect = false;
            for (Player p : session.getPlayers()) {
                if (p.canPlayLastCard()) {
                    anyPlayerHasBabylonEffect = true;
                    if (verbose) {
                        String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());
                        String reset = Config.getInstance().getValueANSI("reset");
                        System.out.println(accent + p.getName() + " has Babylon effect." + reset);
                    }
                }
            }

            if (anyPlayerHasBabylonEffect) {
                if (verbose) {
                    String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
                    String reset = Config.getInstance().getValueANSI("reset");
                    System.out.println(main + "--- " + currentAge + " : Last card play for Babylon ---" + reset);
                }
                // FIX: Use getPlayers()
                for (int i = 0; i < session.getPlayers().length; i++) {
                    Player p = session.getPlayers()[i];
                    if (p.canPlayLastCard() && !p.getHand().isEmpty()) {
                        if (verbose) {
                            String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());
                            String reset = Config.getInstance().getValueANSI("reset");
                            System.out.println(accent + p.getName() + " can play their last card." + reset);
                        }
                        playTurn(session, i, verbose);
                    }
                }
            }

            session.conflictResolution(session.getPlayers(), session.getAge());
            session.prepareNextAge();
        }

        if (verbose) {
            String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
            String reset = Config.getInstance().getValueANSI("reset");
            System.out.println(main + "------ Game Over ------" + reset);
        }

        return calculateResults(session, gameId, verbose);
    }

    private void playTurn(Session session, int playerIndex, boolean verbose) {
        Player p = session.getPlayers()[playerIndex];
        if (p.getHand().isEmpty())
            return;

        if (verbose && !p.getName().equals("Human Player")) {
            String hand = p.getHand().stream().map(c -> c.getName()).reduce((a, b) -> a + ", " + b).orElse("");
            String main = Config.getInstance().getValueANSI(Config.getInstance().getMainColor());
            String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());
            String secondary = Config.getInstance().getValueANSI(Config.getInstance().getSecondaryColor());
            String reset = Config.getInstance().getValueANSI("reset");
            // Name in Main color, "Hand:" in Accent color, Card list in Default/Reset color
            System.out.printf(main + "%-15s " + accent + "Hand: " + secondary + "%s%n", p.getName(), hand + reset);
            // For human players (named "Human Player"), ConsoleStrategy will handle ALL the
            // display
            // (including hand, actions, game state, etc.) so we skip displaying here
        }
        session.playerPlaysCard(playerIndex);
    }

    private GameResult calculateResults(Session session, int gameId, boolean verbose) {
        GameResult gameResult = new GameResult(gameId);

        for (Player p : session.getPlayers()) {
            // Calculate Pre-Purple Score
            p.computeFinalScore();
            int scoreBefore = p.getScore();

            // Apply Purple & Finalize
            p.applyPurpleEffects();
            applyCopyGuildEffect(p, verbose); // Apply copied guild effect
            p.computeFinalScore();

            int purplePoints = p.getScore() - scoreBefore;
            gameResult.addPlayerResult(new PlayerResult(p, purplePoints));
        }
        return gameResult;
    }

    private void applyCopyGuildEffect(Player player, boolean verbose) {
        if (!player.canCopyGuild()) {
            return;
        }

        PurpleEffect bestGuildEffect = null;
        int maxPoints = -1;

        Player leftNeighbor = player.getLeft();
        Player rightNeighbor = player.getRight();
        Player[] neighbors = {leftNeighbor, rightNeighbor};

        for (Player neighbor : neighbors) {
            if (neighbor == null)
                continue;
            for (String cardName : neighbor.getAlreadyBuilt()) {
                Card card = Deserializer.getCardByName(cardName);
                if (card != null && "PURPLE".equals(card.getColor().toString())) {
                    if (card.getEffect()instanceof Effect.PerBoardElement pbe) {
                        // Create a PurpleEffect object from the card's PerBoardElement effect
                        PurpleEffect purpleEffect = new PurpleEffect(pbe.includeSelf(), pbe.includeLeft(),
                                pbe.includeRight(), pbe.points(), pbe.gold(), pbe.type(), pbe.color(), pbe.immediate());

                        int potentialPoints = purpleEffect.calculatePoints(player);

                        if (potentialPoints > maxPoints) {
                            maxPoints = potentialPoints;
                            bestGuildEffect = purpleEffect;
                        }
                    }
                }
            }
        }

        if (bestGuildEffect != null) {
            if (verbose) {
                String accent = Config.getInstance().getValueANSI(Config.getInstance().getAccentColor());

                String reset = Config.getInstance().getValueANSI("reset");
                System.out.println(
                        accent + player.getName() + " copies a guild and gets " + maxPoints + " points." + reset);
            }
            bestGuildEffect.applyPurpleEffect(player);
        }
    }
}
