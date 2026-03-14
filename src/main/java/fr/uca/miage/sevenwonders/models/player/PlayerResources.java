package fr.uca.miage.sevenwonders.models.player;

import fr.uca.miage.sevenwonders.models.card.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerResources {
    private int gold;
    private int silver;
    private final List<List<Card.Materials>> production;

    public PlayerResources() {
        this.gold = 0;
        this.silver = 0;
        this.production = new ArrayList<>();
    }

    public int getGold() {
        return gold;
    }
    public void setGold(int gold) {
        this.gold = gold;
    }
    public void addGold(int amount) {
        this.gold += amount;
    }
    public void removeGold(int amount) {
        this.gold = Math.max(0, this.gold - amount);
    }

    public int getSilver() {
        return silver;
    }
    public void setSilver(int silver) {
        this.silver = silver;
    }
    public void addSilver(int amount) {
        this.silver += amount;
    }

    public List<List<Card.Materials>> getProduction() {
        return production;
    }

    public void addProductionMaterial(Card.Materials material) {
        this.production.add(Collections.singletonList(material));
    }

    public void addProductionMaterials(List<List<Card.Materials>> options) {
        this.production.addAll(options);
    }
}
