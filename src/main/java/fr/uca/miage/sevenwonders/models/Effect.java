package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

import java.util.Collections;
import java.util.List;

/**
 * Represents an effect that can be applied to a player in the Seven Wonders
 * game. Each effect is a variant with its own data and behavior.
 */
// @formatter:off
public sealed interface Effect permits
    Effect.Action,
    Effect.Discount,
    Effect.Gold,
    Effect.Military,
    Effect.PerBoardElement,
    Effect.VictoryPoints,
    Effect.Production,
    Effect.Science
    {
    // @formatter:on
    /**
     * Categories for victory points.
     */
    public enum Category {
        WONDER, CIVILIAN, COMMERCIAL, GUILD, TREASURY, SCIENCE, MILITARY
    }

    /**
     * Applies this effect to the given player.
     *
     * @param player
     *            the player to apply the effect to
     */
    void apply(Player player);

    /**
     * Gets the victory points provided by this effect.
     *
     * @return the victory points, or 0 if none.
     */
    default int getVictoryPoints() {
        return 0;
    }

    /**
     * Gets the military strength provided by this effect.
     *
     * @return the military strength, or 0 if none.
     */
    default int getMilitaryShields() {
        return 0;
    }

    /**
     * Gets the gold amount provided by this effect.
     *
     * @return the gold amount, or 0 if none.
     */
    default int getGoldAmount() {
        return 0;
    }

    /**
     * Gets the production materials provided by this effect.
     *
     * @return the production materials as a list of material options, or an empty
     *         list if none.
     */
    default List<List<Card.Materials>> getProduction() {
        return Collections.emptyList();
    }

    /**
     * Gets the science symbol provided by this effect.
     *
     * @return an Optional containing the science symbol, or empty if none.
     */
    default java.util.Optional<Science.ScienceSymbol> getScienceSymbol() {
        return java.util.Optional.empty();
    }

    /**
     * An action effect for Wonder.
     */
    public static final record Action(ActionType type) implements Effect {
        public enum ActionType {
            PLAY_LAST_CARD, PLAY_DISCARDED, ONE_FREE_PER_AGE, COPY_GUILD
        }

        @Override
        public void apply(Player player) {
            if (player == null)
                throw new IllegalArgumentException("Player cannot be null for Effect.Action");

            switch (type) {
                case PLAY_LAST_CARD -> player.setCanPlayLastCard(true);
                case PLAY_DISCARDED -> player.addPlayFromDiscard(1);
                case ONE_FREE_PER_AGE -> player.addFreeBuildsPerAge(1);
                case COPY_GUILD -> player.setCanCopyGuild(true);
            }
        }
    }

    /**
     * A discount effect that reduces a player's cost for certain actions.
     */
    public static final record Discount(List<Card.Materials> materials, List<NeighborLocation> providers,
            int discountedCost) implements Effect {
        /**
         * L'emplacement du voisin fournisseur (gauche ou droite).
         */
        public enum NeighborLocation {
            LEFT_PLAYER, RIGHT_PLAYER
        }

        @Override
        public void apply(Player player) {
            if (player == null)
                throw new IllegalArgumentException("Player cannot be null for Effect.Discount");

            // Un discount est un effet passif. Le joueur doit stocker cette règle.
            player.addEffect(this);
        }
    }

    /**
     * A gold effect that grants the player a specified amount of gold.
     */
    public static final record Gold(int amount) implements Effect {
        @Override
        public void apply(Player player) {
            Bank.getInstance().withdraw(amount, player);
        }

        @Override
        public int getGoldAmount() {
            return amount;
        }
    }

    /**
     * A military effect that increases the player's military strength.
     */
    public static final record Military(int strength) implements Effect {
        @Override
        public void apply(Player player) {
            player.addMilitaryStrength(strength);
        }

        @Override
        public int getMilitaryShields() {
            return strength;
        }
    }

    /**
     * An effect that applies based on the number of board elements owned by the
     * player. Can be either immediate (for golden cards) or end-game (for purple
     * cards).
     */
    // @formatter:off
    public static final record PerBoardElement(
        boolean includeSelf,
        boolean includeLeft,
        boolean includeRight,
        Effect.VictoryPoints points,
        Effect.Gold gold, String type,
        String[] color,
        boolean immediate // true for golden cards, false for purple cards
    ) implements Effect {
        // @formatter:off

        @Override
        public void apply(Player player) {
            PurpleEffect pE = new PurpleEffect(includeSelf, includeLeft, includeRight, points, gold, type, color,
                    immediate);

            if (immediate) {
                // Apply immediately for golden cards
                pE.applyPurpleEffect(player);
            } else {
                // Add to purple effects for end-game application
                player.addPurpleEffect(pE);
            }
        }
    }

    /**
     * A victory points effect that grants the player victory points.
     */
    public static final record VictoryPoints(int points, Category category) implements Effect {
        public VictoryPoints(int points) {
            this(points, null);
        }

        @Override
        public void apply(Player player) {
            if (player == null)
                throw new IllegalArgumentException("Player cannot be null for Effect.VictoryPoints");
            player.addVictoryPoints(points, category);
        }

        @Override
        public int getVictoryPoints() {
            return points;
        }
    }

    /**
     * A production effect that adds production materials to the player's resources.
     */
    public sealed interface Production extends Effect permits Production.Fixed, Production.Choice {

        /**
         * Required for WonderPrinter compatibility.
         * Returns the materials array for this production.
         */
        Card.Materials[] materials();

        /**
         * A fixed production effect, providing a specific set of materials.
         */
        record Fixed(Card.Materials[] materials) implements Production {
            public Fixed {
                materials = materials != null ? materials.clone() : new Card.Materials[0];
            }

            @Override
            public void apply(Player player) {
                player.addProductionMaterials(List.of(List.of(materials)));
            }

            @Override
            public List<List<Card.Materials>> getProduction() {
                return List.of(List.of(materials));
            }
        }

        /**
         * A choice production effect, allowing the player to choose one set of
         * materials from several options.
         */
        record Choice(List<List<Card.Materials>> options) implements Production {
            public Choice {
                options = options != null ? options : Collections.emptyList();
            }

            @Override
            public void apply(Player player) {
                player.addProductionMaterials(options);
            }

            @Override
            public List<List<Card.Materials>> getProduction() {
                return options;
            }

            /**
             * Implemented for WonderPrinter compatibility.
             * Returns the first option as an array for display purposes.
             */
            @Override
            public Card.Materials[] materials() {
                if (options.isEmpty() || options.get(0).isEmpty()) {
                    return new Card.Materials[0];
                }
                // Return the first option list converted to an array
                return options.get(0).toArray(new Card.Materials[0]);
            }
        }
    }

    /**
     * A science effect that grants the player a science-related benefit.
     */
    public static final record Science(ScienceSymbol symbol) implements Effect {

        /**
         * The type of science symbol.
         */
        public enum ScienceSymbol {
            COMPASS, WHEEL, TABLET, ANY
        }

        @Override
        public void apply(Player player) {
            player.addScience(symbol);
        }

        @Override
        public java.util.Optional<ScienceSymbol> getScienceSymbol() {
            return java.util.Optional.of(symbol);
        }
    }
}
