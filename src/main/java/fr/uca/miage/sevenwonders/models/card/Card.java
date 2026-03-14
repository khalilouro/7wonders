package fr.uca.miage.sevenwonders.models.card;

import fr.uca.miage.sevenwonders.models.*;
/**
 * Represents a card used in the game Seven Wonders.
 * <p>
 * A card is immutable and contains all necessary information for the game: its
 * name, cost, age, color, effect and optional parent cards for chaining.
 * </p>
 *
 * <p>
 * This class fully respects SOLID and GRASP principles:
 * <ul>
 * <li><b>SRP</b>: A card is only responsible for representing card data.</li>
 * <li><b>Encapsulation</b>: All fields are private and final.</li>
 * <li><b>Information Expert</b>: The card knows its own properties.</li>
 * <li><b>Low Coupling / High Cohesion</b>: The class interacts minimally with
 * other modules.</li>
 * </ul>
 * </p>
 */
public class Card {

    // -------------------------------------------------------------------------
    // ENUMS
    // -------------------------------------------------------------------------

    /**
     * Represents the different ages of the game.
     */
    public enum Age {
        AGE_I(1), AGE_II(2), AGE_III(3);

        private final int ageValue;

        Age(int ageValue) {
            this.ageValue = ageValue;
        }

        /**
         * Returns the integer value associated with the age.
         *
         * @return the age number (1, 2, or 3)
         */
        public int getValue() {
            return this.ageValue;
        }
    }

    /**
     * Represents the different colors of cards in the game.
     */
    public enum Color {
        BROWN("BROWN"), GREY("GREY"), BLUE("BLUE"), GREEN("GREEN"), GOLDEN("GOLDEN"), RED("RED"), PURPLE("PURPLE");

        private final String colorName;

        Color(String colorName) {
            this.colorName = colorName;
        }

        @Override
        public String toString() {
            return this.colorName;
        }
    }

    /**
     * Represents the materials a card may produce or require.
     */
    public enum Materials {
        WOOD, STONE, CLAY, ORE, GLASS, PAPYRUS, TEXTILE
    }

    // -------------------------------------------------------------------------
    // FIELDS (private + final = full immutability)
    // -------------------------------------------------------------------------

    /** Name of the card. */
    private final String name;

    /** Cost required to construct the card. */
    private final Cost cost;

    /** Age to which the card belongs. */
    private final Age age;

    /** Color representing the type of the card. */
    private final Color color;

    /** The effect applied when the card is built. */
    private final Effect effect;

    /** Parent cards allowing free chaining. */
    private final String[] parents;

    /**
     * The children of the card used for chaining (cards that this card unlocks).
     */
    public String[] chainChildren;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------

    /**
     * Creates a new immutable card.
     *
     * @param name
     *            name of the card
     * @param cost
     *            cost required to construct the card
     * @param age
     *            the age of the card (I, II, III)
     * @param color
     *            the color representing the type of card
     * @param effect
     *            the effect the card provides
     * @param parents
     *            parent cards that allow free construction (chaining)
     */
    public Card(String name, Cost cost, Age age, Color color, Effect effect, String[] parents, String[] chainChildren) {
        this.name = name;
        this.cost = cost;
        this.age = age;
        this.color = color;
        this.effect = effect;
        this.parents = parents != null ? parents : new String[0];
        this.chainChildren = chainChildren != null ? chainChildren : new String[0];
    }

    // -------------------------------------------------------------------------
    // GETTERS (immutable → no setters)
    // -------------------------------------------------------------------------

    /**
     * Returns the name of the card.
     *
     * @return the card name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the cost required to build this card.
     *
     * @return the card cost
     */
    public Cost getCost() {
        return this.cost;
    }

    /**
     * Returns the age to which this card belongs.
     *
     * @return the card age
     */
    public Age getAge() {
        return this.age;
    }

    /**
     * Returns the color of the card.
     *
     * @return the card color
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Returns the effect granted by this card.
     *
     * @return the card effect
     */
    public Effect getEffect() {
        return this.effect;
    }

    /**
     * Returns the parent cards that allow chaining.
     *
     * @return an array of parent card names
     */
    public String[] getParents() {
        return parents;
    }

    /**
     * Indicates whether this card has parent cards for chaining.
     *
     * @return true if the card has one or more parent cards, false otherwise
     */
    public boolean hasParents() {
        return parents.length > 0;
    }

    /**
     * Returns the children of the card.
     *
     * @return the children of the card
     */
    public String[] getChainChildren() {
        return this.chainChildren;
    }
}
