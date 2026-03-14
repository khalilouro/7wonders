package fr.uca.miage.sevenwonders.ai.strategies;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.ai.Strategy;
import fr.uca.miage.sevenwonders.models.Bank;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random strategy: selects a card and an action at random.
 */
public class RandomStrategy implements Strategy {

    @Override
    public int applyStrategy(Bot bot, Bank bank) {
        List<Card> hand = bot.getHand();
        int remainingCards = hand.size();

        if (remainingCards == 0) {
            return -1;
        }

        int n = ThreadLocalRandom.current().nextInt(remainingCards);
        int m = ThreadLocalRandom.current().nextInt(3);

        return applyStrategyWithLimit(bot, bank, n, m, 10);
    }

    private int applyStrategyWithLimit(Bot bot, Bank bank, int n, int m, int attempts) {
        if (attempts <= 0) {
            return 0 * 10 + n; // Fallback to discard
        }

        switch (m) {
            case 1 : // Play Card
                if (bot.canBuild(bot.getCard(n)).isPresent()) {
                    return m * 10 + n;
                } else {
                    int newN = ThreadLocalRandom.current().nextInt(bot.getHand().size());
                    int newM = ThreadLocalRandom.current().nextInt(3);
                    return applyStrategyWithLimit(bot, bank, newN, newM, attempts - 1);
                }
            case 2 : // Build Wonder
                Wonder w = bot.getWonder();
                if (w != null && !w.isCompleted()) {
                    WonderStage ws = w.getCurrentStage();
                    if (ws != null && bot.canBuild(ws.getCosts()).isPresent()) {
                        return m * 10 + n;
                    }
                }
                // If cannot build wonder, retry with random action
                int newN = ThreadLocalRandom.current().nextInt(bot.getHand().size());
                int newM = ThreadLocalRandom.current().nextInt(2); // Try build or discard
                return applyStrategyWithLimit(bot, bank, newN, newM, attempts - 1);
            default : // Discard (0)
                return m * 10 + n;
        }
    }

    @Override
    public String getName() {
        return "Random";
    }
}
