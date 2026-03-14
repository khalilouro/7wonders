package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.utils.Config;
import fr.uca.miage.sevenwonders.utils.Deserializer;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SessionInitializer {

    public static void initialize(Session session) {
        // Load Cards & Reset Bank
        Deserializer.loadAllCards();
        Bank.getInstance().reset();

        // Config
        int numberOfPlayers = Config.getInstance().getNumberOfPlayers();

        // Initialize State in Session
        session.setAge(Card.Age.AGE_I);
        session.setDeck(new Deck(numberOfPlayers, Card.Age.AGE_I));
        session.setCurrentTurn(1);

        // Load Wonders
        List<Wonder> wonders = loadWonders();
        session.setWonders(wonders);

        // Setup Players
        setupPlayers(session, wonders);

        // Setup Neighbors
        setupNeighborhood(session.getPlayers());

        Log.logEvent("A new game session starts with " + numberOfPlayers + " players.");
        Log.logAge(session.getAge(), "Beginning of age " + session.getAge());
    }

    private static List<Wonder> loadWonders() {
        int r;
        switch (Config.getInstance().getWonderSidesToUse()) {
            case "a" -> r = 0;
            case "b" -> r = 1;
            case "both" -> r = 2;
            default -> r = ThreadLocalRandom.current().nextInt(3);
        }

        List<Wonder> wonders = switch (r) {
            case 0 -> Deserializer.loadWonders(Deserializer.whichSide.A);
            case 1 -> Deserializer.loadWonders(Deserializer.whichSide.B);
            default -> Deserializer.loadWonders(Deserializer.whichSide.rand);
        };
        Collections.shuffle(wonders);
        return wonders;
    }

    private static void setupPlayers(Session session, List<Wonder> wonders) {
        List<Player> generatedPlayers = Config.getInstance().getBots();

        if (Config.getInstance().getShuffleBots()) {
            Collections.shuffle(generatedPlayers);
        }

        Player[] players = generatedPlayers.toArray(new Player[0]);
        session.setPlayers(players);

        Bank bank = session.getBank();

        for (int i = 0; i < players.length; i++) {
            // Assign Wonder & Starting Resources
            Wonder w = wonders.remove(0);
            players[i].setWonderplayer(w);
            players[i].addProductionMaterial(w.getStartingResource());

            // Initial Wealth
            bank.WithdrawSilver(3, players[i]);
        }
    }

    private static void setupNeighborhood(Player[] players) {
        for (int i = 0; i < players.length; i++) {
            Player current = players[i];
            Player left = players[(i - 1 + players.length) % players.length];
            Player right = players[(i + 1) % players.length];

            current.setNeighborhood(left, right);
        }
    }
}
