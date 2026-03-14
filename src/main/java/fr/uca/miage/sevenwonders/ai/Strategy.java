package fr.uca.miage.sevenwonders.ai;

import fr.uca.miage.sevenwonders.models.Bank;

/**
 * Represents a strategy used by a bot in the Seven Wonders game.
 * <p>
 * Each implementation of this interface defines how a bot decides which card to
 * play and which action to perform during its turn:
 * <ul>
 * <li>Play a card (build a structure, produce a resource, etc.)</li>
 * [cite_start]
 * <li>Discard a card to gain coins [cite: 1]</li> [cite_start]
 * <li>Build a stage of its Wonder [cite: 1]</li>
 * </ul>
 * </p>
 *
 * <p>
 * This interface allows different bot behaviors (aggressive, economic,
 * [cite_start]balanced, etc.) to be implemented independently of the core game
 * logic[cite: 1].
 * </p>
 */
public interface Strategy {

    /**
     * [cite_start]Applies the bot's strategy to decide which card and which action
     * to take[cite: 1].
     *
     * @param bot
     *            [cite_start]the bot executing its turn [cite: 1]
     * @param bank
     *            [cite_start]the game's bank, used for transactions or resource
     *            checks [cite: 1]
     * @return an integer representing the chosen action (e.g., 0 = discard, 1 =
     *         [cite_start]play a card, 2 = build a Wonder stage) [cite: 1]
     */
    int applyStrategy(Bot bot, Bank bank);

    String getName();
}
