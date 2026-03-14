package fr.uca.miage.sevenwonders.core;

import fr.uca.miage.sevenwonders.core.session.*;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Deck;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game session of Seven Wonders. Acts as a Facade/Controller that
 * delegates logic to specialized managers.
 */
public class Session {

    // --- State ---
    private Player[] players;
    private Card.Age age;
    private Deck deck;
    private List<Wonder> wonders;
    private List<Card> discardPile;
    private Bank bank;
    private int currentTurn;

    // --- Managers (Sub-components) ---
    private final TurnManager turnManager;
    private final ActionExecutor actionExecutor;
    private final ConflictManager conflictManager;
    private final TransactionManager transactionManager;

    public Session() {
        this.bank = Bank.getInstance();
        this.discardPile = new ArrayList<>();

        // Initialize Managers
        this.transactionManager = new TransactionManager(bank);
        this.actionExecutor = new ActionExecutor(this, transactionManager);
        this.turnManager = new TurnManager(this);
        this.conflictManager = new ConflictManager();

        // Run Initialization Logic
        SessionInitializer.initialize(this);
    }

    // --- Delegated Methods ---

    public void distrebutsCards() {
        turnManager.distributeCards();
    }

    public void prepareNextAge() {
        turnManager.prepareNextAge();
    }

    public void tradeHands() {
        turnManager.tradeHands();
    }

    public void playerPlaysCard(int playerIndex) {
        actionExecutor.executeTurn(playerIndex);
    }

    public void conflictResolution(Player[] players, Card.Age age) {
        conflictManager.resolveConflicts(players, age);
    }

    // --- State Management (Getters & Setters) ---

    public void addToDiscardPile(Card card) {
        this.discardPile.add(card);
    }

    public void removeFromDiscardPile(Card card) {
        this.discardPile.remove(card);
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }

    public Player[] getPlayers() {
        return players;
    }
    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public Card.Age getAge() {
        return age;
    }
    public void setAge(Card.Age age) {
        this.age = age;
    }

    public Deck getDeck() {
        return deck;
    }
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public Bank getBank() {
        return bank;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }
    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public List<Wonder> getWonders() {
        return wonders;
    }
    public void setWonders(List<Wonder> wonders) {
        this.wonders = wonders;
    }
}
