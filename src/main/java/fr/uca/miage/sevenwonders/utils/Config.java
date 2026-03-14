package fr.uca.miage.sevenwonders.utils;

import org.tomlj.*;
import fr.uca.miage.sevenwonders.ai.Bot;
import fr.uca.miage.sevenwonders.models.player.Player;
import fr.uca.miage.sevenwonders.ai.Strategy;
import fr.uca.miage.sevenwonders.ai.strategies.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

    private static Config instance;

    private static final Path cfgPath = GetConfigFilePath.getConfigFilePath();

    // --- Constraints ---
    private static final List<String> VALID_WONDER_SIDES = Arrays.asList("a", "b", "both", "random");
    private static final List<String> VALID_STRATEGIES = Arrays.asList("random", "military", "wonder", "blue",
            "science", "economic", "adaptive", "minmax", "mar");

    // --- Fields initialized with default fallback values ---
    private int numberOfPlayers = 4;
    private String wonderSidesToUse = "both";
    private int numberOfThreads = 1;
    private int gamesToPlay = 500;
    private String mainColor = "cyan";
    private String accentColor = "yellow";
    private String secondaryColor = "white";
    private boolean enableRealPlayer = false;
    private boolean shuffleBots = true;

    // Internal storage for bot definitions
    // We initialize this with a default "Random" blueprint.
    // This list is only overwritten if the user defines [[bots]] in the TOML file.
    private List<BotBlueprint> botBlueprints = new ArrayList<>();

    {
        botBlueprints.add(new BotBlueprint("random", "Random"));
    }

    // Internal record to hold the "recipe" for a bot
    private record BotBlueprint(String strategy, String name) {
    }

    /**
     * Checks if the config file exists. If not, it copies the default resource.
     */
    private static void initConfigFile() {
        try {
            Path cfgDir = GetConfigFilePath.getConfigDirPath();
            if (!Files.exists(cfgDir)) {
                Files.createDirectory(cfgDir);
            }

            if (!Files.exists(cfgPath)) {
                Path cfgDefault = Paths.get("ressources/config.toml");
                if (Files.exists(cfgDefault)) {
                    Files.copy(cfgDefault, cfgPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    throw new IOException("Default config file not found at: " + cfgDefault.toAbsolutePath());
                }
            }
        } catch (Exception e) {
            throw new Error("Error occurred while setting up configuration: " + e.getMessage(), e);
        }
    }

    private Strategy createStrategyFromString(String strategyName) {
        switch (strategyName.toLowerCase()) {
            case "random" :
                return new RandomStrategy();
            case "military" :
                return new MilitaryStrategy();
            case "wonder" :
                return new WonderStrategy();
            case "science" :
                return new ScienceStrategy();
            case "blue" :
                return new BlueStrategy();
            case "economic" :
                return new EconomicStrategy();
            case "adaptive" :
                return new AdaptiveStrategy();
            case "minmax" :
                return new MinMaxStrategy();
            case "mar" :
                return new MarStrategy();
            default :
                throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        }
    }

    /**
     * Private Constructor: Prevents direct initialization.
     */
    private Config() {
        initConfigFile();

        try {
            TomlParseResult result = Toml.parse(cfgPath);

            if (result.hasErrors()) {
                result.errors().forEach(error -> System.err.println(error.toString()));
                throw new Error("Configuration file contains syntax errors.");
            }

            // 1. Process Scalar Values
            for (String dottedKey : result.dottedKeySet()) {
                String[] keyParts = dottedKey.split("\\.", 2);
                if (keyParts.length == 2) {
                    processConfigValue(keyParts[0], keyParts[1], result);
                }
            }

            // 2. Process Bots Array
            // If "bots" is defined in TOML, we overwrite the default Random blueprint.
            // If "bots" is NOT in TOML, we keep the default Random blueprint.
            if (result.contains("bots")) {
                parseBotsSection(result);
            }

        } catch (Exception e) {
            throw new Error("Error while parsing toml config: " + e.getMessage(), e);
        }
    }

    private void parseBotsSection(TomlParseResult result) {
        TomlArray botsArray = result.getArray("bots");
        // If the array exists but is empty, we warn and keep the default.
        if (botsArray == null || botsArray.isEmpty()) {
            System.err.println("Warning: 'bots' array found but empty. Using default Random strategy.");
            return;
        }

        List<BotBlueprint> parsedBlueprints = new ArrayList<>();

        for (int i = 0; i < botsArray.size(); i++) {
            if (botsArray.get(i)instanceof TomlTable botTable) {
                String strategy = botTable.getString("strategy");
                String name = botTable.getString("name");

                if (strategy == null || !VALID_STRATEGIES.contains(strategy.toLowerCase())) {
                    throw new Error("Invalid bot strategy at index " + i + ": " + strategy + ". Valid strategies: "
                            + VALID_STRATEGIES);
                }
                parsedBlueprints.add(new BotBlueprint(strategy, name));
            }
        }
        this.botBlueprints = parsedBlueprints;
    }

    private void processConfigValue(String section, String key, TomlParseResult result) {
        String fullKey = section + "." + key;
        switch (section) {
            case "general" :
                if (key.equals("number_of_players")) {
                    int parsedPlayers = result.getLong(fullKey).intValue();
                    if (parsedPlayers > 7 || parsedPlayers < 3) {
                        throw new Error("Invali number of players :" + parsedPlayers);
                    } else {
                        this.numberOfPlayers = parsedPlayers;

                    }
                }
                if (key.equals("wonder_sides_to_use")) {
                    String parsedSides = result.getString(fullKey);
                    if (VALID_WONDER_SIDES.contains(parsedSides)) {
                        this.wonderSidesToUse = parsedSides;
                    } else {
                        throw new Error("Unkown wonder side : " + parsedSides);
                    }
                }
                if (key.equals("number_of_threads"))
                    this.numberOfThreads = result.getLong(fullKey).intValue();
                if (key.equals("games_to_play"))
                    this.gamesToPlay = result.getLong(fullKey).intValue();
                if (key.equals("enable_real_player"))
                    this.enableRealPlayer = result.getBoolean(fullKey);
                if (key.equals("shuffle_bots"))
                    this.enableRealPlayer = result.getBoolean(fullKey);
                break;
            case "colors" :
                if (key.equals("main")) {
                    this.mainColor = result.getString(fullKey);
                }
                if (key.equals("accent")) {
                    this.accentColor = result.getString(fullKey);
                }
                if (key.equals("secondary")) {
                    this.secondaryColor = result.getString(fullKey);
                }
                break;

        }
    }

    public String getValueANSI(String colorName) {
        if (colorName == null)
            return "\u001B[0m"; // Reset
        switch (colorName.toLowerCase()) {
            case "black" :
                return "\u001B[30m";
            case "red" :
                return "\u001B[31m";
            case "green" :
                return "\u001B[32m";
            case "yellow" :
                return "\u001B[33m";
            case "blue" :
                return "\u001B[34m";
            case "purple" :
                return "\u001B[35m";
            case "cyan" :
                return "\u001B[36m";
            case "white" :
                return "\u001B[37m";
            case "reset" :
                return "\u001B[0m";
            default :
                return "\u001B[0m"; // Default to reset if unknown
        }
    }

    /**
     * Public Static Getter: Provides global access to the single instance. This
     * initializes the object only when it's first needed (Lazy Loading).
     *
     * @return The single Config instance.
     */
    public static Config getInstance() {
        if (instance == null) {
            // Synchronization is crucial for thread safety in a multithreaded environment
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    /**
     * Generates a fresh list of Players (Bots) for a new game session. Ensures the
     * list size matches 'number_of_players' exactly. Handles naming conflicts
     * (e.g., "Bot", "Bot 2", "Bot 3").
     */
    public List<Player> getBots() {
        List<Player> players = new ArrayList<>();

        if (isRealPlayerEnabled()) {
            players.add(new Bot("Human Player", new ConsoleStrategy()));
        }

        // Use local variable for blueprints to avoid race conditions or modification
        List<BotBlueprint> definitions = this.botBlueprints;

        // Safety check: ensure definitions is never empty
        if (definitions.isEmpty()) {
            definitions = new ArrayList<>();
            definitions.add(new BotBlueprint("random", "Random"));
        }

        int definitionsCount = definitions.size();
        int botsToCreate = isRealPlayerEnabled() ? this.numberOfPlayers - 1 : this.numberOfPlayers;

        for (int i = 0; i < botsToCreate; i++) {
            // 1. Cycle Logic: Pick a blueprint based on index
            BotBlueprint blueprint = definitions.get(i % definitionsCount);

            // 2. Name Generation Logic
            // Calculate which cycle we are in (0 = first time, 1 = second time, etc.)
            int cycle = i / definitionsCount;

            String strategyType = blueprint.strategy;
            String baseName = blueprint.name;

            // If no name provided, derive it from the strategy (e.g., "MinMax")
            if (baseName == null) {
                Strategy tempStrat = createStrategyFromString(strategyType);
                baseName = tempStrat.getName();
            }

            // If this is a recycled bot (cycle > 0), append the number (e.g., "Random 2")
            String finalName = baseName;
            if (cycle > 0) {
                finalName = baseName + " " + (cycle + 1);
            }

            // 3. Object Creation
            Player newPlayer;
            Strategy strategy = createStrategyFromString(strategyType);
            newPlayer = new Bot(finalName, strategy);

            players.add(newPlayer);
        }

        return players;
    }

    // --- Getters ---
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public String getWonderSidesToUse() {
        return wonderSidesToUse;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public String getMainColor() {
        return mainColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public int getGamesToPlay() {
        return gamesToPlay;
    }

    public boolean isRealPlayerEnabled() {
        return enableRealPlayer;
    }

    public boolean getShuffleBots() {
        return shuffleBots;
    }
}
