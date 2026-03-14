package fr.uca.miage.sevenwonders.models;

import java.util.Collections;
import java.util.List;

import fr.uca.miage.sevenwonders.utils.*;
import fr.uca.miage.sevenwonders.models.card.Card;

/**
 * This class represents a Deck of cards in the game.
 */
public class Deck {
    /** The list of cards in the deck. */
    private List<Card> cards;

    /**
     * Constructs a Deck for the specified number of players and a specific Age.
     *
     * @param number_players
     *            The number of players in the game.
     * @param age
     *            The Age (AGE_I, AGE_II, or AGE_III) for which to load cards.
     */
    public Deck(int number_players, Card.Age age) {
        this.cards = Deserializer.loadCards(number_players, age);

        Collections.shuffle(this.cards);
    }

    /**
     * Shuffles the deck of cards.
     */
    public void shuffle() {
        Collections.shuffle(this.cards);
    }

    /**
     * Draws a card from the deck.
     *
     * @return The drawn card, or null if the deck is empty.
     */
    public Card drawCard() {
        if (cards.isEmpty())
            return null;
        return cards.remove(0);
    }
}
