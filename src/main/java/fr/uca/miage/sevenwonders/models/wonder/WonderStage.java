package fr.uca.miage.sevenwonders.models.wonder;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.player.Player;

/**
 * Represents one construction step of a Wonder board. Each stage has a cost and
 * one or more effects, and must be completed sequentially.
 */
public class WonderStage {

    // ATTRIBUTES

    /** The resource costs required to build this stage. */
    private final Cost costs;

    /**
     * The effects granted upon completing this stage (e.g., Victory Points,
     * abilities).
     */
    private final Effect[] effects;

    // CONSTRUCTORS

    /**
     * Constructs a WonderStage with specified costs and effects.
     *
     * @param costs
     *            The costs required to build it.
     * @param effects
     *            The effects granted upon completion.
     */
    public WonderStage(Cost costs, Effect[] effects) {
        this.costs = costs;
        this.effects = (effects == null) ? new Effect[0] : effects;
    }

    /**
     * Constructs a WonderStage with specified costs and no effects.
     *
     * @param costs
     *            The costs required to build it.
     */
    public WonderStage(Cost costs) {
        this(costs, new Effect[0]);
    }

    // METHODS

    /**
     * Checks if the Player has the necessary resources to build this stage.
     *
     * @param player
     *            The Player attempting the construction.
     * @return true if the Player can BUILD, false otherwise.
     */
    public boolean canBuild(Player player) {
        if (player == null)
            return false;

        // TODO: implémenter la vraie logique (player.hasResources(costs), voisins,
        // etc.)
        return true;
    }

    // GETTERS

    /**
     * Gets the costs of the stage.
     *
     * @return The costs required to build this stage.
     */
    public Cost getCosts() {
        return costs;
    }

    /**
     * Gets the effects of the stage.
     *
     * @return The effects granted upon completing this stage.
     */
    public Effect[] getEffects() {
        return effects;
    }
}
