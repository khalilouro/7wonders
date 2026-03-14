package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.Effect;
import fr.uca.miage.sevenwonders.services.ScoreCalculator;

public class PlayerScience {
    private int tablet;
    private int compass;
    private int wheel;
    private int anyScience;

    public void addSymbol(Effect.Science.ScienceSymbol symbol) {
        if (symbol == null)
            return;
        switch (symbol) {
            case COMPASS -> this.compass++;
            case WHEEL -> this.wheel++;
            case TABLET -> this.tablet++;
            case ANY -> this.anyScience++;
        }
    }

    public int calculateScore() {
        return ScoreCalculator.calculateScienceScore(tablet, compass, wheel, anyScience);
    }

    public int getTablet() {
        return tablet;
    }
    public int getCompass() {
        return compass;
    }
    public int getWheel() {
        return wheel;
    }
    public int getAnyScience() {
        return anyScience;
    }
}
