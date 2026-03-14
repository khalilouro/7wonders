package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.ArrayList;
import java.util.List;

public class TurnManager {
    private final Session session;

    public TurnManager(Session session) {
        this.session = session;
    }

    public void distributeCards() {
        Log.logAge(session.getAge(), "Distributing 7 cards to each player for this age");
        Deck deck = session.getDeck();
        Player[] players = session.getPlayers();

        for (Player player : players) {
            List<Card> handForThisAge = new ArrayList<>();
            for (int c = 0; c < 7; c++) {
                Card card = deck.drawCard();
                if (card != null) {
                    handForThisAge.add(card);
                }
            }
            player.setHand(handForThisAge);
            Log.logEvent("Player " + player.getName() + " receives their hand.");
        }
    }

    public void prepareNextAge() {
        Card.Age currentAge = session.getAge();
        if (currentAge == Card.Age.AGE_I) {
            session.setAge(Card.Age.AGE_II);
        } else if (currentAge == Card.Age.AGE_II) {
            session.setAge(Card.Age.AGE_III);
        } else {
            return; // Already at Age III
        }

        // New deck for the new age
        session.setDeck(new Deck(session.getPlayers().length, session.getAge()));
        session.setCurrentTurn(1);

        // Reset per-age effects (like Olympia free build)
        for (Player player : session.getPlayers()) {
            player.resetPerAgeEffects();
        }

        Log.logAge(session.getAge(), "Beginning of age " + session.getAge());
    }

    public void tradeHands() {
        Player[] players = session.getPlayers();
        Card.Age age = session.getAge();
        int currentTurn = session.getCurrentTurn();

        switch (age) {
            case AGE_I, AGE_III -> {
                Log.logTurn(age, currentTurn, "Players pass their hands to the next player on the left.");
                tradeHandsClockwise(players);
            }
            case AGE_II -> {
                Log.logTurn(age, currentTurn, "Players pass their hands to the next player on the right.");
                tradeHandsCounterClockwise(players);
            }
        }
        // Advance turn counter
        session.setCurrentTurn(currentTurn + 1);
    }

    private void tradeHandsClockwise(Player[] players) {
        List<Card> firstPlayerHand = players[0].getHand();
        for (int i = 0; i < players.length - 1; i++) {
            players[i].setHand(players[i + 1].getHand());
        }
        players[players.length - 1].setHand(firstPlayerHand);
    }

    private void tradeHandsCounterClockwise(Player[] players) {
        List<Card> lastPlayerHand = players[players.length - 1].getHand();
        for (int i = players.length - 1; i > 0; i--) {
            players[i].setHand(players[i - 1].getHand());
        }
        players[0].setHand(lastPlayerHand);
    }
}
