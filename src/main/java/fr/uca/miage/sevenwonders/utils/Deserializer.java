package fr.uca.miage.sevenwonders.utils;

import fr.uca.miage.sevenwonders.models.wonder.*;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Utility class for deserializing game data (cards and wonders) from JSON
 * files. The class provides methods to load cards for different ages and load
 * wonders, with support for selecting sides.
 */
public class Deserializer {
    // Private constructor to prevent instantiation
    private Deserializer() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * Enum representing the different sides of a Wonder. A side, B side, or a
     * random sides (rand).
     */
    public enum whichSide {
        /**
         * Picks only A sides
         */
        A,
        /**
         * Picks only B sides
         */
        B,
        /**
         * Picks A and B sides randomly
         */
        rand
    }

    private static JSONObject jsonObject;
    private static final java.util.Map<String, Card> allCards = new java.util.HashMap<>();

    public static Card getCardByName(String name) {
        return allCards.get(name);
    }

    public static void loadAllCards() {
        if (!allCards.isEmpty()) {
            return;
        }
        try {
            if (jsonObject == null) {
                String content = new String(Files.readAllBytes(Paths.get("data/cards.json")));
                jsonObject = new JSONObject(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return;
        }

        for (Card.Age age : Card.Age.values()) {
            String ageKey = "";
            switch (age) {
                case AGE_I :
                    ageKey = "age1";
                    break;
                case AGE_II :
                    ageKey = "age2";
                    break;
                case AGE_III :
                    ageKey = "age3";
                    break;
            }
            JSONObject ageObject = jsonObject.getJSONObject(ageKey);
            JSONArray cardsArray = ageObject.getJSONArray("cards");

            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject jsonCard = cardsArray.getJSONObject(i);
                Card card = parseCard(jsonCard, age);
                if (card != null && !allCards.containsKey(card.getName())) {
                    allCards.put(card.getName(), card);
                }
            }
        }
    }

    /**
     * Loads the cards for a given number of players and the specified game age.
     *
     * @param numPlayers
     *            the number of players in the game
     * @param age
     *            the age of the game (AGE_I, AGE_II, AGE_III)
     * @return a list of cards corresponding to the specified age and number of
     *         players
     */
    public static List<Card> loadCards(int numPlayers, Card.Age age) {
        try {
            if (jsonObject == null) {
                String content = new String(Files.readAllBytes(Paths.get("data/cards.json")));
                jsonObject = new JSONObject(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return new ArrayList<>();
        }

        // Determine which age to parse
        String ageKey = "";
        switch (age) {
            case AGE_I :
                ageKey = "age1";
                break;
            case AGE_II :
                ageKey = "age2";
                break;
            case AGE_III :
                ageKey = "age3";
                break;
        }

        JSONObject ageObject = jsonObject.getJSONObject(ageKey);
        JSONArray cardsArray = ageObject.getJSONArray("cards");

        List<Card> cards = parseCards(cardsArray, numPlayers, age);

        return cards;
    }

    private static List<Card> parseCards(JSONArray jsonArray, int numPlayers, Card.Age age) {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonCard = jsonArray.getJSONObject(i);

            // Check if this card should be included for the number of players
            JSONObject countPerNbPlayer = jsonCard.getJSONObject("countPerNbPlayer");
            int count = countPerNbPlayer.getInt(String.valueOf(numPlayers));

            // Add the card 'count' times
            for (int j = 0; j < count; j++) {
                Card card = parseCard(jsonCard, age);
                if (card != null) {
                    cards.add(card);
                }
            }
        }

        return cards;
    }

    private static Card parseCard(JSONObject json, Card.Age age) {
        String name = json.getString("name");
        Card.Color color = Card.Color.valueOf(json.getString("color"));
        Effect effect = parseEffectForCard(json.getJSONObject("effect"), color);
        Cost cost = json.has("requirements") ? parseCost(json.getJSONObject("requirements")) : Cost.free();
        String[] chainParents = new String[0];
        if (json.has("chainParents")) {
            JSONArray parentsArray = json.getJSONArray("chainParents");
            chainParents = new String[parentsArray.length()];
            for (int i = 0; i < parentsArray.length(); i++) {
                chainParents[i] = parentsArray.getString(i);
            }
        }

        String[] chainChildren = new String[0];
        if (json.has("chainChildren")) {
            JSONArray childrenArray = json.getJSONArray("chainChildren");
            chainChildren = new String[childrenArray.length()];
            for (int i = 0; i < childrenArray.length(); i++) {
                chainChildren[i] = childrenArray.getString(i);
            }
        }

        Card card = new Card(name, cost, age, color, effect, chainParents, chainChildren);

        return card;
    }

    private static Cost parseCost(JSONObject json) {
        // Handle free cost (no requirements)
        if (json == null || json.isEmpty()) {
            return Cost.free();
        }

        boolean hasGold = json.has("gold");
        boolean hasResources = json.has("resources");

        // Case 1: Only gold
        if (hasGold && !hasResources) {
            int goldAmount = json.getInt("gold");
            return Cost.gold(goldAmount);
        }

        // Case 2: Only resources
        if (!hasGold && hasResources) {
            return parseCostResourceString(json.getString("resources"));
        }

        // Case 3: Both gold and resources
        if (hasGold && hasResources) {
            int goldAmount = json.getInt("gold");
            Cost resourceCost = parseCostResourceString(json.getString("resources"));

            // For the new Cost system, we create a compound cost
            return Cost.compound(Cost.gold(goldAmount), resourceCost);
        }

        // Default: free cost
        return Cost.free();
    }

    private static Effect parseEffectForCard(JSONObject effectJson, Card.Color cardColor) {
        // The first key is the effect type
        String effectType = effectJson.keys().next();

        switch (effectType) {
            case "production" :
                return parseProductionEffect(effectJson.getJSONObject("production"));
            case "discount" :
                return parseDiscountEffect(effectJson.getJSONObject("discount"));
            case "gold" :
                return parseGoldEffect(effectJson.getInt("gold"));
            case "points" :
                return parsePointsEffect(effectJson.getInt("points"), getCategoryFromColor(cardColor));
            case "military" :
                return parseMilitaryEffect(effectJson.getInt("military"));
            case "science" :
                return parseScienceEffect(Effect.Science.ScienceSymbol.valueOf(effectJson.getString("science")));
            case "perBoardElement" :
                return parsePerBoardElementEffect(effectJson.getJSONObject("perBoardElement"), Optional.of(cardColor));
            case "action" :
                return parseActionEffect(effectJson.getString("action"));
            default :
                throw new IllegalArgumentException("Unknown effect: " + effectType);
        }
    }

    // Separate method for wonder stages (they don't have card colors)
    private static Effect parseEffectForWonderStage(JSONObject effectJson) {
        // For wonder stages, we don't have a card color, so we'll treat them as
        // immediate effects
        String effectType = effectJson.keys().next();

        switch (effectType) {
            case "production" :
                return parseProductionEffect(effectJson.getJSONObject("production"));
            case "discount" :
                return parseDiscountEffect(effectJson.getJSONObject("discount"));
            case "gold" :
                return parseGoldEffect(effectJson.getInt("gold"));
            case "points" :
                return parsePointsEffect(effectJson.getInt("points"), Effect.Category.WONDER);
            case "military" :
                return parseMilitaryEffect(effectJson.getInt("military"));
            case "science" :
                return parseScienceEffect(Effect.Science.ScienceSymbol.valueOf(effectJson.getString("science")));
            case "perBoardElement" :
                // For wonder stages, treat PerBoardElement as immediate
                return parsePerBoardElementEffect(effectJson.getJSONObject("perBoardElement"), Optional.empty());
            case "action" :
                return parseActionEffect(effectJson.getString("action"));
            default :
                throw new IllegalArgumentException("Unknown effect: " + effectType);
        }
    }

    private static Effect parseProductionEffect(JSONObject production) {
        String resources = production.getString("resources");
        return parseProductionResourceString(resources);
    }

    private static Effect.Production parseProductionResourceString(String resourceString) {
        // Handle choice resources (with /)
        if (resourceString.contains("/")) {
            String[] options = resourceString.split("/");
            List<List<Card.Materials>> choiceOptions = new ArrayList<>();
            for (String option : options) {
                List<Card.Materials> materialsInOption = new ArrayList<>();
                for (int i = 0; i < option.length(); i++) {
                    materialsInOption.add(parseSingleResource(String.valueOf(option.charAt(i))));
                }
                choiceOptions.add(materialsInOption);
            }
            return new Effect.Production.Choice(choiceOptions);
        }
        // Handle multiple of same resource (like "WW", "SSS")
        else {
            Card.Materials[] materials = new Card.Materials[resourceString.length()];
            for (int i = 0; i < resourceString.length(); i++) {
                materials[i] = parseSingleResource(String.valueOf(resourceString.charAt(i)));
            }
            return new Effect.Production.Fixed(materials);
        }
    }

    private static Effect parseDiscountEffect(JSONObject discount) {
        // 1. Déterminer les ressources affectées
        String resourceTypesString = discount.getString("resourceTypes");

        // Convertit la chaîne de ressources unique (ex: "CSOW") en tableau Materials[]
        List<Card.Materials> materials = parseFixedMaterialsString(resourceTypesString);

        // 2. Déterminer l'emplacement du voisin affecté
        JSONArray providersArray = discount.getJSONArray("providers");
        List<Effect.Discount.NeighborLocation> providers = new ArrayList<>();
        for (int i = 0; i < providersArray.length(); i++) {
            String providerStr = providersArray.getString(i);
            providers.add(Effect.Discount.NeighborLocation.valueOf(providerStr));
        }

        // 3. Déterminer le coût réduit
        int discountedCost;
        if (discount.has("discountedPrice")) {
            discountedCost = discount.getInt("discountedPrice"); // Lit la valeur si elle est présente
        } else {
            discountedCost = 0; // Définit à 0 si absente
        }

        // 4. Créer le record Discount
        return new Effect.Discount(materials, providers, discountedCost);
    }

    private static List<Card.Materials> parseFixedMaterialsString(String resourceString) {
        List<Card.Materials> materials = new ArrayList<>();
        for (int i = 0; i < resourceString.length(); i++) {
            materials.add(parseSingleResource(String.valueOf(resourceString.charAt(i))));
        }
        return materials;
    }

    private static Cost parseCostResourceString(String resourceString) {
        // Handle choice resources (with /)
        if (resourceString.contains("/")) {
            String[] options = resourceString.split("/");
            List<List<Card.Materials>> choiceOptions = new ArrayList<>();
            for (String option : options) {
                List<Card.Materials> materialsInOption = new ArrayList<>();
                for (int i = 0; i < option.length(); i++) {
                    materialsInOption.add(parseSingleResource(String.valueOf(option.charAt(i))));
                }
                choiceOptions.add(materialsInOption);
            }
            return Cost.choiceMaterials(choiceOptions);
        }
        // Handle multiple of same resource (like "WW", "SSS")
        else {
            Card.Materials[] materials = new Card.Materials[resourceString.length()];
            for (int i = 0; i < resourceString.length(); i++) {
                materials[i] = parseSingleResource(String.valueOf(resourceString.charAt(i)));
            }
            return Cost.materials(materials);
        }
    }

    private static Card.Materials parseSingleResource(String resource) {
        switch (resource) {
            case "W" :
                return Card.Materials.WOOD;
            case "S" :
                return Card.Materials.STONE;
            case "C" :
                return Card.Materials.CLAY;
            case "O" :
                return Card.Materials.ORE;
            case "G" :
                return Card.Materials.GLASS;
            case "P" :
                return Card.Materials.PAPYRUS;
            case "L" :
                return Card.Materials.TEXTILE;
            default :
                throw new IllegalArgumentException("Unknown resource: " + resource);
        }
    }

    private static Effect parseActionEffect(String action) {
        return new Effect.Action(Effect.Action.ActionType.valueOf(action));
    }

    private static Effect parseGoldEffect(int goldAmount) {
        return new Effect.Gold(goldAmount);
    }

    private static Effect parsePointsEffect(int points, Effect.Category category) {
        return new Effect.VictoryPoints(points, category);
    }

    private static Effect.Category getCategoryFromColor(Card.Color color) {
        return switch (color) {
            case BLUE -> Effect.Category.CIVILIAN;
            case GOLDEN -> Effect.Category.COMMERCIAL;
            case PURPLE -> Effect.Category.GUILD;
            case GREEN -> Effect.Category.SCIENCE;
            default -> Effect.Category.CIVILIAN; // Default fallback
        };
    }

    private static Effect parseMilitaryEffect(int militaryStrength) {
        return new Effect.Military(militaryStrength);
    }

    private static Effect parseScienceEffect(Effect.Science.ScienceSymbol scienceType) {
        return new Effect.Science(scienceType);
    }

    private static Effect parsePerBoardElementEffect(JSONObject perBoard, Optional<Card.Color> cardColor) {
        JSONArray boardsArray = perBoard.getJSONArray("boards");
        boolean includeSelf = false;
        boolean includeLeft = false;
        boolean includeRight = false;

        for (int i = 0; i < boardsArray.length(); i++) {
            String board = boardsArray.getString(i);
            switch (board) {
                case "SELF" :
                    includeSelf = true;
                    break;
                case "LEFT" :
                    includeLeft = true;
                    break;
                case "RIGHT" :
                    includeRight = true;
                    break;
            }
        }

        // Parse victory points effect if present
        Effect.VictoryPoints pointsEffect = null;
        if (perBoard.has("points")) {
            int points = perBoard.getInt("points");
            // Determine category for per-board element points
            // GUILD (purple cards) or COMMERCIAL (yellow cards)
            Effect.Category category = (cardColor.isEmpty())
                    ? Effect.Category.WONDER
                    : (cardColor.get() == Card.Color.PURPLE) ? Effect.Category.GUILD : Effect.Category.COMMERCIAL;
            pointsEffect = new Effect.VictoryPoints(points, category);
        }

        // Parse gold effect if present
        Effect.Gold goldEffect = null;
        if (perBoard.has("gold")) {
            int goldAmount = perBoard.getInt("gold");
            goldEffect = new Effect.Gold(goldAmount);
        }

        // Parse type and colors
        String type = perBoard.getString("type");

        String[] colors = null;
        if (perBoard.has("colors")) {
            JSONArray colorsArray = perBoard.getJSONArray("colors");
            colors = new String[colorsArray.length()];
            for (int i = 0; i < colorsArray.length(); i++) {
                colors[i] = colorsArray.getString(i);
            }
        }

        // Determine if this is immediate (golden cards) or end-game (purple cards)
        boolean immediate = (cardColor.isEmpty() || cardColor.get() == Card.Color.GOLDEN);

        return new Effect.PerBoardElement(includeSelf, includeLeft, includeRight, pointsEffect, goldEffect, type,
                colors, immediate);
    }

    /**
     * Parses a JSON array of wonders and filters them based on the desired side.
     *
     * @param wS
     *            the desired side of the wonder (A, B, or rand)
     * @return a list of wonders parsed from the JSON array
     */
    public static List<Wonder> loadWonders(whichSide wS) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("data/wonders.json")));
            JSONArray jsonArray = new JSONArray(content);

            // Filter the JSON array based on side before parsing
            JSONArray filteredArray = new JSONArray();
            Random random = new Random();

            switch (wS) {
                case A :
                    // Even indexes (0, 2, 4, ...) are A sides
                    for (int i = 0; i < jsonArray.length(); i += 2) {
                        filteredArray.put(jsonArray.getJSONObject(i));
                    }
                    break;
                case B :
                    // Odd indexes (1, 3, 5, ...) are B sides
                    for (int i = 1; i < jsonArray.length(); i += 2) {
                        filteredArray.put(jsonArray.getJSONObject(i));
                    }
                    break;
                case rand :
                    // For each wonder pair, randomly pick A or B side
                    for (int i = 0; i < jsonArray.length(); i += 2) {
                        int chosenIndex = i + random.nextInt(2); // i or i+1
                        filteredArray.put(jsonArray.getJSONObject(chosenIndex));
                    }
                    break;
            }

            List<Wonder> boards = parseWonders(filteredArray);

            return boards;

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return new ArrayList<>();
        }
    }

    private static List<Wonder> parseWonders(JSONArray jsonArray) {
        List<Wonder> boards = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonBoard = jsonArray.getJSONObject(i);
            Wonder wonder = parseWonder(jsonBoard);
            boards.add(wonder);
        }

        return boards;
    }

    private static Wonder parseWonder(JSONObject json) {
        String name = json.getString("name");
        Wonder.Side side = Wonder.Side.valueOf(json.getString("side"));

        JSONArray stagesArray = json.getJSONArray("stages");
        WonderStage[] stages = new WonderStage[stagesArray.length()];
        for (int i = 0; i < stagesArray.length(); i++) {
            stages[i] = parseWonderStage(stagesArray.getJSONObject(i));
        }

        Card.Materials startingResource = parseSingleResource(json.getString("startingResource"));
        Wonder board = new Wonder(name, startingResource, stages, side);

        return board;
    }

    private static WonderStage parseWonderStage(JSONObject json) {
        Cost cost = parseCost(json.getJSONObject("requirements"));

        JSONObject effectsObject = json.getJSONObject("effects");

        int effectCount = effectsObject.length();
        Effect[] effects = new Effect[effectCount];

        int index = 0;
        for (String key : effectsObject.keySet()) {
            JSONObject singleEffect = new JSONObject();
            singleEffect.put(key, effectsObject.get(key));
            effects[index++] = parseEffectForWonderStage(singleEffect);
        }

        return new WonderStage(cost, effects);
    }
}
