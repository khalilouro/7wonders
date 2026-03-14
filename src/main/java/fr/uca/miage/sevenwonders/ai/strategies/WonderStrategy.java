package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;

import java.util.*;

/**
 * Strategy focused on building wonder stages and acquiring necessary resources.
 */
public class WonderStrategy implements Strategy {
    /** Fallback strategy used when no wonder-related option is possible. */
    private final Strategy fallback = new RandomStrategy();

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        if (hand.isEmpty()) {
            return -1;
        }

        // 1. First priority: build the next wonder stage if possible
        if (!bot.getWonder().isCompleted()) {
            WonderStage nextStage = bot.getWonder().getNextStageToBuild();
            if (nextStage != null && bot.canBuild(nextStage.getCosts()).isPresent()) {
                int cardIndex = findBestCardForWonder(hand, bot);
                if (cardIndex != -1) {
                    return 2 * 10 + cardIndex; // Action 2: build wonder stage
                }
            }
        }

        // 2. Second priority: build cards that provide resources needed for next wonder
        // stage
        int resourceCardIndex = buildNeededResourceCard(bot, hand);
        if (resourceCardIndex != -1) {
            return 1 * 10 + resourceCardIndex; // Action 1: build card
        }

        // 3. Third priority: sell card if coins needed
        if (needsCoinsForWonder(bot)) {
            int sellIndex = findCardToSell(hand, bot);
            if (sellIndex != -1) {
                return 0 * 10 + sellIndex; // Action 0: sell card
            }
        }

        // 4. Fourth priority: try to build any high-value card
        int bestCardIndex = findBestCardToBuild(hand, bot);
        if (bestCardIndex != -1) {
            return 1 * 10 + bestCardIndex; // Action 1: build card
        }

        // 5. Fallback to random strategy
        return fallback.applyStrategy(bot, bank);
    }

    private int findBestCardForWonder(List<Card> hand, Bot bot) {
        // Try to find cards that we cannot build normally first
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!bot.canBuild(card).isPresent()) {
                return i;
            }
        }

        // Otherwise, prefer non-resource cards
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.getColor() != Card.Color.BROWN && card.getColor() != Card.Color.GREY) {
                return i;
            }
        }
        return 0;
    }

    private int buildNeededResourceCard(Bot bot, List<Card> hand) {
        if (bot.getWonder().isCompleted()) {
            return -1;
        }

        WonderStage nextStage = bot.getWonder().getNextStageToBuild();
        if (nextStage == null) {
            return -1;
        }

        Map<Card.Materials, Integer> requiredResources = getRequiredResourcesForNextStage(bot);
        Map<Card.Materials, Integer> currentProduction = analyzeResourceProduction(bot);
        List<Card.Materials> missingResources = identifyMissingResources(requiredResources, currentProduction);

        for (Card.Materials missing : missingResources) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (producesMaterial(card, missing) && bot.canBuild(card).isPresent()) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Map<Card.Materials, Integer> getRequiredResourcesForNextStage(Bot bot) {
        Map<Card.Materials, Integer> required = new HashMap<>();
        WonderStage nextStage = bot.getWonder().getNextStageToBuild();

        if (nextStage != null) {
            Cost cost = nextStage.getCosts();
            extractMaterialsFromCost(cost, required);
        }
        return required;
    }

    private void extractMaterialsFromCost(Cost cost, Map<Card.Materials, Integer> required) {
        if (cost == null) return;

        switch (cost) {
            case Cost.Materials materialsCost -> {
                for (Card.Materials material : materialsCost.materials()) {
                    required.put(material, required.getOrDefault(material, 0) + 1);
                }
            }
            case Cost.Compound compoundCost -> {
                for (Cost subCost : compoundCost.costs()) {
                    extractMaterialsFromCost(subCost, required);
                }
            }
            case Cost.Trading tradingCost -> {
                extractMaterialsFromCost(tradingCost.baseCost(), required);
            }
            default -> {}
        }
    }

    private Map<Card.Materials, Integer> analyzeResourceProduction(Bot bot) {
        Map<Card.Materials, Integer> production = new HashMap<>();

        // Use accessor for resources
        List<List<Card.Materials>> playerProduction = bot.getResources().getProduction();
        for (List<Card.Materials> productionOption : playerProduction) {
            for (Card.Materials material : productionOption) {
                production.put(material, production.getOrDefault(material, 0) + 1);
            }
        }

        Card.Materials startingResource = bot.getWonder().getStartingResource();
        production.put(startingResource, production.getOrDefault(startingResource, 0) + 1);

        return production;
    }

    private List<Card.Materials> identifyMissingResources(Map<Card.Materials, Integer> required,
            Map<Card.Materials, Integer> current) {
        List<Card.Materials> missing = new ArrayList<>();

        for (Map.Entry<Card.Materials, Integer> entry : required.entrySet()) {
            Card.Materials material = entry.getKey();
            int needed = entry.getValue();
            int have = current.getOrDefault(material, 0);

            if (have < needed) {
                missing.add(material);
            }
        }
        return missing;
    }

    private boolean producesMaterial(Card card, Card.Materials material) {
        if (!(card.getEffect()instanceof Effect.Production productionEffect)) {
            return false;
        }

        for (List<Card.Materials> productionOption : productionEffect.getProduction()) {
            if (productionOption.contains(material)) {
                return true;
            }
        }
        return false;
    }

    private boolean needsCoinsForWonder(Bot bot) {
        if (bot.getWonder().isCompleted())
            return false;

        WonderStage nextStage = bot.getWonder().getNextStageToBuild();
        if (nextStage == null)
            return false;

        // FIX: Access gold/silver via Resources
        int totalCoins = bot.getResources().getGold() * 3 + bot.getResources().getSilver();

        int requiredGold = calculateRequiredGold(nextStage.getCosts());

        return totalCoins < requiredGold + 3;
    }

    private int calculateRequiredGold(Cost cost) {
        if (cost == null) return 0;

        return switch (cost) {
            case Cost.Gold goldCost -> goldCost.amount();
            case Cost.Compound compoundCost -> {
                int total = 0;
                for (Cost subCost : compoundCost.costs()) {
                    total += calculateRequiredGold(subCost);
                }
                yield total;
            }
            case Cost.Trading tradingCost -> calculateRequiredGold(tradingCost.baseCost());
            default -> 0;
        };
    }

    private int findBestCardToBuild(List<Card> hand, Bot bot) {
        Card.Color[] priorities = {Card.Color.BLUE, Card.Color.GREEN, Card.Color.RED, Card.Color.GOLDEN};

        for (Card.Color color : priorities) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (card.getColor() == color && bot.canBuild(card).isPresent()) {
                    return i;
                }
            }
        }

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (bot.canBuild(card).isPresent()) {
                return i;
            }
        }
        return -1;
    }

    private int findCardToSell(List<Card> hand, Bot bot) {
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!bot.canBuild(card).isPresent() && !providesNeededResource(card, bot)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!bot.canBuild(card).isPresent()) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!(card.getEffect() instanceof Effect.Production)) {
                return i;
            }
        }
        return -1;
    }

    private boolean providesNeededResource(Card card, Bot bot) {
        if (bot.getWonder().isCompleted() || !(card.getEffect() instanceof Effect.Production)) {
            return false;
        }

        Map<Card.Materials, Integer> requiredResources = getRequiredResourcesForNextStage(bot);
        Map<Card.Materials, Integer> currentProduction = analyzeResourceProduction(bot);

        Effect.Production productionEffect = (Effect.Production) card.getEffect();
        for (List<Card.Materials> productionOption : productionEffect.getProduction()) {
            for (Card.Materials produced : productionOption) {
                int needed = requiredResources.getOrDefault(produced, 0);
                int have = currentProduction.getOrDefault(produced, 0);
                if (needed > have) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "Wonder";
    }
}
