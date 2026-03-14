package fr.uca.miage.sevenwonders.models.player;

public class PlayerMilitary {
    private int conflictPoints;
    private int militaryStrength;
    private int defeatTokens;

    public void addStrength(int amount) {
        this.militaryStrength += amount;
    }

    public void addConflictPoints(int points) {
        this.conflictPoints += points;
    }

    public void addDefeatToken() {
        this.defeatTokens++;
    }

    public int getConflictPoints() {
        return conflictPoints;
    }
    public int getStrength() {
        return militaryStrength;
    }
    public int getDefeatTokens() {
        return defeatTokens;
    }
}
