# Rapport du Sprint 3
**Période:** 03/11/2025 - 07/11/2025

## Objectifs du Sprint

L'objectif principal de ce troisième sprint était double. Premièrement, finaliser une première version fonctionnelle du moteur de jeu (US 3.5), capable de faire jouer un bot de manière autonome sur un Âge complet. Deuxièmement, rédiger le Rapport Intermédiaire exigé pour le projet.

Les fonctionnalités prévues (US) pour atteindre cet objectif incluaient l'ajout du système de voisinage (US 3.1) essentiel pour les interactions, l'implémentation de la résolution des conflits militaires en fin d'Âge (US 3.2), et le calcul du score final (US 3.3).

## Travail Réalisé

Un effort significatif a été consacré à l'amélioration de la qualité et de la lisibilité du code avec l'ajout de la documentation Javadoc à l'ensemble des classes et méthodes publiques existantes.

Pour gérer la construction des merveilles (une tâche reportée du sprint précédent), une nouvelle classe WonderStage a été créée, simplifiant la logique de construction d'étapes.

Enfin, les classes Effect, Player et Session ont été modifiées en profondeur pour intégrer le nouveau système de voisinage et la mécanique de gestion des conflits (calcul des points d'attaque et de défense). La classe Main a également été développée pour servir de point d'entrée et lancer la boucle de jeu principale, qui délègue la logique à Session.

### Repartition des Tâches

Classe `WonderStage`: Implémentation réalisée par M. Diallo et M. Maniliuc.

Développement du Bot (Stratégie Aléatoire): M. Ghilane et M. Rahmouni. Le design pattern 'Strategy' a été utilisé pour assurer une meilleure modularité et faciliter l'ajout de futurs bots.

Documentation (Javadoc): Tâche collective réalisée par l'ensemble de l'équipe.

Système de Conflits et Voisinage: M. Ghilane.

Calcul du Score Final et Main: M. Maniliuc et M. Boutrik. Le point d'entrée (Main) exécute désormais la boucle principale du jeu.

### Livrable de la Semaine

Le livrable de ce sprint est une étape majeure: il s'agit de la première version du code fonctionnelle, capable d'exécuter une partie complète (Âge I) jouée par un bot.

Comme à l'accoutumée, les diagrammes UML mis à jour sont disponibles sous `doc/sprint-3/diagrams`. Ce sprint incluait également la rédaction du Rapport Intermédiaire. Conformément aux exigences de ce rapport, nous avons ajouté un diagramme d'activité global, ainsi que des diagrammes de séquence et de cas d'utilisation (use case) détaillant les trois actions principales du jeu (jouer une carte, défausser, construire une merveille).

### Prochaines Étapes

Le Sprint 4 se concentrera sur l'enrichissement des mécaniques de jeu. Les objectifs principaux seront d'implémenter le système de commerce avec les voisins (bâtiments jaunes et achat de ressources), la logique des bâtiments scientifiques (cartes vertes) et l'intégration des cartes de l'Âge II.
