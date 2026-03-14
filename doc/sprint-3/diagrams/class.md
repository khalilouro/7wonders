```mermaid
classDiagram
direction LR

%% ============================
%% ENUMERATIONS
%% ============================

class Age {
    <<enumeration>>
    AGE_I
    AGE_II
    AGE_III
}

class Color {
    <<enumeration>>
    BROWN
    GRAY
    BLUE
    GREEN
    GOLDEN
    RED
    PURPLE
}

class Materials {
    <<enumeration>>
    WOOD
    STONE
    CLAY
    ORE
    GLASS
    PAPYRUS
    TEXTILE
}

class EffectType {
    <<enumeration>>
    RESOURCE
    SCIENCE
    MILITARY
    VICTORY
    COIN
    WONDER
}

class Side {
    <<enumeration>>
    A
    B
}

%% ============================
%% CLASSES PRINCIPALES
%% ============================

class Session {
    + Player[4] players
    - Deck deck
    + Age AGE
    - List~Wonder~ wonders
    - Bank bank
    + Session()
    + void distributeCards()
    + void startGame()
    + void conflictResolution(Player[] players, Age age)
}

class Player {
    + String name
    + int score
    - static final int HAND_SIZE
    - Card[] hand
    - Wonder wonder
    - int total_gold
    - int total_silver
    - int victoryPoints
    - int militaryPoints
    - List~Card.Materials~ produces
    - Player playerInLeft
    - Player playerInRight
    - Player[] neighborhood
    + Player(String name)
    + Player(String name, Wonder wonder)
    + Card[] getHand()
    + void setHand(Card[] newHand)
    + void addCardToHand(Card, int)
    + Card removeCardFromHand(int)
    + void setScore(int)
    + int getScore()
    + String getName()
    + void setName(String)
    + Wonder getWonder()
    + void setWonderplayer(Wonder)
    + void addVictoryPoints(int)
    + void addMilitaryPoints(int)
    + int getVictoryPoints()
    + int getMilitaryPoints()
    + Player getPlayerInLeft()
    + Player getPlayerInRight()
    + void setPlayerInLeft(Player)
    + void setPlayerInRight(Player)
    + Player[] getNeighborhood()
    + void setNeighborhood(Player left, Player right)
    + void addProductionMaterials(Card.Materials[])
    + boolean canBuild(int)
    + void discard(int, Bank)
}

class Bot {
    - Strategy strategy
    + Bot(String name, Strategy strategy)
    + int applyStrategy(Bot bot, Bank bank)
}

class Deck {
    - List~Card~ cards
    + Deck()
    + Deck(Age age)
    + Card drawCard()
    + void shuffle()
    + boolean isEmpty()
}

class Card {
    - Cost cost
    - Age age
    - Color color
    - String name
    - Effect effect
    + Card(Cost cost, Age age, Color color, String name)
    + Card(String name, Color color, Age age, Cost cost, Effect effect)
    + String getName()
    + Cost getCost()
    + Age getAge()
    + Color getColor()
    + Effect getEffect()
}

class Cost {
    <<interface>>
}

class CostGold {
    - int gold_amount
    + CostGold(int)
}

class CostMaterials {
    - List~Materials~ materials
    + CostMaterials(List~Materials~)
}

class CostGoldMaterials {
    - List~Materials~ materials
    - int gold_amount
    + CostGoldMaterials(List~Materials~, int)
}

class Effect {
    - EffectType type
    - int value
    - String symbol
    + Effect(EffectType type, int value, String symbol)
    + EffectType getType()
    + int getValue()
    + String getSymbol()
}

class Bank {
    - int totalCoins
    + Bank(int)
    + void DepositSilver(int, Player)
    + void WithdrawSilver(int, Player)
    + int getTotalCoins()
}

class Wonder {
    - String name
    - Side currentSide
    - int currentStage
    - int totalStages
    - List~WonderStage~ stages
    + Wonder(String name, int totalStages)
    + String getName()
    + int getCurrentStage()
    + int getTotalStages()
    + Side getCurrentSide()
    + void setCurrentSide(Side)
    + boolean buildStage()
    + boolean isCompletedStage()
    + List~WonderStage~ getStages()
}

class WonderStage {
    - String name
    - Cost cost
    - Effect effect
    + WonderStage(String name, Cost cost, Effect effect)
    + String getName()
    + Cost getCost()
    + Effect getEffect()
}

%% ============================
%% STRATEGY SYSTEM
%% ============================

class Strategy {
    <<interface>>
    + int applyStrategy(Bot bot, Bank bank)
}

class RandomStrategy {
    + RandomStrategy()
    + int applyStrategy(Bot bot, Bank bank)
}

%% ============================
%% RELATIONS
%% ============================

Session --> Deck : "uses"
Session --> Player : "contains 4"
Session --> Wonder : "uses"
Session --> Bank : "uses"
Deck --> Card : "contains *"
Player "1" --> "1" Wonder
Player "1" --> "7" Card : "contains"
Player --> Bank : "uses"
Player --> Player : "neighborhood"
Card --> Age : "has"
Card --> Color : "has"
Card --> Materials : "requires"
Card --> Cost : "has"
Card --> Effect : "has"
Cost <|.. CostGold : "implements"
Cost <|.. CostMaterials : "implements"
Cost <|.. CostGoldMaterials : "implements"
Effect --> EffectType : "type"
Wonder --> WonderStage : "contains *"
Wonder --> Side : "has"
WonderStage --> Cost : "requires"
WonderStage --> Effect : "gives"
Bot --|> Player
Bot ..|> Strategy
RandomStrategy ..|> Strategy

```