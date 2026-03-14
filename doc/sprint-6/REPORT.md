# Rapport du Sprint 6
**Période:** 24/11/2025 - 28/11/2025

## Objectifs du Sprint

L'objectif central était de doter le jeu de stratégies autonomes, chaque membre de l'équipe devant implémenter une logique décisionnelle distincte. En outre, le sprint visait à poser les bases de bots « méta », capables d'analyser dynamiquement l'état de la partie pour adapter leur comportement, conformément à la vision établie dans notre planification `PLANNING.md`.

## Inflexions

Le commanditaire a demandé l'intégration de quatre nouvelles évolutions classées par difficulté, incluant: [1] l'ajout de couleurs dans la console, [2] l'exécution de parties en parallèle, [3] une variante pour deux joueurs (Duel) et [4] l'ajout d'un joueur humain. Après avoir évalué la charge de travail restante et le délai de deux semaines avant le rapport final, nous avons décidé d'implémenter [1], [2] et [4]. Nous avons choisi d'écarter la variante Duel, car elle nécessiterait des modifications trop profondes du code actuel pour être réalisée dans les délais impartis.

## Travail Réalisé

Durant cette semaine, nous avons finalisé l'implémentation des cartes, ce qui inclut les cartes commerciales (jaunes) dont le développement s'est étalé sur plusieurs semaines. En prévision des inflexions demandées, nous avons également mis en place la gestion des arguments en ligne de commande (CLI) ainsi qu'une configuration via un fichier TOML, ce qui facilitera l'intégration des fonctionnalités futures.

Plusieurs stratégies de `Bot` ont été développées, notamment les stratégies `Military`, `Economic`, `Blue`, `Science`, `AnyResource` et `Wonder` (hush). Pour l'instant, ces bots restent statiques, choisissant une stratégie unique pour toute la partie. Le développement du bot dynamique, capable de changer de stratégie selon l'état du jeu, débutera la semaine prochaine.

Parallèlement au développement des fonctionnalités, nous poursuivons nos efforts de refactorisation pour assurer la conformité du code aux principes SOLID et GRASP. Cette semaine, le travail s'est concentré sur les classes `Card` et `Wonder`. Cependant, ces modifications n'ont pas été intégrées pour cette livraison. En raison du rythme soutenu de développement des fonctionnalités durant ce sprint, les branches de refactorisation ont accumulé un retard conséquent par rapport à la branche `develop`, entraînant des merge conflicts complexes que l'interface web de GitHub ne permet pas de résoudre. M. Boutrik a été désigné pour résoudre ces conflits techniques jusqu' pré-sprint de la semaine prochaine. Il est important de noter que ce processus de refactorisation entraîne temporairement une légère diminution de la couverture des tests, le temps que l'ensemble du système soit stabilisé.

### Répartition des Tâches

Le travail a été réparti de manière à couvrir à la fois les nouvelles stratégies de jeu et l'amélioration structurelle du projet:

- `M. Rahmouni`: a implementé la stratégie `Science` (green) ainsi que les tests pour la stratégie `Military` (red). Il a également pris en charge la refactorisation des classes `Card` et `Wonder` pour les rendre conformes aux principes SOLID/GRASP, tout en assistant M. Landoulsi sur la partie Business du projet.

- `M. Ghilane el Hassani`: a développé la stratégie `Military` (red) et a rédige les tests correspondant à la stratégie `Science` (green).

- `M. Dialo`: a finalisé l'implémentation des cartes `Yellow`, une tâche complexe qui a nécessité près de trois semaines de travail. Il a également implémenté la stratégie `Economic` (gold) et a corrigé un bug concernant les cartes `Purple`.

- `M. Maniliuc`: a mis en place les arguments de ligne de commande et la configuration TOML. Il a également implémenté la stratégie basée sur les merveilles `Wonder` (hush).

- `M. Boutrik`: a implémenté les stratégies `Blue` et `AnyResource`, tout en apportant son aide sur la partie Business, spécifiquement sur les aspects financiers.


#### Rappel de l'organization de l'Équipe

M. Landoulsi est principalement responsable de la partie « Business », ce qui explique naturellement une quantité moindre de contributions directes au code dans le repositoire github.

M. Boutrik assure la gestion du développement, ce qui inclut la tenue du repositoire (versioning), la rédaction des issues, les merges des Pull Requests, la résolution des conflits et les correctifs ponctuels sur les branches de fonctionnalité, ainsi que la rédaction du rapport hebdomadaire.

### Livrable de la Semaine

Comme à l'accoutumée, les diagrammes de conception ont été mis à jour et sont disponibles dans `docs/sprint-6/diagrams`. Le jeu est désormais presque complet ; seul un bug concernant les ressources optionnelles reste à corriger (Issue #89).

### Prochaines Étapes

Le pré-sprint de la semaine prochaine sera dédié à la correction d'un bug spécifique affectant le système des ressources optionnelles (Issue #89). Par la suite, le sprint sera consacré à la refactorisation complète du code, incluant les classes restantes `Session` et `Player`, pour atteindre une conformité totale aux principes SOLID et GRASP, ainsi qu'à la finalisation du développement des bots dynamiques.
