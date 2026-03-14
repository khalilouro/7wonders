package fr.uca.miage.sevenwonders.models.wonder;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;

/**
 * Represents a Wonder board in the game, consisting of multiple construction
 * stages. Each Wonder has a name, a side (A or B), a starting resource, and a
 * sequence of stages to build.
 */
public class Wonder {

    // ATTRIBUTES

    /** The name of the Wonder (e.g., "Giza", "Babylon"). */
    private String name;

    /** The side of the Wonder currently in use (A or B). */
    public enum Side {
        /** Side A of the Wonder. */
        A,
        /** Side B of the Wonder. */
        B
    }

    /** The current side of the Wonder (A or B). */
    private Side currentSide;

    /** The array of construction stages for this Wonder. */
    public WonderStage[] stages;

    /** The index of the next stage to be built (0-based). */
    private int currentStage;

    /** The starting resource provided by this Wonder. */
    private Card.Materials startingResource;

    // CONSTRUCTORS

    /**
     * Constructs a Wonder with specified name, starting resource, stages, and side.
     *
     * @param name
     *            The name of the Wonder.
     * @param startingResource
     *            The starting resource provided by the Wonder.
     * @param stages
     *            The array of construction stages for this Wonder.
     * @param currentSide
     *            The side of the Wonder board currently in use (A or B).
     */
    public Wonder(String name, Card.Materials startingResource, WonderStage[] stages, Side currentSide) {
        this.name = name;
        this.currentSide = currentSide;
        this.stages = stages == null ? new WonderStage[0] : stages;
        this.currentStage = 0;
        this.startingResource = startingResource;
    }

    /**
     * Constructs a Wonder with specified name, starting resource, and side, but no
     * stages.
     *
     * @param name
     *            The name of the Wonder.
     * @param currentSide
     *            The side of the Wonder board currently in use (A or B).
     * @param startingResource
     *            The starting resource provided by the Wonder.
     */
    public Wonder(String name, Side currentSide, Card.Materials startingResource) {
        this(name, startingResource, new WonderStage[0], currentSide);
    }

    // METHODS

    /**
     * Checks if the Player can build the current stage of the Wonder.
     *
     * @param player
     *            The Player attempting the construction.
     * @return true if the Player can build the current stage, false otherwise.
     */
    public boolean canBuildStage(Player player) {
        if (isCompleted())
            return false;

        WonderStage nextStage = stages[currentStage];
        return nextStage.canBuild(player);
    }

    /**
     * Attempts to build the current stage of the Wonder for the given Player.
     * <p>
     * Cette méthode existe pour compatibilité (Session, etc.) mais délègue
     * maintenant la logique à {@link WonderBuilder} pour respecter mieux
     * SOLID/GRASP.
     * </p>
     *
     * @param player
     *            The Player attempting the construction.
     * @return true if the stage was successfully built, false otherwise.
     */
    public boolean buildStage(Player player) {
        WonderBuilder builder = new WonderBuilder();
        return builder.buildStage(this, player);
    }

    /**
     * Gets the next WonderStage to be built.
     *
     * @return The next WonderStage to be built, or null if all stages are
     *         completed.
     */
    public WonderStage getNextStageToBuild() {
        if (isCompleted())
            return null;
        return stages[currentStage];
    }

    /**
     * Indicates whether the Wonder has been fully constructed.
     *
     * @return true if the Wonder has been fully constructed, false otherwise.
     */
    public boolean isCompleted() {
        return currentStage >= stages.length;
    }

    /** Internal: advance to the next stage. Used by WonderBuilder. */
    void advanceStage() {
        if (!isCompleted()) {
            currentStage++;
        }
    }

    // GETTERS

    /**
     * Gets the name of the Wonder.
     *
     * @return The name of the Wonder.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current stage.
     *
     * @return The current stage to be built, or null if completed.
     */
    public WonderStage getCurrentStage() {
        if (isCompleted())
            return null;
        return stages[currentStage];
    }

    /**
     * Gets the Wonder stages.
     *
     * @return The Wonder stages.
     */
    public WonderStage[] getWonderStages() {
        return stages;
    }

    /**
     * Gets the total stages.
     *
     * @return The total number of stages for this Wonder.
     */
    public int getTotalStages() {
        return stages.length;
    }

    /**
     * Gets the current side of the Wonder.
     *
     * @return The current side of the Wonder board (A or B).
     */
    public Side getCurrentSide() {
        return currentSide;
    }

    /**
     * Sets the current side of the Wonder.
     *
     * @param side
     *            The side to set (A or B).
     */
    public void setCurrentSide(Side side) {
        this.currentSide = side;
    }

    /**
     * Gets the starting resource of the Wonder.
     *
     * @return The starting resource provided by this Wonder.
     */
    public Card.Materials getStartingResource() {
        return startingResource;
    }

    /**
     * Gets the current stage index.
     *
     * @return The index of the current stage.
     */
    public int getStageIndex() {
        return currentStage;
    }
}
