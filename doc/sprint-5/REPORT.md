# Rapport du Sprint 5
**Période:** 17/11/2025 - 21/11/2025

## Objectifs du Sprint

L'objectif de ce sprint était de finaliser les mécaniques du jeu en implémentant le chaînage et les guildes (cartes violletes), et de commencer l'amélioration de la qualité du codebase.

## Inflexions

Une mention est nécessaire concernant "INFLEXION" annoncée sur Moodle par l'Enseignant, initialement prévu pour la semaine précédente et reportée à cette semaine. Aucune information complémentaire n'ayant été reçue, nous avouns conclu que cette activité n'aurait finalement pas lieu.

## Travail Réalisé

Nous avons initié une discussion approfondie sur l'état actuel du codebase au regard des principes SOLID et GRASP. Nous avons notamment identifié que les classes `Player` et `Session` en particulier présentent une quantité excessive de responsabilités, justifiant un effort de refactorisation. Une stratégie a été établie pour restructurer progressivement l'intégralité du code sur les trois prochaines semaines, précédant la livraison finale. Il est à noter que ce processus de refactorisation entraînera temporairement une légère diminution de la couverture des tests.

Du côté du développement des fonctionnalités, nous avons réussi à implémenter les cartes violettes et la mécanique de chaînage, en plus d'effectuer des correctifs pour plusieurs bugs existants.

### Répartition des Tâches

Le travail s'est concentré sur la finalisation des fonctionnalités de jeu et sur les premières réflexions architecturales.

- `chaînage`: la mécanique de chaînage a été implémentée par M. Landoulsi, bénéficiant du soutien actif de M. Maniliuc.

- `PurpleEffect`: M. Rahmouni a intégré les cartes violettes (guildes) et a mis à jour les diagrammes de classes pour refléter les récentes modifications apportées à la classe `Effect`. M. Ghilane el Hassani a pris en charge la mise à jour des tests suite à ces changements.

- `Log`: M. Ghilane el Hassani a également amélioré la classe `Log` pour générer un fichier JSON plus organisé et lisible, ce qui a mené à l'adoption d'un nouveau système d'enregistrement où les « journaux » de chaque partie sont stockés individuellement au format JSON (`<timestamp>.log`) dans un nouveau dossier `log/`, remplaçant le fichier unique `app.log.jsonl` à la racine.

- `Effect`: le premier effort de refactorisation basé sur les principes SOLID/GRASP a été réalisé par M. Maniliuc pour la classe `Effect`, optant pour l'utilisation d'interfaces plutôt que des structures switch case complexes. M. Maniliuc a également réalisé un travail notable en corrigeant un bug critique dans le système d'achat aux voisins, où le joueur effectuant l'achat payait bien le coût des ressources, mais le voisin concerné ne recevait pas le paiement correspondant.

- `Yellow`: M. Oumar a tenté d'implémenter les effets des cartes commerciales (jaunes) dans sa branche `feat/yellow`, mais cette tâche s'est montré beaucoup plus complexe que prévu, ce qui justifie son report au sprint de la semaine prochaine.

- `Bank`: M. Boutrik a corrigé un bug dans la classe `Bank` où la conversion de l'argent du joueur lors d'un dépôt ou d'un échange était incorrecte; cette correction a été effectuée lors de la résolution de merge conflits et des ajustements sur les fichiers de tests.

#### Rappel de l'organization de l'Équipe

M. Landoulsi est principalement responsable de la partie « Business », ce qui explique naturellement une quantité moindre de contributions directes au code dans le repositoire github.

M. Boutrik assure la gestion du développement, ce qui inclut la tenue du repositoire (versioning), la rédaction des issues, les merges des Pull Requests, la résolution des conflits et les correctifs ponctuels sur les branches de fonctionnalité, ainsi que la rédaction du rapport hebdomadaire.

### Livrable de la Semaine

Comme à l'accoutumée, les diagrammes de conception ont été mis à jour et sont disponibles dans `docs/sprint-5/diagrams`. Le jeu est désormais presque complet, seules les cartes commerciales (jaunes) — dont l'implémentation est plus difficile que prévu — restent à intégrer et sont, comme la semaine précédente, reportées au prochain sprint.

### Prochaines Étapes

Le pré-sprint de la semaine prochaine sera dédié à la correction d'un bug spécifique affectant le système des ressources optionnelles (Issue #89). Par la suite, le sprint principal sera consacré à l'implémentation de différentes stratégies de bots, comme spécifié dans `PLANNING.md`, tout en poursuivant la refactorisation progressive du code pour atteindre une meilleure conformité aux principes SOLID et GRASP.
