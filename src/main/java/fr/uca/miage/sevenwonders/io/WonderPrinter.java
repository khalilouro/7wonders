package fr.uca.miage.sevenwonders.io;

import fr.uca.miage.sevenwonders.models.*;
import fr.uca.miage.sevenwonders.models.wonder.*;
import fr.uca.miage.sevenwonders.models.card.*;

/**
 * Utility class for printing Wonders as ASCII art in the console.
 */
public class WonderPrinter {

    // ANSI color codes
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String CYAN = "\033[38;5;51m";
    private static final String GREEN = "\033[38;5;28m";
    private static final String YELLOW = "\033[38;5;220m";
    private static final String GREY = "\033[38;5;246m";

    /**
     * Prints a Wonder with its stages in ASCII art format.
     *
     * @param wonder
     *            the Wonder to print
     */
    public static void printWonder(Wonder wonder) {
        if (wonder == null) {
            System.out.println("(No Wonder assigned)");
            return;
        }

        int width = 76;

        // Top border
        System.out.println(CYAN + "╔" + "═".repeat(width - 2) + "╗" + RESET);

        // Wonder name and side
        String title = wonder.getName() + " (Side " + wonder.getCurrentSide() + ")";
        System.out.println(
                CYAN + "║" + RESET + " " + BOLD + centerText(title, width - 4) + RESET + " " + CYAN + "║" + RESET);

        // Starting resource
        String resource = "Starting Resource: " + getMaterialName(wonder.getStartingResource());
        System.out.println(CYAN + "║" + RESET + " " + padRight(resource, width - 4) + " " + CYAN + "║" + RESET);

        // Separator
        System.out.println(CYAN + "╠" + "═".repeat(width - 2) + "╣" + RESET);

        // Stages header
        String stagesHeader = BOLD + "STAGES:" + RESET;
        System.out.println(CYAN + "║" + RESET + " " + padRight(stagesHeader, width - 4) + " " + CYAN + "║" + RESET);

        // Display each stage
        for (int i = 0; i < wonder.getTotalStages(); i++) {
            boolean isBuilt = i < wonder.getStageIndex();
            WonderStage stage = wonder.stages[i];

            String status = isBuilt ? GREEN + "[X]" + RESET : GREY + "[ ]" + RESET;
            String stageNum = "Stage " + (i + 1);

            // Stage line
            String stageLine = status + " " + BOLD + stageNum + RESET;
            System.out.println(CYAN + "║" + RESET + " " + padRight(stageLine, width - 4) + " " + CYAN + "║" + RESET);

            // Cost
            String costText = "    Cost: " + formatStageCost(stage.getCosts());
            System.out.println(CYAN + "║" + RESET + " " + padRight(costText, width - 4) + " " + CYAN + "║" + RESET);

            // Effects
            String effectText = "    Effect: " + formatStageEffects(stage.getEffects());
            System.out.println(CYAN + "║" + RESET + " " + padRight(effectText, width - 4) + " " + CYAN + "║" + RESET);
        }

        // Progress indicator
        System.out.println(CYAN + "╠" + "═".repeat(width - 2) + "╣" + RESET);
        String progress = "Progress: " + wonder.getStageIndex() + "/" + wonder.getTotalStages() + " stages built";
        System.out.println(CYAN + "║" + RESET + " " + padRight(progress, width - 4) + " " + CYAN + "║" + RESET);

        // Bottom border
        System.out.println(CYAN + "╚" + "═".repeat(width - 2) + "╝" + RESET);
    }

    /**
     * Formats the cost of a Wonder stage.
     */
    private static String formatStageCost(Cost cost) {
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
                sb.append(getMaterialName(materials.materials()[i]));
            }
            return sb.toString();
        } else if (cost instanceof Cost.Compound compound) {
            return "Complex (" + compound.costs().size() + " components)";
        }
        return "Unknown";
    }

    /**
     * Formats the effects of a Wonder stage.
     */
    private static String formatStageEffects(Effect[] effects) {
        if (effects == null || effects.length == 0) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < effects.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(formatEffect(effects[i]));
        }
        return sb.toString();
    }

    /**
     * Formats a single effect.
     */
    private static String formatEffect(Effect effect) {
        if (effect instanceof Effect.VictoryPoints vp) {
            return "+" + vp.points() + " VP";
        } else if (effect instanceof Effect.Military mil) {
            return "+" + mil.strength() + " Military";
        } else if (effect instanceof Effect.Gold gold) {
            return "+" + gold.amount() + " Gold";
        } else if (effect instanceof Effect.Production prod) {
            if (prod.materials() != null && prod.materials().length > 0) {
                return "Produce: " + getMaterialName(prod.materials()[0]);
            }
            return "Production";
        } else if (effect instanceof Effect.Science sci) {
            return "Science: " + getScienceName(sci.symbol());
        }
        return "Special";
    }

    /**
     * Gets the name of a material.
     */
    private static String getMaterialName(Card.Materials material) {
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
     * Gets the name of a science symbol.
     */
    private static String getScienceName(Effect.Science.ScienceSymbol symbol) {
        return switch (symbol) {
            case COMPASS -> "Compass";
            case WHEEL -> "Wheel";
            case TABLET -> "Tablet";
            case ANY -> "Joker";
        };
    }

    /**
     * Gets the visual length of text (excluding ANSI escape codes).
     */
    private static int getVisualLength(String text) {
        // Remove ANSI escape codes to get actual visual length
        String withoutAnsi = text.replaceAll("\\u001b\\[[0-9;]*m", "");
        return withoutAnsi.length();
    }

    /**
     * Centers text within the specified width.
     */
    private static String centerText(String text, int width) {
        int visualLen = getVisualLength(text);
        int padding = (width - visualLen) / 2;
        int rightPadding = width - visualLen - padding;
        return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, rightPadding));
    }

    /**
     * Pads text to the right to reach the specified width.
     */
    private static String padRight(String text, int width) {
        int visualLen = getVisualLength(text);
        if (visualLen >= width) {
            return text.substring(0, Math.min(text.length(), width));
        }
        return text + " ".repeat(width - visualLen);
    }
}
