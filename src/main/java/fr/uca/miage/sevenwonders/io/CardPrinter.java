package fr.uca.miage.sevenwonders.io;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.card.*;
import fr.uca.miage.sevenwonders.models.player.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for printing cards as ASCII art in the console.
 */
public class CardPrinter {

    private static final int CARD_WIDTH = 28;
    private static final int CARD_SPACING = 3;

    // ANSI color codes
    private static final String RESET = "\033[0m";
    private static final String BROWN_COLOR = "\033[38;5;94m";
    private static final String GREY_COLOR = "\033[38;5;246m";
    private static final String BLUE_COLOR = "\033[38;5;33m";
    private static final String GREEN_COLOR = "\033[38;5;28m";
    private static final String GOLDEN_COLOR = "\033[38;5;220m";
    private static final String RED_COLOR = "\033[38;5;196m";
    private static final String PURPLE_COLOR = "\033[38;5;129m";
    private static final String BOLD = "\033[1m";
    private static final String DIM = "\033[2m";
    private static final String DARK_GREY = "\033[38;5;240m";

    /**
     * Prints a hand of cards side-by-side in ASCII art format.
     *
     * @param hand
     *            the list of cards to print
     */
    public static void printHand(List<Card> hand) {
        if (hand == null || hand.isEmpty()) {
            System.out.println("(Empty hand)");
            return;
        }

        // Generate card strings for all cards
        List<List<String>> cardLines = new ArrayList<>();
        for (Card card : hand) {
            cardLines.add(getCardLines(card));
        }

        // Print cards side-by-side (max 3 per row for readability)
        int cardsPerRow = Math.min(3, hand.size());
        for (int startIdx = 0; startIdx < hand.size(); startIdx += cardsPerRow) {
            int endIdx = Math.min(startIdx + cardsPerRow, hand.size());
            printCardRow(cardLines.subList(startIdx, endIdx), startIdx);
            if (endIdx < hand.size()) {
                System.out.println(); // Add spacing between rows
            }
        }
    }

    /**
     * Prints a hand of cards with playability indicators. Unplayable cards are
     * shown in grey with a ✗ marker. Playable cards are shown in color with a ✓
     * marker.
     *
     * @param hand
     *            the list of cards to print
     * @param player
     *            the player to check card playability for
     */
    public static void printHandWithPlayability(List<Card> hand, Player player) {
        if (hand == null || hand.isEmpty()) {
            System.out.println("(Empty hand)");
            return;
        }

        // Generate card strings for all cards with playability info
        List<List<String>> cardLines = new ArrayList<>();
        List<Boolean> playability = new ArrayList<>();

        for (Card card : hand) {
            boolean canPlay = player.canBuild(card).isPresent();
            playability.add(canPlay);
            cardLines.add(getCardLinesWithPlayability(card, canPlay));
        }

        // Print cards side-by-side (max 3 per row for readability)
        int cardsPerRow = Math.min(3, hand.size());
        for (int startIdx = 0; startIdx < hand.size(); startIdx += cardsPerRow) {
            int endIdx = Math.min(startIdx + cardsPerRow, hand.size());
            printCardRowWithPlayability(cardLines.subList(startIdx, endIdx), playability.subList(startIdx, endIdx),
                    startIdx);
            if (endIdx < hand.size()) {
                System.out.println(); // Add spacing between rows
            }
        }
    }

    /**
     * Prints a single row of cards side-by-side.
     *
     * @param cardsInRow
     *            list of card line lists to print
     * @param startIndex
     *            the starting index for card numbering
     */
    private static void printCardRow(List<List<String>> cardsInRow, int startIndex) {
        int maxLines = cardsInRow.stream().mapToInt(List::size).max().orElse(0);

        for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
            for (int cardIdx = 0; cardIdx < cardsInRow.size(); cardIdx++) {
                List<String> cardLines = cardsInRow.get(cardIdx);
                if (lineIdx < cardLines.size()) {
                    System.out.print(cardLines.get(lineIdx));
                } else {
                    System.out.print(" ".repeat(CARD_WIDTH));
                }
                if (cardIdx < cardsInRow.size() - 1) {
                    System.out.print(" ".repeat(CARD_SPACING));
                }
            }
            System.out.println();
        }

        // Print card indices below each card
        for (int cardIdx = 0; cardIdx < cardsInRow.size(); cardIdx++) {
            String indexLabel = "[" + (startIndex + cardIdx) + "]";
            int padding = (CARD_WIDTH - indexLabel.length()) / 2;
            System.out.print(" ".repeat(padding) + BOLD + indexLabel + RESET
                    + " ".repeat(CARD_WIDTH - padding - indexLabel.length()));
            if (cardIdx < cardsInRow.size() - 1) {
                System.out.print(" ".repeat(CARD_SPACING));
            }
        }
        System.out.println();
    }

    /**
     * Generates the ASCII art lines for a single card.
     *
     * @param card
     *            the card to render
     * @return list of strings representing each line of the card
     */
    private static List<String> getCardLines(Card card) {
        List<String> lines = new ArrayList<>();
        String color = getCardColor(card.getColor());

        // Top border with color
        lines.add(color + "╔" + "═".repeat(CARD_WIDTH - 2) + "╗" + RESET);

        // Card name (truncate if too long)
        String name = truncate(card.getName(), CARD_WIDTH - 4);
        lines.add(color + "║" + RESET + " " + BOLD + centerText(name, CARD_WIDTH - 4) + RESET + " " + color + "║"
                + RESET);

        // Separator
        lines.add(color + "╠" + "═".repeat(CARD_WIDTH - 2) + "╣" + RESET);

        // Color/Type
        String colorName = card.getColor().toString();
        lines.add(
                color + "║" + RESET + " " + padRight("Type: " + colorName, CARD_WIDTH - 4) + " " + color + "║" + RESET);

        // Age
        String ageText = "Âge " + card.getAge().getValue();
        lines.add(color + "║" + RESET + " " + padRight(ageText, CARD_WIDTH - 4) + " " + color + "║" + RESET);

        // Separator
        lines.add(color + "╟" + "─".repeat(CARD_WIDTH - 2) + "╢" + RESET);

        // Cost
        String costText = "Coût: " + formatCost(card.getCost());
        lines.add(color + "║" + RESET + " " + padRight(costText, CARD_WIDTH - 4) + " " + color + "║" + RESET);

        // Effect
        String effectText = "Effet: " + formatEffect(card.getEffect());
        lines.add(color + "║" + RESET + " " + padRight(effectText, CARD_WIDTH - 4) + " " + color + "║" + RESET);

        // Bottom border
        lines.add(color + "╚" + "═".repeat(CARD_WIDTH - 2) + "╝" + RESET);

        return lines;
    }

    /**
     * Generates the ASCII art lines for a single card with playability indicator.
     *
     * @param card
     *            the card to render
     * @param canPlay
     *            whether the card can be played
     * @return list of strings representing each line of the card
     */
    private static List<String> getCardLinesWithPlayability(Card card, boolean canPlay) {
        List<String> lines = new ArrayList<>();
        String color = canPlay ? getCardColor(card.getColor()) : DARK_GREY;
        String textStyle = canPlay ? "" : DIM;

        // Top border with color
        lines.add(color + "╔" + "═".repeat(CARD_WIDTH - 2) + "╗" + RESET);

        // Card name with playability indicator
        String indicator = canPlay ? GREEN_COLOR + "OK" + RESET : RED_COLOR + "NO" + RESET;
        String name = truncate(card.getName(), CARD_WIDTH - 7);
        lines.add(color + "║" + RESET + " " + indicator + " " + textStyle + BOLD + name + RESET
                + " ".repeat(CARD_WIDTH - 7 - name.length()) + " " + color + "║" + RESET);

        // Separator
        lines.add(color + "╠" + "═".repeat(CARD_WIDTH - 2) + "╣" + RESET);

        // Color/Type
        String colorName = card.getColor().toString();
        lines.add(color + "║" + RESET + " " + textStyle + padRight("Type: " + colorName, CARD_WIDTH - 4) + RESET + " "
                + color + "║" + RESET);

        // Age
        String ageText = "Age " + card.getAge().getValue();
        lines.add(color + "║" + RESET + " " + textStyle + padRight(ageText, CARD_WIDTH - 4) + RESET + " " + color + "║"
                + RESET);

        // Separator
        lines.add(color + "╟" + "─".repeat(CARD_WIDTH - 2) + "╢" + RESET);

        // Cost
        String costText = "Cost: " + formatCost(card.getCost());
        lines.add(color + "║" + RESET + " " + textStyle + padRight(costText, CARD_WIDTH - 4) + RESET + " " + color + "║"
                + RESET);

        // Effect
        String effectText = "Effect: " + formatEffect(card.getEffect());
        lines.add(color + "║" + RESET + " " + textStyle + padRight(effectText, CARD_WIDTH - 4) + RESET + " " + color
                + "║" + RESET);

        // Bottom border
        lines.add(color + "╚" + "═".repeat(CARD_WIDTH - 2) + "╝" + RESET);

        return lines;
    }

    /**
     * Prints a single row of cards side-by-side with playability indicators.
     *
     * @param cardsInRow
     *            list of card line lists to print
     * @param playability
     *            list of playability flags for each card
     * @param startIndex
     *            the starting index for card numbering
     */
    private static void printCardRowWithPlayability(List<List<String>> cardsInRow, List<Boolean> playability,
            int startIndex) {
        int maxLines = cardsInRow.stream().mapToInt(List::size).max().orElse(0);

        for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
            for (int cardIdx = 0; cardIdx < cardsInRow.size(); cardIdx++) {
                List<String> cardLines = cardsInRow.get(cardIdx);
                if (lineIdx < cardLines.size()) {
                    System.out.print(cardLines.get(lineIdx));
                } else {
                    System.out.print(" ".repeat(CARD_WIDTH));
                }
                if (cardIdx < cardsInRow.size() - 1) {
                    System.out.print(" ".repeat(CARD_SPACING));
                }
            }
            System.out.println();
        }

        // Print card indices below each card with playability status
        for (int cardIdx = 0; cardIdx < cardsInRow.size(); cardIdx++) {
            boolean canPlay = playability.get(cardIdx);
            String indexLabel = "[" + (startIndex + cardIdx) + "]";
            String statusLabel = canPlay ? " OK" : " NO";
            String fullLabel = indexLabel + statusLabel;
            int padding = (CARD_WIDTH - fullLabel.length()) / 2;
            System.out.print(" ".repeat(padding) + BOLD + indexLabel + RESET + statusLabel
                    + " ".repeat(CARD_WIDTH - padding - fullLabel.length()));
            if (cardIdx < cardsInRow.size() - 1) {
                System.out.print(" ".repeat(CARD_SPACING));
            }
        }
        System.out.println();
    }

    /**
     * Gets the ANSI color code for a card color.
     */
    private static String getCardColor(Card.Color color) {
        return switch (color) {
            case BROWN -> BROWN_COLOR;
            case GREY -> GREY_COLOR;
            case BLUE -> BLUE_COLOR;
            case GREEN -> GREEN_COLOR;
            case GOLDEN -> GOLDEN_COLOR;
            case RED -> RED_COLOR;
            case PURPLE -> PURPLE_COLOR;
        };
    }

    /**
     * Formats the cost for display.
     */
    private static String formatCost(Cost cost) {
        if (cost instanceof Cost.Free) {
            return "Free";
        } else if (cost instanceof Cost.Gold gold) {
            return gold.amount() + " Gold";
        } else if (cost instanceof Cost.Materials materials) {
            if (materials.materials().length == 0) {
                return "Free";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < materials.materials().length; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(getMaterialSymbol(materials.materials()[i]));
            }
            return sb.toString();
        } else if (cost instanceof Cost.Compound compound) {
            return "Complex";
        } else if (cost instanceof Cost.Trading trading) {
            return formatCost(trading.baseCost());
        }
        return "Unknown";
    }

    /**
     * Gets a symbol for a material.
     */
    private static String getMaterialSymbol(Card.Materials material) {
        return switch (material) {
            case WOOD -> "Wood";
            case STONE -> "Stone";
            case CLAY -> "Clay";
            case ORE -> "Ore";
            case GLASS -> "Glass";
            case PAPYRUS -> "Papyrus";
            case TEXTILE -> "Textile";
        };
    }

    /**
     * Formats the effect for display.
     */
    private static String formatEffect(Effect effect) {
        if (effect instanceof Effect.VictoryPoints vp) {
            return vp.points() + " VP";
        } else if (effect instanceof Effect.Military mil) {
            return mil.strength() + " Military";
        } else if (effect instanceof Effect.Gold gold) {
            return gold.amount() + " Gold";
        } else if (effect instanceof Effect.Production prod) {
            if (prod.materials() == null || prod.materials().length == 0) {
                return "Production";
            }
            return "Prod: " + getMaterialSymbol(prod.materials()[0]);
        } else if (effect instanceof Effect.Science sci) {
            return "Science: " + getScienceSymbol(sci.symbol());
        } else if (effect instanceof Effect.Discount) {
            return "Discount";
        } else if (effect instanceof Effect.PerBoardElement) {
            return "Per Element";
        } else if (effect instanceof Effect.Action) {
            return "Action";
        }
        return "Unknown";
    }

    /**
     * Gets a symbol for a science type.
     */
    private static String getScienceSymbol(Effect.Science.ScienceSymbol symbol) {
        return switch (symbol) {
            case COMPASS -> "Compass";
            case WHEEL -> "Wheel";
            case TABLET -> "Tablet";
            case ANY -> "Joker";
        };
    }

    /**
     * Truncates text to fit within the specified width.
     */
    private static String truncate(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return text;
        }
        return text.substring(0, maxWidth - 1) + "…";
    }

    /**
     * Centers text within the specified width.
     */
    private static String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        int rightPadding = width - text.length() - padding;
        return " ".repeat(padding) + text + " ".repeat(rightPadding);
    }

    /**
     * Pads text to the right to reach the specified width.
     */
    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }
}
