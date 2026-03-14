package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.ai.Strategy;

import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import fr.uca.miage.sevenwonders.services.ScoreCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A strategy that evaluates all possible moves and selects the one that
 * maximizes a heuristic score.
 */
public class MinMaxStrategy implements Strategy {

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        double bestScore = Double.NEGATIVE_INFINITY;
        int bestAction = -1; // 0=sell, 1=build card, 2=build wonder
        int bestCardIndex = -1;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            // Calculate Denial Value
            double denialValue = evaluateDenial(bot, card);

            // 1. Evaluate Building the Card
            Optional<Cost> buildCost = bot.canBuild(card);
            if (buildCost.isPresent()) {
                double score = evaluateBuildCard(bot, card, bot.getPlayerInLeft(), bot.getPlayerInRight());
                score += denialValue;
                score -= calculateCostPenalty(buildCost.get());
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = 1;
                    bestCardIndex = i;
                }
            }

            // 2. Evaluate Building Wonder Stage
            Wonder wonder = bot.getWonder();
            if (!wonder.isCompleted()) {
                WonderStage nextStage = wonder.getNextStageToBuild();
                if (nextStage != null) {
                    Optional<Cost> stageCost = bot.canBuild(nextStage.getCosts());
                    if (stageCost.isPresent()) {
                        double score = evaluateBuildWonder(bot, nextStage, bot.getPlayerInLeft(),
                                bot.getPlayerInRight());
                        score += denialValue;
                        score -= calculateCostPenalty(stageCost.get());
                        if (score > bestScore) {
                            bestScore = score;
                            bestAction = 2;
                            bestCardIndex = i;
                        }
                    }
                }
            }

            // 3. Evaluate Selling the Card
            double sellScore = evaluateSellCard(bot, card);
            sellScore += denialValue;
            if (sellScore > bestScore) {
                bestScore = sellScore;
                bestAction = 0;
                bestCardIndex = i;
            }
        }

        if (bestAction == -1) {
            return 0;
        }

        return bestAction * 10 + bestCardIndex;
    }

    private double evaluateBuildCard(Bot bot, Card card, Player left, Player right) {
        double score = 0;
        StrategyWeights weights = getAgeWeights(card.getAge());

        // 1. Victory Points
        int vp = card.getEffect().getVictoryPoints();
        if (vp > 0) {
            score += vp * weights.vp();
        }

        // 2. Military
        int shields = card.getEffect().getMilitaryShields();
        if (shields > 0) {
            score += evaluateMilitary(bot, shields, left, right) * weights.military();
        }

        // 3. Science
        Optional<Effect.Science.ScienceSymbol> sci = card.getEffect().getScienceSymbol();
        if (sci.isPresent()) {
            score += evaluateScience(bot, new Effect.Science(sci.get())) * weights.science();
            score += 2.0;
        }

        // 4. Resources
        if (card.getEffect()instanceof Effect.Production prod) {
            if (!prod.getProduction().isEmpty()) {
                score += evaluateProduction(bot, prod) * weights.production();
            }
        }

        // 5. Commercial
        if (card.getColor() == Card.Color.GOLDEN) {
            score += 2.0 * weights.commercial();
            if (card.getEffect() instanceof Effect.Discount) {
                score += 3.0;
            }
            int gold = card.getEffect().getGoldAmount();
            if (gold > 0) {
                score += gold * 0.5;
            }
            if (card.getEffect()instanceof Effect.PerBoardElement pbe) {
                score += evaluatePerBoardElement(bot, pbe);
            }
        }

        // 6. Guilds
        if (card.getColor() == Card.Color.PURPLE) {
            score += 5.0 * weights.guild();
            if (card.getEffect()instanceof Effect.PerBoardElement pbe) {
                score += evaluatePerBoardElement(bot, pbe);
            }
        }

        // 7. Chaining
        score += evaluateChaining(bot, card);

        return score;
    }

    private double evaluateChaining(Bot bot, Card card) {
        if (card.getChainChildren().length == 0)
            return 0.0;
        return card.getChainChildren().length * 1.5;
    }

    private record StrategyWeights(double vp, double military, double science, double production, double commercial,
            double guild) {
    }

    private StrategyWeights getAgeWeights(Card.Age age) {
        return switch (age) {
            case AGE_I -> new StrategyWeights(0.5, 1.0, 1.2, 2.0, 1.0, 1.0);
            case AGE_II -> new StrategyWeights(1.0, 1.2, 1.2, 1.0, 1.2, 1.0);
            case AGE_III -> new StrategyWeights(2.0, 1.5, 1.5, 0.1, 1.0, 2.0);
        };
    }

    private double evaluateBuildWonder(Bot bot, WonderStage stage, Player left, Player right) {
        double score = 4.0;
        for (Effect effect : stage.getEffects()) {
            int vp = effect.getVictoryPoints();
            if (vp > 0)
                score += vp;

            int shields = effect.getMilitaryShields();
            if (shields > 0)
                score += evaluateMilitary(bot, shields, left, right);

            Optional<Effect.Science.ScienceSymbol> sci = effect.getScienceSymbol();
            if (sci.isPresent())
                score += evaluateScience(bot, new Effect.Science(sci.get()));

            if (effect instanceof Effect.Production prod && !prod.getProduction().isEmpty()) {
                score += evaluateProduction(bot, prod);
            }

            int gold = effect.getGoldAmount();
            if (gold > 0)
                score += gold * 0.5;

            if (effect instanceof Effect.PerBoardElement pbe) {
                score += evaluatePerBoardElement(bot, pbe);
            }
        }
        return score;
    }

    private double evaluateSellCard(Bot bot, Card card) {
        return 1.5;
    }

    private double evaluateDenial(Bot bot, Card card) {
        Player neighbor;
        if (card.getAge() == Card.Age.AGE_II) {
            neighbor = bot.getPlayerInRight();
        } else {
            neighbor = bot.getPlayerInLeft();
        }

        if (neighbor == null || neighbor.canBuild(card).isEmpty()) {
            return 0.0;
        }

        double neighborScore = 0;
        StrategyWeights weights = getAgeWeights(card.getAge());

        int shields = card.getEffect().getMilitaryShields();
        if (shields > 0) {
            neighborScore += evaluateMilitary(neighbor, shields, neighbor.getPlayerInLeft(),
                    neighbor.getPlayerInRight()) * weights.military();
        }

        Optional<Effect.Science.ScienceSymbol> sci = card.getEffect().getScienceSymbol();
        if (sci.isPresent()) {
            neighborScore += evaluateScience(neighbor, new Effect.Science(sci.get())) * weights.science();
        }

        int vp = card.getEffect().getVictoryPoints();
        if (vp > 0) {
            neighborScore += vp * weights.vp();
        }

        if (card.getColor() == Card.Color.PURPLE) {
            neighborScore += 5.0 * weights.guild();
            if (card.getEffect()instanceof Effect.PerBoardElement pbe) {
                neighborScore += evaluatePerBoardElement(neighbor, pbe);
            }
        }

        return neighborScore * 0.4;
    }

    private double evaluateMilitary(Player player, int strength, Player left, Player right) {
        int currentStrength = player.getMilitaryStrength();
        int newStrength = currentStrength + strength;
        double score = 0;

        if (left != null && currentStrength <= left.getMilitaryStrength()
                && newStrength >= left.getMilitaryStrength()) {
            score += 5.0;
        }
        if (right != null && currentStrength <= right.getMilitaryStrength()
                && newStrength >= right.getMilitaryStrength()) {
            score += 5.0;
        }
        return score;
    }

    private double evaluateScience(Player player, Effect.Science science) {
        int t = player.getTablet();
        int c = player.getCompass();
        int w = player.getWheel();
        int any = player.getAnyScience();

        int currentScore = ScoreCalculator.calculateScienceScore(t, c, w, any);

        switch (science.symbol()) {
            case TABLET -> t++;
            case COMPASS -> c++;
            case WHEEL -> w++;
            case ANY -> any++;
        }

        int newScore = ScoreCalculator.calculateScienceScore(t, c, w, any);

        return newScore - currentScore;
    }

    private double evaluateProduction(Player player, Effect.Production production) {
        double value = 0;
        List<List<Card.Materials>> current = player.getResources().getProduction(); // Access via Resources
        List<Card.Materials> flatCurrent = new ArrayList<>();
        for (List<Card.Materials> l : current) {
            flatCurrent.addAll(l);
        }

        int[] missingForWonder = getMissingResourcesForWonder(player);

        for (List<Card.Materials> productionOption : production.getProduction()) {
            for (Card.Materials mat : productionOption) {
                int ordinal = mat.ordinal();
                if (missingForWonder[ordinal] > 0) {
                    value += 8.0;
                    missingForWonder[ordinal]--;
                } else if (!flatCurrent.contains(mat)) {
                    value += 4.0;
                } else {
                    value += 1.0;
                }
            }
        }
        return value;
    }

    private double evaluatePerBoardElement(Player player, Effect.PerBoardElement perBoard) {
        int count = 0;
        if (perBoard.includeSelf())
            count += countElements(player, perBoard.type(), perBoard.color());
        if (perBoard.includeLeft() && player.getPlayerInLeft() != null)
            count += countElements(player.getPlayerInLeft(), perBoard.type(), perBoard.color());
        if (perBoard.includeRight() && player.getPlayerInRight() != null)
            count += countElements(player.getPlayerInRight(), perBoard.type(), perBoard.color());

        double score = 0;
        if (perBoard.points() != null) {
            score += count * perBoard.points().points();
        }
        if (perBoard.gold() != null) {
            score += count * perBoard.gold().amount() * 0.5;
        }
        return score;
    }

    private int countElements(Player player, String type, String[] colors) {
        int count = 0;
        Map<String, Integer> boardElements = player.getBoardElement();
        if ("CARD".equals(type)) {
            for (String color : colors) {
                count += boardElements.getOrDefault(color, 0);
            }
        } else {
            count += boardElements.getOrDefault(type, 0);
        }
        return count;
    }

    private int[] getMissingResourcesForWonder(Player player) {
        int[] missing = new int[Card.Materials.values().length];
        if (player.getWonder().isCompleted())
            return missing;

        WonderStage nextStage = player.getWonder().getNextStageToBuild();
        if (nextStage == null)
            return missing;

        int[] required = new int[Card.Materials.values().length];
        extractMaterialsFromCost(nextStage.getCosts(), required);

        List<List<Card.Materials>> availableOptions = player.getResources().getProduction(); // Access via Resources
        List<Card.Materials> available = new ArrayList<>();
        for (List<Card.Materials> l : availableOptions) {
            available.addAll(l);
        }
        available.add(player.getWonder().getStartingResource());

        for (int i = 0; i < required.length; i++) {
            if (required[i] > 0) {
                Card.Materials mat = Card.Materials.values()[i];
                int needed = required[i];
                int have = (int) available.stream().filter(m -> m == mat).count();
                if (have < needed) {
                    missing[i] = needed - have;
                }
            }
        }
        return missing;
    }

    private void extractMaterialsFromCost(Cost cost, int[] required) {
        if (cost == null)
            return;
        if (cost instanceof Cost.Materials materialsCost) {
            for (Card.Materials material : materialsCost.materials()) {
                required[material.ordinal()]++;
            }
        } else if (cost instanceof Cost.Compound compoundCost) {
            for (Cost subCost : compoundCost.costs()) {
                extractMaterialsFromCost(subCost, required);
            }
        } else if (cost instanceof Cost.Trading tradingCost) {
            extractMaterialsFromCost(tradingCost.baseCost(), required);
        }
    }

    private double calculateCostPenalty(Cost cost) {
        if (cost instanceof Cost.Gold gold) {
            return gold.amount() * 0.5;
        }
        if (cost instanceof Cost.Trading trading) {
            return trading.tradingInfo().getTotalTradingCost() * 0.5;
        }
        if (cost instanceof Cost.Compound compound) {
            double penalty = 0;
            for (Cost c : compound.costs()) {
                penalty += calculateCostPenalty(c);
            }
            return penalty;
        }
        return 0;
    }

    @Override
    public String getName() {
        return "MinMax";
    }
}
