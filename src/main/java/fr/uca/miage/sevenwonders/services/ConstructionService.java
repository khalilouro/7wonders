package fr.uca.miage.sevenwonders.services;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.NeighborTrading;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConstructionService {

    /**
     * Determines if the player can build the given card.
     */
    public Optional<Cost> canBuild(Player player, Card card) {
        // 1. Chaining (Free)
        if (canBuildViaChaining(player, card)) {
            return Optional.of(Cost.free());
        }

        // 2. Special Ability (Olympia Free Build)
        if (player.hasFreeBuildAvailable()) {
            return Optional.of(Cost.free());
        }

        // 3. Regular Cost
        return canBuild(player, card.getCost());
    }

    /**
     * Checks if a card can be built for free via chaining (parent cards).
     */
    public boolean canBuildViaChaining(Player player, Card card) {
        if (!card.hasParents()) {
            return false;
        }

        List<String> alreadyBuilt = player.getBoard().getAlreadyBuilt();
        for (String parent : card.getParents()) {
            if (alreadyBuilt.contains(parent)) {
                Log.save(String.format("%s can build %s for free via chaining from %s", player.getName(),
                        card.getName(), parent));
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a specific Cost object can be paid by the player.
     */
    public Optional<Cost> canBuild(Player player, Cost cost) {
        if (cost == null) return Optional.of(Cost.free());

        return switch (cost) {
            case Cost.Free free -> Optional.of(free);
            case Cost.Gold gold -> hasSufficientGold(player, gold.amount()) ? Optional.of(gold) : Optional.empty();
            case Cost.Materials materials -> resolveMaterialsCost(player, materials);
            case Cost.ChoiceMaterials choice -> resolveChoiceMaterials(player, choice);
            case Cost.Compound compound -> resolveCompoundCost(player, compound);
            case Cost.Trading trading -> resolveTradingCost(player, trading);
        };
    }

    private Optional<Cost> resolveMaterialsCost(Player player, Cost.Materials cost) {
        List<Card.Materials> required = new ArrayList<>(Arrays.asList(cost.materials()));
        List<Card.Materials> remaining = findUnsatisfiedMaterials(required,
                new ArrayList<>(player.getResources().getProduction()));

        // Fully satisfied locally
        if (remaining.isEmpty()) {
            return Optional.of(cost);
        }

        // Try trading for remainder
        Optional<NeighborTrading> trading = calculateNeighborTrading(player, remaining);
        if (trading.isPresent() && hasSufficientGold(player, trading.get().getTotalTradingCost())) {
            return Optional.of(Cost.withTrading(cost, trading.get()));
        }

        return Optional.empty();
    }

    private Optional<Cost> resolveChoiceMaterials(Player player, Cost.ChoiceMaterials cost) {
        // 1. Try local build for any option
        for (List<Card.Materials> option : cost.options()) {
            List<Card.Materials> remaining = findUnsatisfiedMaterials(new ArrayList<>(option),
                    new ArrayList<>(player.getResources().getProduction()));
            if (remaining.isEmpty()) {
                return Optional.of(Cost.materials(option.toArray(new Card.Materials[0])));
            }
        }

        // 2. Try trading for any option
        for (List<Card.Materials> option : cost.options()) {
            List<Card.Materials> remaining = findUnsatisfiedMaterials(new ArrayList<>(option),
                    new ArrayList<>(player.getResources().getProduction()));
            Optional<NeighborTrading> trading = calculateNeighborTrading(player, remaining);
            if (trading.isPresent() && hasSufficientGold(player, trading.get().getTotalTradingCost())) {
                return Optional
                        .of(Cost.withTrading(Cost.materials(option.toArray(new Card.Materials[0])), trading.get()));
            }
        }
        return Optional.empty();
    }

    private Optional<Cost> resolveCompoundCost(Player player, Cost.Compound cost) {
        int goldNeeded = 0;
        NeighborTrading totalTrade = new NeighborTrading();
        List<Cost> payableComponents = new ArrayList<>();

        for (Cost subCost : cost.costs()) {
            Optional<Cost> res = canBuild(player, subCost);
            if (res.isEmpty())
                return Optional.empty();

            Cost p = res.get();
            if (p instanceof Cost.Gold g) {
                goldNeeded += g.amount();
            } else if (p instanceof Cost.Trading t) {
                totalTrade.getTrades().addAll(t.tradingInfo().getTrades());
                payableComponents.add(t.baseCost());
            } else {
                payableComponents.add(p);
            }
        }

        if (!hasSufficientGold(player, goldNeeded + totalTrade.getTotalTradingCost())) {
            return Optional.empty();
        }

        Cost base = new Cost.Compound(payableComponents);
        if (goldNeeded > 0) {
            // Reconstruct compound with gold if needed (simplified)
            // Ideally, we'd add Gold cost to the list, but standard Compound handles list
            // of costs
        }

        if (!totalTrade.getTrades().isEmpty()) {
            return Optional.of(Cost.withTrading(base, totalTrade));
        }
        return Optional.of(base);
    }

    private Optional<Cost> resolveTradingCost(Player player, Cost.Trading cost) {
        int total = calculateTotalCost(cost);
        return hasSufficientGold(player, total) ? Optional.of(cost) : Optional.empty();
    }

    // --- Core Algorithms ---

    private List<Card.Materials> findUnsatisfiedMaterials(List<Card.Materials> required,
            List<List<Card.Materials>> available) {
        if (required.isEmpty())
            return new ArrayList<>();
        if (available.isEmpty())
            return required;

        Card.Materials target = required.get(0);

        // Try to find target in available options
        for (int i = 0; i < available.size(); i++) {
            if (available.get(i).contains(target)) {
                List<Card.Materials> nextReq = new ArrayList<>(required);
                nextReq.remove(target);
                List<List<Card.Materials>> nextAvail = new ArrayList<>(available);
                nextAvail.remove(i);

                List<Card.Materials> result = findUnsatisfiedMaterials(nextReq, nextAvail);
                if (result.isEmpty())
                    return new ArrayList<>();
            }
        }

        // Target not found in this branch, move to next
        List<Card.Materials> remaining = new ArrayList<>();
        remaining.add(target);
        List<Card.Materials> nextReq = new ArrayList<>(required);
        nextReq.remove(target);
        remaining.addAll(findUnsatisfiedMaterials(nextReq, available));
        return remaining;
    }

    private Optional<NeighborTrading> calculateNeighborTrading(Player player, List<Card.Materials> missing) {
        NeighborTrading info = new NeighborTrading();
        for (Card.Materials mat : missing) {
            boolean leftProduces = produces(player.getLeft(), mat);
            boolean rightProduces = produces(player.getRight(), mat);

            if (!leftProduces && !rightProduces)
                return Optional.empty();

            Player supplier = chooseBestSupplier(player, player.getLeft(), player.getRight(), mat);
            int cost = calculateSingleTradeCost(player, supplier, mat);
            info.addTrade(supplier, mat, cost);
        }
        return Optional.of(info);
    }

    private boolean produces(Player p, Card.Materials m) {
        if (p == null)
            return false;
        for (List<Card.Materials> opt : p.getResources().getProduction()) {
            if (opt.contains(m))
                return true;
        }
        return false;
    }

    private Player chooseBestSupplier(Player buyer, Player left, Player right, Card.Materials mat) {
        if (left == null)
            return right;
        if (right == null)
            return left;
        int costL = calculateSingleTradeCost(buyer, left, mat);
        int costR = calculateSingleTradeCost(buyer, right, mat);
        return costL <= costR ? left : right;
    }

    private int calculateSingleTradeCost(Player buyer, Player supplier, Card.Materials mat) {
        int cost = 2; // Base cost
        if (supplier == null)
            return 999;

        Effect.Discount.NeighborLocation loc = (supplier == buyer.getLeft())
                ? Effect.Discount.NeighborLocation.LEFT_PLAYER
                : Effect.Discount.NeighborLocation.RIGHT_PLAYER;

        for (Effect.Discount d : buyer.getBoard().getDiscounts()) {
            if (d.providers().contains(loc) && d.materials().contains(mat)) {
                return d.discountedCost();
            }
        }
        return cost;
    }

    private boolean hasSufficientGold(Player p, int amount) {
        return (p.getResources().getGold() * 3 + p.getResources().getSilver()) >= amount;
    }

    private int calculateTotalCost(Cost cost) {
        if (cost instanceof Cost.Gold g)
            return g.amount();
        if (cost instanceof Cost.Trading t)
            return calculateTotalCost(t.baseCost()) + t.tradingInfo().getTotalTradingCost();
        if (cost instanceof Cost.Compound c)
            return c.costs().stream().mapToInt(this::calculateTotalCost).sum();
        return 0;
    }
}
