package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.utils.Log;

public class ConflictManager {

    public void resolveConflicts(Player[] players, Card.Age age) {
        Log.logEvent("Military conflicts between neighboring cities are being resolved for this age.");

        for (Player player : players) {
            // Check both neighbors (0=Left, 1=Right)
            for (int i = 0; i < 2; i++) {
                Player neighbor = player.getNeighborhood()[i];

                if (player.getMilitaryStrength() > neighbor.getMilitaryStrength()) {
                    player.addConflictPoints(2 * age.getValue() - 1);
                    Log.logEvent(player.getName() + " wins a military conflict and gains victory points.");
                }

                if (player.getMilitaryStrength() < neighbor.getMilitaryStrength()) {
                    player.addConflictPoints(-1);
                    // Update board visuals and military component
                    player.getBoard().updateBoardElement("DEFEAT_TOKEN", 1);
                    player.getMilitary().addDefeatToken();

                    Log.logEvent(player.getName() + " loses a military conflict and suffers a defeat point.");
                }
            }
        }
        Log.logEvent("All military conflicts for this age have been resolved.");
    }
}
