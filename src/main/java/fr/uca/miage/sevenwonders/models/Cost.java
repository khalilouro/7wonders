package fr.uca.miage.sevenwonders.models;

import fr.uca.miage.sevenwonders.models.card.Card;

import java.util.Arrays;
import java.util.List;

/**
 * Base interface for all costs in Seven Wonders
 */
public sealed interface Cost permits Cost.Free,Cost.Gold,Cost.Materials,Cost.Compound,Cost.Trading,Cost.ChoiceMaterials {

    /** Free cost - no payment required */
    record Free() implements Cost {
    }

    /** Cost requiring only gold */
    record Gold(int amount) implements Cost {
    }

    /** Cost requiring only materials */
    record Materials(Card.Materials[] materials) implements Cost {
        public Materials {
            if (materials == null) {
                materials = new Card.Materials[0];
            }
        }
    }

    /**
     * Cost requiring a choice of materials. The player can choose one of the
     * options. Each option is a list of materials.
     */
    record ChoiceMaterials(List<List<Card.Materials>> options) implements Cost {
        public ChoiceMaterials {
            if (options == null) {
                options = new java.util.ArrayList<>();
            }
        }
    }

    /** Compound cost combining multiple cost types */
    record Compound(List<Cost> costs) implements Cost {
        public Compound(Cost... costs) {
            this(Arrays.asList(costs));
        }
    }

    /** Trading decorator that wraps another cost with trading information */
    record Trading(Cost baseCost, NeighborTrading tradingInfo) implements Cost {
    }

    // Static factory methods
    static Cost free() {
        return new Free();
    }

    static Cost gold(int amount) {
        return new Gold(amount);
    }

    static Cost materials(Card.Materials... materials) {
        return new Materials(materials);
    }

    static Cost choiceMaterials(List<List<Card.Materials>> options) {
        return new ChoiceMaterials(options);
    }

    static Cost compound(Cost... costs) {
        return new Compound(costs);
    }

    static Cost withTrading(Cost baseCost, NeighborTrading tradingInfo) {
        return new Trading(baseCost, tradingInfo);
    }
}
