# Rapport du Sprint 7
**Période:** 01/12/2025 - 08/12/2025

## Objectifs du Sprint

Ce sprint avait pour but principal la refactorisation structurelle des classes `Session` et `Player`, ainsi que la finalisation des bots. L'objectif est de passer de stratégies statiques à des bots dynamiques et adaptatives, tout en assurant l'intégration des dernières inflexions demandées par le commanditaire.

## Gestion des Priorités et Contraintes

La charge de travail liée à la partie Business du projet s'est avérée beaucoup plus conséquente que prévu, impactant le rythme des intégrations techniques.

Une grande partie des fonctionnalités développées cette semaine se trouve encore en attente de merge - en Pull Request - et n'a pas été intégrée à la branche `develop`. Cette situation s'explique par la mobilisation de M. Boutrik, responsable de la gestion du dépôt et de la résolution des conflits, sur l'élaboration du Business Plan. Après discussion, l'équipe a décidé de poursuivre le développement sur des sous-branches distinctes pour ne pas ralentir la production, reportant la résolution des conflits et les merges à la semaine prochaine. En conséquence, les diagrammes de conception n'ont pas été mis à jour pour cette livraison.

Voici la liste des fonctionnalités qui ont pu être fusionnées (présentant peu de conflits) :

- Effect : `Action`

- Bot Dynamique : `BotMar`

- Strategy : `MinMax` et `Adaptative`

- Infléxion : Parallel Execution

- Fix : Calcul des points de victoire (Score Points)

Voici la liste des fonctionnalités présentant des merges conflits complexes nécessitant une intervention dédiée :

- Infléxion : Human Player

- Refactor SOLID / GRASP : `Card` et `Wonder`

## Inflexions

Conformément à ce qui a été établi dans le rapport précédent (`docs/sprint-6/REPORT.md`), nous avons choisi d'implémenter l'ensemble des inflexions demandées, à l'exception de [3] la variante Duel pour deux joueurs. Nous avons finalisé ces développements : [1] l'ajout de couleurs dans la console, [2] l'exécution parallèle des parties (via fichier de configuration TOML) et [4] l'intégration du joueur humain interactif sont désormais effectifs.

## Travail Réalisé

Cette semaine a marqué la finalisation des inflexions et le déploiement de certains bots avancés. Contrairement aux stratégies statiques précédentes, ces bots sont dynamiques et capables de modifier leur stratégie en temps réel selon l'état courant de la partie. Pour stimuler l'innovation, chaque membre de l'équipe devra concevoir sa propre logique de bot, l'objectif étant de les faire s'affronter pour évaluer la pertinence de chaque algorithme.

### Répartition des Tâches

- `M. Rahmouni`: A pris en charge l'inflexion concernant le Joueur Humain et a travaillé sur la refactorisation des classes `Wonder` et `Card` pour assurer la conformité aux principes SOLID et GRASP.

- `M. Ghilane el Hassani`: A développé le `BotMar`, un bot personnalisé doté d'une approche de changement de stratégie qu'il a lui-même élaborée.

- `M. Oumar`: A implémenté le dernier effet manquant du jeu, l'effet `Action` (partie des effets de Merveilles), et a créé la stratégie `Adaptative` basée sur ses propres règles.

- `M. Maniliuc`: A corrigé le calcul des points de score (les points `yellow` et `purple` étaient incorrectement comptabilisés comme `blue`) et a conçu la stratégie `MinMax` avec un règlement personnalisé.

- `M. Boutrik`: A assisté M. Landoulsi sur le Business Plan. Tandis que M. Landoulsi rédigeait le contenu, M. Boutrik a assuré la mise en forme du document selon les normes requises et a réalisé le fichier Excel financier incluant toute la trésorerie.

#### Rappel de l'organization de l'Équipe

M. Landoulsi est principalement responsable de la partie « Business », ce qui explique naturellement une quantité moindre de contributions directes au code dans le repositoire github.

M. Boutrik assure la gestion du développement, ce qui inclut la tenue du repositoire (versioning), les merges des Pull Requests, la résolution des conflits et les correctifs ponctuels sur les branches de fonctionnalité, ainsi que la rédaction du rapport hebdomadaire.

### Livrable de la Semaine

Comme expliqué précédemment, l'intégralité des Pull Requests n'a pas pu être fusionnée en raison de la priorité donnée au Business Plan. Par conséquent, les diagrammes de conception n'ont pas été mis à jour dans cette livraison. Le code disponible sur les branches `main` et `develop` contient uniquement les fonctionnalités listées comme fusionnées ci-dessus, le reste étant disponible sur les branches de fonctionnalités respectives.

### Prochaines Étapes

La prochaine étape consistera à terminer l'implémentation du reste des bots. Nous procéderons également au refactoring des classes `Player` et `Session` comme prévu, dès que les merge conflicts actuels seront résolus. Enfin, nous mettrons à jour les diagrammes et rédigerons le rapport final.
