package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.wonder.*;
import fr.uca.miage.sevenwonders.models.card.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A strategy focused on establishing a strong resource economy.
 * <p>
 * This strategy prioritizes building Resource cards (Brown/Grey). It
 * intelligently selects cards by analyzing the costs of the player's unbuilt
 * Wonder stages and prioritizing the resources required to build them.
 * </p>
 * <p>
 * Hierarchy of decisions: - Build a Resource card (Brown/Grey) that produces a
 * material needed for the Wonder.</li> - Build any other affordable Resource
 * card. - Build any other affordable card (Fallback). - Discard a card for
 * coins (Last Resort).
 * </p>
 */
public class AnyResourceStrategy implements Strategy {

    /**
     * Executes the strategy logic to select the best resource card.
     *
     * @param bot
     *            The bot instance executing this strategy.
     * @param bank
     *            The bank instance.
     * @return An integer encoding the action (0=discard, 1=build) and the card
     *         index.
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        Wonder wonder = bot.getWonder();

        List<Card.Materials> neededMaterials = new ArrayList<>();
        if (wonder != null && !wonder.isCompleted()) {
            for (int k = wonder.getStageIndex(); k < wonder.getTotalStages(); k++) {
                WonderStage stage = wonder.stages[k];

                if (stage != null) {
                    extractNeededMaterials(stage.getCosts(), neededMaterials);
                }
            }
        }

        int bestIndex = -1;
        int maxMatchCount = -1;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            // Skip nulls or cards we cannot build
            if (card == null || bot.canBuild(card).isEmpty())
                continue;

            if (card.getColor() == Card.Color.BROWN || card.getColor() == Card.Color.GREY) {

                int matchCount = 0;

                // Calculate how many produced materials match the Wonder's needs
                if (card.getEffect()instanceof Effect.Production production) {
                    for (List<Card.Materials> productionOption : production.getProduction()) {
                        for (Card.Materials produced : productionOption) {
                            if (neededMaterials.contains(produced)) {
                                matchCount++;
                            }
                        }
                    }
                }

                if (matchCount > maxMatchCount) {
                    maxMatchCount = matchCount;
                    bestIndex = i;
                }
            }
        }

        // If a resource card was found, build it
        if (bestIndex != -1)
            return encode(1, bestIndex);

        // Last Resort: Discard the first card
        return encode(0, 0);
    }

    /**
     * Recursively extracts materials from a Cost object and adds them to the list.
     *
     * @param cost
     *            The cost object to analyze.
     * @param list
     *            The list to populate with materials.
     */
    private void extractNeededMaterials(Cost cost, List<Card.Materials> list) {
        if (cost instanceof Cost.Materials materialsCost) {
            for (Card.Materials m : materialsCost.materials())
                list.add(m);
        } else if (cost instanceof Cost.Compound compoundCost) {
            for (Cost subCost : compoundCost.costs())
                extractNeededMaterials(subCost, list);
        } else if (cost instanceof Cost.Trading tradingCost) {
            extractNeededMaterials(tradingCost.baseCost(), list);
        }
    }

    /**
     * Encodes the action and card index.
     *
     * @param action
     *            0 = discard, 1 = build structure.
     * @param index
     *            The index of the card in the hand.
     * @return The encoded integer value.
     */
    private int encode(int action, int index) {
        return action * 10 + index;
    }

    @Override
    public String getName() {
        return "AnyResource";
    }
}
