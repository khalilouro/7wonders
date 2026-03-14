package fr.uca.miage.sevenwonders.models.wonder;

import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.Effect;

/**
 * Service responsible for handling the construction of Wonder stages.
 * Centralises the game logic related to building a Wonder stage.
 */
public class WonderBuilder {

    /**
     * Attempts to build the current stage of the given Wonder for the given Player.
     *
     * @param wonder
     *            The Wonder to build.
     * @param player
     *            The Player attempting the construction.
     * @return true if the stage was successfully built, false otherwise.
     */
    public boolean buildStage(Wonder wonder, Player player) {
        if (wonder == null || player == null)
            return false;

        if (wonder.isCompleted())
            return false;

        WonderStage stage = wonder.getNextStageToBuild();
        if (stage == null)
            return false;

        if (!stage.canBuild(player))
            return false;

        // Apply effects
        for (Effect e : stage.getEffects()) {
            if (e != null) {
                e.apply(player);
            }
        }

        // Advance Wonder to the next stage
        wonder.advanceStage();

        return true;
    }
}
