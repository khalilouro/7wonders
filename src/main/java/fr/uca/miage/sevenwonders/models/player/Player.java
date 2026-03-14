package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Cost;
import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.models.PurpleEffect;
import fr.uca.miage.sevenwonders.models.card.Card;
import fr.uca.miage.sevenwonders.models.wonder.Wonder;
import fr.uca.miage.sevenwonders.services.ConstructionService;
import fr.uca.miage.sevenwonders.services.ScoreCalculator;
import fr.uca.miage.sevenwonders.utils.Log;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The Facade for the Player entity. Coordinates the specific components:
 * Resources, Science, Military, and Board.
 */
public class Player {

    // Identity
    private String name;
    private int score;

    // Components (Composition)
    private final PlayerResources resources;
    private final PlayerScience science;
    private final PlayerMilitary military;
    private final PlayerBoard board;

    // Services
    private final ConstructionService constructionService;
    private final ScoreCalculator scoreCalculator;

    // Game Logic Flags
    private boolean canPlayLastCard = false;
    private int playFromDiscardCount = 0;
    private boolean canCopyGuild = false;
    private int freeBuildsPerAge = 0;
    private int usedFreeBuildsPerAge = 0;
    private final int[] victoryPointsByCategory = new int[Effect.Category.values().length];

    // Constructors
    public Player(String name) {
        this(name, null);
    }

    public Player(String name, Wonder wonder) {
        this.name = name;

        // Init Components
        this.resources = new PlayerResources();
        this.science = new PlayerScience();
        this.military = new PlayerMilitary();
        this.board = new PlayerBoard(wonder);

        // Init Services
        this.constructionService = new ConstructionService();
        this.scoreCalculator = new ScoreCalculator();
    }

    // -----------------------------------------------------------
    // FACADE METHODS (Delegating to Components)
    // -----------------------------------------------------------

    // --- Resources ---
    public int getGoldPoints() {
        return resources.getGold() + (resources.getSilver() / 3);
    }
    public PlayerResources getResources() {
        return resources;
    }
    public void addProductionMaterials(List<List<Card.Materials>> opts) {
        resources.addProductionMaterials(opts);
    }
    public void addProductionMaterial(Card.Materials mat) {
        resources.addProductionMaterial(mat);
    }

    // --- Board (Hand, Wonder, Neighbors) ---
    public PlayerBoard getBoard() {
        return board;
    }
    public Wonder getWonder() {
        return board.getWonder();
    }
    public void setWonderplayer(Wonder w) {
        board.setWonder(w);
    }
    public List<Card> getHand() {
        return board.getHand();
    }
    public void setHand(List<Card> h) {
        board.setHand(h);
    }
    public void addCardToHand(Card c, int pos) {
        board.addCard(c);
    }
    public Card getCard(int pos) {
        return board.getHand().get(pos);
    }
    public Card removeCardFromHand(int pos) {
        return board.removeCard(pos);
    }

    // Neighbors
    public void setNeighborhood(Player left, Player right) {
        board.setNeighborhood(left, right);
    }
    public void setPlayerInLeft(Player p) {
        board.setNeighborhood(p, board.getRight());
    }
    public void setPlayerInRight(Player p) {
        board.setNeighborhood(board.getLeft(), p);
    }
    public Player getPlayerInLeft() {
        return board.getLeft();
    }
    public Player getPlayerInRight() {
        return board.getRight();
    }
    public Player getLeft() {
        return board.getLeft();
    }
    public Player getRight() {
        return board.getRight();
    }
    public Player[] getNeighborhood() {
        return new Player[]{board.getLeft(), board.getRight()};
    }

    // --- Military ---
    public PlayerMilitary getMilitary() {
        return military;
    }
    public void addConflictPoints(int pts) {
        military.addConflictPoints(pts);
    }
    public void addMilitaryStrength(int str) {
        military.addStrength(str);
    }
    public int getMilitaryStrength() {
        return military.getStrength();
    }
    public int getConflictPoints() {
        return military.getConflictPoints();
    }

    // --- Science ---
    public PlayerScience getScience() {
        return science;
    }
    public void addScience(Effect.Science.ScienceSymbol sym) {
        science.addSymbol(sym);
    }
    public int calculateScienceScore() {
        return science.calculateScore();
    }

    // --- Game Actions ---
    public Card discard(int index) {
        Card c = board.removeCard(index);
        if (c != null) {
            Log.logEvent(this.name + " discards card: " + c.getName());
        }
        return c;
    }

    public Optional<Cost> canBuild(Card card) {
        return constructionService.canBuild(this, card);
    }

    public Optional<Cost> canBuild(Cost cost) {
        return constructionService.canBuild(this, cost);
    }

    public boolean canBuildViaChaining(Card card) {
        return constructionService.canBuildViaChaining(this, card);
    }

    public void computeFinalScore() {
        this.score = scoreCalculator.computeFinalScore(this);
    }

    // --- Effects & Scoring ---
    public void addVictoryPoints(int points, Effect.Category cat) {
        updateVictoryPoints(points, cat);
    }

    public void addVictoryPoints(int points) {
        updateVictoryPoints(points, Effect.Category.CIVILIAN);
    }

    public void updateVictoryPoints(int points, Effect.Category cat) {
        if (cat == null)
            cat = Effect.Category.CIVILIAN;
        if (cat != Effect.Category.TREASURY && cat != Effect.Category.SCIENCE && cat != Effect.Category.MILITARY) {
            int current = this.victoryPointsByCategory[cat.ordinal()];
            this.victoryPointsByCategory[cat.ordinal()] = current + points;
        } else {
            this.victoryPointsByCategory[cat.ordinal()] = points;
        }
    }

    public int getTotalVictoryPoints() {
        int sum = 0;
        for (int p : victoryPointsByCategory)
            sum += p;
        return sum;
    }

    /**
     * Helper method for stats to get points by specific category.
     */
    public int getPointsByCategory(Effect.Category category) {
        if (category == null)
            return 0;
        return victoryPointsByCategory[category.ordinal()];
    }

    public void addEffect(Effect effect) {
        if (effect instanceof Effect.Discount d)
            board.addDiscount(d);
    }

    public void addPurpleEffect(PurpleEffect p) {
        board.addPurpleEffect(p);
    }
    public void applyPurpleEffects() {
        for (PurpleEffect p : board.getPurpleEffects())
            p.applyPurpleEffect(this);
    }

    public void addAlreadyBuilt(Card c) {
        board.addAlreadyBuilt(c);
    }
    public List<String> getAlreadyBuilt() {
        return board.getAlreadyBuilt();
    }

    // Legacy/Session support
    public Map<String, Integer> getBoardElement() {
        return board.getBoardElements();
    }
    public List<List<Card.Materials>> getProduction() {
        return resources.getProduction();
    }
    public List<Effect.Discount> getDiscounts() {
        return board.getDiscounts();
    }
    public String getName() {
        return name;
    }
    public int getScore() {
        return score;
    }

    // --- Special Flags ---
    public boolean hasFreeBuildAvailable() {
        return freeBuildsPerAge > usedFreeBuildsPerAge;
    }
    public void useFreeBuild() {
        if (hasFreeBuildAvailable())
            usedFreeBuildsPerAge++;
    }
    public void addFreeBuildsPerAge(int c) {
        this.freeBuildsPerAge += c;
    }
    public void resetPerAgeEffects() {
        this.usedFreeBuildsPerAge = 0;
    }

    public void setCanPlayLastCard(boolean b) {
        this.canPlayLastCard = b;
    }
    public boolean canPlayLastCard() {
        return canPlayLastCard;
    }

    public void addPlayFromDiscard(int c) {
        this.playFromDiscardCount += c;
    }
    public boolean canPlayFromDiscard() {
        return playFromDiscardCount > 0;
    }
    public void usePlayFromDiscard() {
        if (playFromDiscardCount > 0)
            playFromDiscardCount--;
    }

    public void setCanCopyGuild(boolean b) {
        this.canCopyGuild = b;
    }
    public boolean canCopyGuild() {
        return canCopyGuild;
    }

    // Compatibility getters
    public int getTablet() {
        return science.getTablet();
    }
    public int getCompass() {
        return science.getCompass();
    }
    public int getWheel() {
        return science.getWheel();
    }
    public int getAnyScience() {
        return science.getAnyScience();
    }
}
