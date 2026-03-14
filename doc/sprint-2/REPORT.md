# Rapport du Sprint 2
**Période:** 20/10/2025 - 24/10/2025

## Objectifs du Sprint

L'objectif principal de ce deuxième sprint était d'implémenter les actions fondamentales du joueur, qui lui permettront (une fois l'ensemble des classes connectées via la fonction main) d'interagir concrètement avec le moteur du jeu. Les users stories (US) planifiées visaient à permettre au joueur de construire un bâtiment, de défausser une carte, et d'utiliser une carte pour construire une étape de sa merveille. Il était également prévu de mettre en place un système de gestion de pièces d'or, de comptabiliser la production de ressources brutes et de gérer la construction des bâtiments civils pour les points de victoire.

## Travail Réalisé

### Nouvelles Classes

Pour soutenir les nouvelles fonctionnalités, deux nouvelles classes importantes ont été introduites dans notre codebase: la classe `Bank` et `Effect`. `Bank` a été créée pour centraliser et gérer toute logique liée aux pièces d'or. Parallèlement, la classe `Effect` a été implémentée pour héberger une liste structurée de tous les effets possibles pouvant être déclenchés par les cartes ou les étapes de merveilles. Les diagrammes de conception (UML) ont également été mis à jour pour refléter ces ajouts.

### Repartition des Tâches

- Classe `Bank`: l'implemémentation a été conjointement réalisée par Maniluc et Diallo, tandis que les tests ont été rédigée par Landoulsi.
- Classe `Effect`: le développement a été assuré par Maniluc et Ghilane el Hassani, avec Diallo comme responsable de la couverture de tests.
- Classe `Session`: des modifications ont été apportées par Rahmouni pour la logique de choix de merveille, la construction des bâtiments et la défausse de cartes). Le code a été revu et amélioré par Maniluc, et les tests ont été implémentés par Ghilane el Hassani.
- Classe `Player`: la classe a été adaptée par Landoulsi pour s'aligner sur les nouvelles modifications de `Session`. Les tests, écrits par Ghilane el Hassani, ont aussi été mis à jour par Rahmouni.
- Classe `Card`: le test existant a été mis à jour par Boutrik.
- Tâches "Transversales": le pipeline a été géré par Boutrik, ainsi que les revue de code et la gestion des pull requests pour résoudre les merge conflicts. L'intégration de JaCoCo a été suggérée et réalisée par Ghilane el Hassani.
- Classe `Wonder`: le développement de upgrade des merveilles est en cours par Diallo. La conception d'une nouvelle classe `WonderStage` est en discussion et sera finalisée lors du prochain pré-sprint.
- Diagrammes UML: ils ont été mis à jour par Rahmouni, avec l'appui ponctuel de Boutrik et Maniluc.

### Contrainte Inattendue et Réajustement

Ce sprint a été marqué par un événement soudain et imprévu. En effet, nous avons reçu une notification le jour-même nous informant, sans préavis, que la partie « business » du projet débutait cette semaine-là. En conséquence, le temps alloué au développement a été réduit de pratiquement la moitié. Malgré cette contrainte, le groupe a réussi à s'adapter et à livrer la quasi-totalité des objectifs fixés, ne reportant que la fonctionnalité de construction de merveille.

### Workflow et Intégration Continue (CD/CI)

Pour améliorer la stabilité du code, nous avons mis en place un pipeline d'intégration continue (CD/CI) robuste en utilisant Github Actions. Ces workflows automatisent nos processus de test: les tests unitaires (UTs) sont exécutés automatiquement à chaque _push_ sur une branche `feat|fix|refactor|chore`, et les tests d'intégration (ITs) sont déclenchés lors de chaque creátion de _pull request_ ciblant les branches `main` ou `develop`.

En complément de ce pipeline, nous avons intégré *JaCoCo* comme une nouvelle dépendance Maven. Cet outil génère automatiquement un rapport de couverture de tests. Il nous fournit des métriques précieuses en mesurant la couverture des instructions (méthodes, fonctions, [...]) ainsi que la couverture des branches (conditions if, switch), nous aidant à identifier les parties du code qui nécessitent des tests supplémentaires.

### Modèle de Branches

Nous avons également eu une discussion stratégique concernant notre modèle de branches. Il a été décidé de conserver notre système actuel (`main`, `develop` et `feat|fix|refactor|chore`) et de ne pas adopter une branche `release`. Cette décision vise à maintenir un processus de développement simple et efficace. Nous avons voulu éviter la complication qui pourrait survenir si, par exemple, des tests unitaires passaient lors d'une _pull request_ vers `develop`, mais que des tests d'intégration échouaient ensuite lors d'un _pull request_ vers `release`. Gérer les corrections dans un tel scénario a été jugé trop complexe et inutile. Nous confirmons donc notre approche: les tests unitaires (UTs) sont liés aux _pushes_ de branches de fonctionnalités et les tests d'intégration sont validés lors des _pull request_.

### Livrable de la Semaine

Le livrable de cette semaine est la continuation directe du développement du sprint précédent. Il comprend les nouvelles classes `Bank` et `Effect` ainsi que la mise en place d'une "suite" de tests complète pour le codebase, incluant l'intégration de JaCoCo. Les diagrammes de conception (UML) ont été mis à jour pour refléter ces ajouts et sont disponible sous `doc/sprint-2/diagrams`. Bien que la fonction `main` principale affiche encore "Hello World", un travail de conception et d'implémentation substantiel a été réalisé. La logique de base pour la gestion des cartes civiles (bleues), de matières premières et manufacturées (marrons et grises) est désormais intégrée au code et testée.

### Prochaines Étapes

Avant le début officiel du Sprint 3, un « pré-sprint » sera dédié à la finalisation des tâches reportées. L'objectif sera de terminer l'implémentation de la construction des étapes de merveilles et d'effectuer une mise à jour nécessaire sur la classe `Deserializer`. Ensuite, le Sprint 3 se concentrera sur l'introduction du système de conflits militaires (cartes rouges) et la création d'un premier livrable fonctionnel. L'objectif sera d'avoir un "bot" très simple capable de jouer une partie de base de manière autonome.
