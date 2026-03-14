# Rapport du Sprint 4
**Période:** 10/11/2025 - 14/11/2025

Avant de détailler ce sprint, il est à noter que la qualité des rapports de la semaine précédente était légèrement inférieure en raison de contraintes de temps. Notre priorité était de livrer une version fonctionnelle du jeu, incluant un bot aléatoire, pour le Rapport Intermédiaire. Cela a conduit à une rédaction plus précipitée et moins detaillée que prévu de ces documents.

## Objectifs du Sprint

L'objectif principal de ce quatrième sprint était d'étoffer significativement les mécaniques de jeu, en se concentrant sur l'implémentation des cartes commerciales (jaunes) et le système d'achat de ressources aux voisins. Il était également prévu d'intégrer la logique de comptage des points pour les cartes scientifiques (vertes) et d'adapter la boucle logique de jeu pour gérer l'Âge II.

## Inflexions

Une mention doit être faite concernant l’élément nommé INFLEXION, annoncé sur Moodle par l'Enseignant comme prévu pour cette semaine. N’ayant reçu aucune information complémentaire, nous avons supposé que cette activité serait décalée et qu’elle interviendrait la semaine prochaine.

## Travail Réalisé

À ce stade, le jeu est fonctionnel pour les Âges I et II, intégrant les cartes de ressources (brunes et grises), civiles (bleues), militaires (rouges) et scientifiques (vertes). Les mécaniques fondamentales sont en place, notamment la gestion complète des ressources, la possibilité cruciale d'acheter des ressources manquantes à ses voisins directs, la résolution des conflits militaires à la fin de chaque âge, et l'échange des mains entre joueurs à la fin de chaque tour (avec inversion du sens pour l'Âge II).

Le moteur de jeu est désormais exécutable via la commande Maven `mvn compile exec:java`. La classe `Main` gère le déroulement de la partie et affiche les informations pertinentes sur la sortie standard (stdout), permettant de suivre la progression et les actions des bots.

### Répartition des Tâches

Le travail de cette semaine s'est concentré sur l'enrichissement des mécaniques de jeu et la stabilisation du moteur.

- Bâtiments Scientifiques: M. Rahmouni a implémenté un test qui manquait pour la classe `Deserializer`, a développé la logique de comptage des points pour les bâtiments scientifiques (verts) et a mis à jour les diagrammes (UML). 

- Classe `Log`: M. Ghilane el Hassani a rédigé les tests pour les cartes vertes et a introduit une nouvelle classe `Log`. Cette classe enregistre les événements clés de la partie, comme le mélange du deck ou la carte jouée par un joueur, dans un fichier JSON.

- Système de paiement: M. Maniliuc a implémenté le système de paiement des ressources (brown et silver), ainsi que la fonctionnalité cruciale permettant à un joueur d'acheter des ressources à ses voisins directs s'il ne les possède pas.

- Bâtiments commerciaux: M. Oumar a débuté les travaux sur les effets des bâtiments commerciaux (jaunes) et la rédaction des tests pour le système de paiement. Ces deux tâches présentent une complexité élevée et dépendent de composants développés par d'autres membres, expliquant pourquoi elles ne sont pas encore finalisées.

- Age II: M. Landoulsi a modifié `Main` et `Session` pour intégrer l'Âge II, ce qui inclut des switch case pour gérer l'Âge et l'inversion du sens de distribution des mains (tradeHand). L'implémentation de l'Âge III n'est pas encore possible, car l'absence des cartes de guilde (violettes) ne permet pas de distribuer un nombre suffisant de cartes aux joueurs (e.g. 23 cartes pour 4 joueurs).

- Intégration continue: M. Boutrik a ajusté la configuration Maven pour que les tests d'intégration, qui n'étaient pas lancés par la commande `mvn verify`, soient désormais correctement exécutés. Il a également corrigé plusieurs tests qui échouaient. En complément, il a mis en place un nouveau workflow qui exécute la partie 50 fois de suite, permettant d'identifier et de corriger deux cas imprévus de Runtime Exception.

### Livrable de la Semaine

Comme à l'accoutumée, les diagrammes de conception ont été mis à jour et sont disponibles dans le dossier `docs/sprint-4/diagrams`. En suivant l'approche adoptée pour le rapport intermédiaire, nous avons produit deux versions du diagramme de classes: une version complète `class.md` et une version simplifiée `class_simple.md`, qui inclut toutes les classes mais omet les interfaces pour plus de clarté. De plus, deux diagrammes de classes spécifiques ont été créés pour `Card` et `Player`/`Bot`, détaillant les interfaces et interactions de ces deux composants centraux.

### Prochaines Étapes

Le "pré-sprint" de la semaine prochaine sera consacré à la finalisation des tâches en cours. L'objectif principal sera de terminer l'implémentation du comptage des points pour les cartes commerciales (jaunes) ainsi que les tests du système de paiement des ressources, tous deux commencés par M. Oumar. La stabilisation complète de ces fonctionnalités constituera une base solide pour aborder le Sprint 5, qui portera notamment sur le chaînage et les mécaniques de l’Âge III.
