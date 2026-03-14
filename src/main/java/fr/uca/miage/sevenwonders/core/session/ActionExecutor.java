package fr.uca.miage.sevenwonders.core.session;

import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.core.Session;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.models.wonder.WonderStage;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.Optional;

public class ActionExecutor {
    private final Session session;
    private final TransactionManager transactionManager;

    public ActionExecutor(Session session, TransactionManager transactionManager) {
        this.session = session;
        this.transactionManager = transactionManager;
    }

    public void executeTurn(int playerIndex) {
        Player[] players = session.getPlayers();
        if (playerIndex < 0 || playerIndex >= players.length)
            return;

        Player currentPlayer = players[playerIndex];

        // Determine action via Strategy/Bot
        int code = -1;
        if (currentPlayer instanceof Bot bot) {
            code = bot.applyStrategy(bot, session.getBank());
        } else {
            throw new IllegalStateException("Only bots supported for auto-execution.");
        }

        if (code < 0)
            return;

        int actionType = code / 10;
        int cardIndex = code % 10;

        // Log the decision
        Log.logTurn(session.getAge(), session.getCurrentTurn(),
                currentPlayer.getName() + " chooses action " + actionType + " for card index " + cardIndex);

        // Validate Card Index
        if (cardIndex < 0 || cardIndex >= currentPlayer.getHand().size()) {
            if (currentPlayer.getHand().isEmpty())
                return;
            cardIndex = currentPlayer.getHand().size() - 1; // Fallback
        }

        Card currentCard = currentPlayer.getCard(cardIndex);
        Log.logTurn(session.getAge(), session.getCurrentTurn(),
                currentPlayer.getName() + " selected card: " + currentCard.getName());

        switch (actionType) {
            case 0 -> executeDiscard(currentPlayer, cardIndex);
            case 1 -> executeBuildStructure(currentPlayer, currentCard, cardIndex);
            case 2 -> executeBuildWonder(currentPlayer, cardIndex);
            default -> {
                Log.logTurn(session.getAge(), session.getCurrentTurn(),
                        "Invalid action " + actionType + ", discarding instead.");
                executeDiscard(currentPlayer, cardIndex);
            }
        }
    }

    private void executeDiscard(Player player, int cardIndex) {
        Card discardedCard = player.discard(cardIndex);
        if (discardedCard != null) {
            session.addToDiscardPile(discardedCard);
            session.getBank().withdraw(3, player); // Bank gives 3 coins
            Log.logTurn(session.getAge(), session.getCurrentTurn(),
                    player.getName() + " discards " + discardedCard.getName() + " for 3 coins.");
        }
    }

    private void executeBuildStructure(Player player, Card card, int cardIndex) {
        Optional<Cost> toPay = player.canBuild(card);

        if (toPay.isPresent()) {
            Cost cost = toPay.get();

            // Handle Olympia/Free Build Logic
            boolean isNaturallyFree = (card.getCost() == null || card.getCost() instanceof Cost.Free);
            boolean isFreeByChaining = player.canBuildViaChaining(card);

            if (!isFreeByChaining && !isNaturallyFree && player.hasFreeBuildAvailable() && cost instanceof Cost.Free) {
                // If construction service returned Free cost because of the ability, consume it
                player.useFreeBuild();
                Log.logEvent(player.getName() + " uses their free build ability.");
            }

            try {
                transactionManager.payCost(cost, player);
                player.removeCardFromHand(cardIndex);

                // Update board state
                String colorKey = card.getColor().toString();
                player.getBoard().updateBoardElement(colorKey, 1);

                player.addAlreadyBuilt(card);
                card.getEffect().apply(player);

                Log.logTurn(session.getAge(), session.getCurrentTurn(),
                        player.getName() + " successfully built " + card.getName());

            } catch (IllegalStateException e) {
                Log.logTurn(session.getAge(), session.getCurrentTurn(),
                        "Build failed: " + e.getMessage() + ". Discarding instead.");
                executeDiscard(player, cardIndex);
            }
        } else {
            Log.logTurn(session.getAge(), session.getCurrentTurn(),
                    "Cannot build " + card.getName() + ". Discarding instead.");
            executeDiscard(player, cardIndex);
        }
    }

    private void executeBuildWonder(Player player, int cardIndex) {
        Wonder w = player.getWonder();
        WonderStage stage = w.getCurrentStage();

        if (stage == null) {
            executeDiscard(player, cardIndex);
            return;
        }

        Optional<Cost> toPay = player.canBuild(stage.getCosts());

        if (toPay.isPresent()) {
            try {
                transactionManager.payCost(toPay.get(), player);
                w.buildStage(player);

                player.getBoard().updateBoardElement("BUILT_WONDER_STAGES", 1);
                player.removeCardFromHand(cardIndex);

                Log.logTurn(session.getAge(), session.getCurrentTurn(), player.getName() + " built a wonder stage.");
            } catch (IllegalStateException e) {
                Log.logTurn(session.getAge(), session.getCurrentTurn(), "Wonder build failed: " + e.getMessage());
                executeDiscard(player, cardIndex);
            }
        } else {
            Log.logTurn(session.getAge(), session.getCurrentTurn(), "Cannot afford wonder stage. Discarding instead.");
            executeDiscard(player, cardIndex);
        }
    }
}
