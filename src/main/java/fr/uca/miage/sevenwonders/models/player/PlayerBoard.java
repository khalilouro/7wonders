package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.PurpleEffect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerBoard {
    private static final int HAND_SIZE = 7;

    private Wonder wonder;
    private List<Card> hand;
    private final List<String> playedCards;

    // Neighbors
    private Player leftNeighbor;
    private Player rightNeighbor;

    // Board Elements (Tokens, Colors)
    private final Map<String, Integer> boardElement;
    private final List<PurpleEffect> purpleEffects;
    private final List<Effect.Discount> discounts;

    public PlayerBoard(Wonder wonder) {
        this.wonder = wonder;
        this.hand = new ArrayList<>(HAND_SIZE);
        this.playedCards = new ArrayList<>();
        this.purpleEffects = new ArrayList<>();
        this.discounts = new ArrayList<>();
        this.boardElement = new HashMap<>();
        initializeBoardElements();
    }

    private void initializeBoardElements() {
        String[] keys = {"BROWN", "GREY", "BLUE", "GREEN", "RED", "GOLDEN", "PURPLE", "DEFEAT_TOKEN",
                "BUILT_WONDER_STAGES"};
        for (String key : keys)
            boardElement.put(key, 0);
    }

    // Hand Management
    public List<Card> getHand() {
        return hand;
    }
    public void setHand(List<Card> hand) {
        this.hand = hand;
    }
    public void addCard(Card c) {
        this.hand.add(c);
    }

    public Card removeCard(int index) {
        if (index >= 0 && index < hand.size())
            return hand.remove(index);
        return null;
    }

    // Neighbors
    public void setNeighborhood(Player left, Player right) {
        this.leftNeighbor = left;
        this.rightNeighbor = right;
    }
    public Player getLeft() {
        return leftNeighbor;
    }
    public Player getRight() {
        return rightNeighbor;
    }

    // Effects & Played Cards
    public void addAlreadyBuilt(Card card) {
        playedCards.add(card.getName());
    }
    public List<String> getAlreadyBuilt() {
        return playedCards;
    }

    public void addDiscount(Effect.Discount d) {
        discounts.add(d);
    }
    public List<Effect.Discount> getDiscounts() {
        return discounts;
    }

    public void addPurpleEffect(PurpleEffect p) {
        purpleEffects.add(p);
    }
    public List<PurpleEffect> getPurpleEffects() {
        return purpleEffects;
    }

    public Map<String, Integer> getBoardElements() {
        return boardElement;
    }
    public void updateBoardElement(String key, int delta) {
        boardElement.put(key, boardElement.getOrDefault(key, 0) + delta);
    }

    public Wonder getWonder() {
        return wonder;
    }
    public void setWonder(Wonder w) {
        this.wonder = w;
    }
}
