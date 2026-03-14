package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.*;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.wonder.*;
import fr.uca.miage.sevenwonders.models.card.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Strategy focusing on maximizing gold income, either by playing Gold/Economic
 * (Yellow) cards or by discarding cards for 3 coins.
 */
public class EconomicStrategy implements Strategy {
    // Constantes d'action (doivent correspondre à la Session)
    private static final int ACTION_DISCARD = 0;
    private static final int ACTION_BUILD = 1;
    private static final int ACTION_WONDER = 2;

    /**
     * Applies the economic strategy: the bot selects the best card to play or
     * wonder stage to build that maximizes gold income. If no such action is
     * possible, it chooses to discard a card for 3 coins.
     *
     * @param bot
     *            the bot executing the strategy.
     * @param bank
     *            The game's bank, used for transactions or resources.
     * @return An integer encoding both the chosen action and the selected card
     *         index: {@code action * 10 + cardIndex}
     */
    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();

        if (hand.isEmpty()) {
            return -1;
        }

        // 1. Identifier la meilleure carte économique à jouer.
        Optional<CardAction> bestAction = selectBestEconomicAction(bot, hand);

        if (bestAction.isPresent()) {
            CardAction action = bestAction.get();
            return action.action * 10 + action.cardIndex;
        }

        // 2. Si aucune carte ne peut être jouée ou construite, choisir la défausse
        // (discard).
        // On choisit simplement la première carte, car toutes les cartes défaussées
        // valent 3 pièces.
        return ACTION_DISCARD * 10 + 0;
    }

    /**
     * Selects the best economic action (play a card or build a wonder stage) that
     * maximizes gold income.
     *
     * @param bot
     *            the bot executing the strategy.
     * @param hand
     *            the current hand of cards available to the bot.
     * @return An Optional containing the best CardAction if available, or empty if
     *         no action is possible.
     */
    private Optional<CardAction> selectBestEconomicAction(Bot bot, List<Card> hand) {

        // Liste pour stocker toutes les actions valides (Action, Index, Valeur)
        List<CardAction> validActions = new ArrayList<>();

        // Trouver la meilleure carte à jouer (Jaune > Autre)
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);

            // Tenter de jouer la carte (ACTION_BUILD)
            Optional<Cost> costOption = bot.canBuild(card.getCost());
            if (costOption.isPresent()) {
                Cost cost = costOption.get();
                int monetaryCost = calculateMonetaryCost(cost); // Calculer le coût en pièces

                int score = 0;

                // Priorisation basée sur la couleur et l'effet
                if (card.getColor() == Card.Color.GOLDEN) {
                    score = 100; // Haute priorité pour les cartes GOLDEN
                } else if (card.getEffect()instanceof Effect.Gold goldEffect) {
                    // Si l'effet est un gain immédiat d'or (comme Tavern)
                    score = 100 + goldEffect.amount();
                } else {
                    score = 50; // Carte non-commerciale mais jouable (à moindre coût)
                }

                // PÉNALITÉ DE COÛT : Plus la carte coûte cher en pièces, moins elle est
                // prioritaire
                score -= monetaryCost * 5;

                validActions.add(new CardAction(ACTION_BUILD, i, score, cost));
            }

            // Tenter de construire une Merveille (ACTION_WONDER)
            Wonder w = bot.getWonder();
            if (w != null && !w.isCompleted()) {
                WonderStage ws = w.getCurrentStage();
                Optional<Cost> costToWonderOption = bot.canBuild(ws.getCosts());

                if (costToWonderOption.isPresent() && i == 0) { // On prend la première carte pour le Wonder (n=0)
                    Cost costToWonder = costToWonderOption.get();
                    int monetaryCost = calculateMonetaryCost(costToWonder);

                    // Score de base pour la Merveille (priorité moyenne)
                    int score = 60;
                    score -= monetaryCost * 5;

                    validActions.add(new CardAction(ACTION_WONDER, i, score, costToWonder));
                }
            }
        }

        if (validActions.isEmpty()) {
            return Optional.empty();
        }

        // Trier les actions: priorité aux scores les plus élevés
        validActions.sort(Comparator.comparingInt(a -> a.score));

        // Retourner la meilleure action
        CardAction best = validActions.get(validActions.size() - 1);
        return Optional.of(best);
    }

    /**
     * Calcule le coût monétaire total (en pièces d'or) d'un coût donné.
     * Cette méthode est simplifiée et suppose que le coût est principalement en
     * pièces d'or
     * ou qu'une Cost.Trading a déjà été résolue par canBuild.
     */
    private int calculateMonetaryCost(Cost cost) {
        return switch (cost) {
            case Cost.Gold gold -> gold.amount();
            case Cost.Trading trading -> trading.tradingInfo().getTotalTradingCost();
            case Cost.Compound compound -> compound.costs().stream()
                    .mapToInt(this::calculateMonetaryCost)
                    .sum();
            default -> 0; // Les coûts en matériaux (Cost.Materials) ou gratuits sont considérés comme 0
                          // dans cette stratégie simplifiée
        };
    }

    /**
     * Enregistrement interne pour associer une action, un index et un score de
     * priorité.
     */
    private record CardAction(int action, int cardIndex, int score, Cost cost) {
    }

    @Override
    public String getName() {
        return "Economic";
    }
}
