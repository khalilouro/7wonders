# Rapport du Sprint 1
**Période:** 13/10/2025 - 17/10/2025

## Objectifs du Sprint

L'objectif principal de ce premier sprint était de poser les bases techniques de notre moteur de jeu. Le travail s'est concentré sur la mise en place du squelette structurel de l'application à travers de la création des classes de base `Card`, `Wonder`, `Player`, `Deck` et `Session`. Parallèlement, nous visions à implémenter une première version de la logique de jeu pour la gestion des tours et des phases, et à permettre le chargement des données initiales des cartes et merveilles à partir de fichiers JSON.

## Travail Réalisé

### Modélisation et Classes de Base

Une avance a été faite sur la modélisation du jeu avec l'implémentation d'une première version simple pour chaque classe requise. Pour assurer la robustesse de notre base de code, les tests unitaires pour les classes les plus fondamentales, `Card` et `Wonder`, ont été implementé. La gestion de l'état du jeu, incluant la logique des tours et des phases, a été centralisée au sein de la classe `Session`, qui agira comme le principal orchestrateur de la partie.

### Gestion des Données (JSON)

Concernant le chargement des données, la logique a été encapsulée dans une nouvelle classe `Deserializer`. Un Pull Request pour intégrer cette fonctionnalité est actuellement en cours de discussion et révision. En préparation, les fichiers de données `cards.json` et `wonders.json` ont été créés et sont déjà presque complets.

### Qualité du Code et Formatation

En plus des tâches de développement prévues, une nouvelle User Story a été absorvé par le sprint pour standardiser le style du code à travers le projet. Cette initiative, visant à garantir la cohérence et la lisibilité du code, est actuellement en discussion via un Pull Request dédié.

### Conception et Diagrammes UML

Il est à noter qu'une part importante du temps de ce sprint a été allouée à la conception et à la modélisation UML. Cet investissement dans les diagrammes, créés avec Mermaid et disponibles en `doc/sprint-1/diagrams`, on été jugé crucial pour aligner la vision de l'équipe et garantir une base architecturale solide avant de poursuivre plus l'implémentation.

## Répartition des Tâches

Pour chaque tâche de développement, nous avons adopté une méthodologie de travail collaboratif visant à assurer la qualité du code. Un membre est désigné comme responsable du développement principal (implémentation de la classe), un autre est responsable de la création des tests unitaires correspondants, et un ou deux autres membres sont chargés de la revue de code. Ce processus garantit que chaque Pull Request est examiné, testé et validé avant sa fusion (_merge_).

Voici un résumé des responsabilités de chaque membre de l'équipe pour les différentes tâches de ce sprint :

| Tâche | Responsable Développement | Responsable Tests | Responsable Revue de Code |
|:-:|:-|:-|:-|
| `Card` | Ghilane El Hassani | Boutrik | Rahmouni, Diallo |
| `Wonder` | Diallo | Landoulsi | Ghilane El Hassani |
| `Deck` | Boutrik | Diallo* | Landoulsi |
| `Player` | Landoulsi | Ghilane El Hassani* | Rahmouni |
| `Session` | Rahmouni | Maniluc* | Boutrik |
| `Deserializer` | Maniluc | Boutrik* | (pas défini) |

\* tâches que nous n'avons pas eu le temps de réaliser. Seuls les tests pour les classes `Card` et `Wonder` ont été implémentés.

En parallèle, la conception architecturale et la création des diagrammes UML avec Mermaid ont été menées par M. Rahmouni et M. Landoulsi.

## Modèle de Branches

Pour la gestion de notre code source, nous avons adopté un flux de travail structuré. La branche `main` est exclusivement réservée aux versions taguées (livrables). Le développement actif a lieu dans des branches dédiées en suivant le standard [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) pour le noms de branches (feat/..., fix/...) qui sont ensuite fusionnées dans la branche `develop`. Cette dernière sert de branche d'intégration principale. À la fin de chaque sprint hebdomadaire, `develop` est fusionnée dans `main` pour constituer le livrable officiel de la semaine.

## Livrable de la Semaine

Le livrable pour ce premier sprint constitue une fondation solide pour la suite du projet. Il se compose des diagrammes UML de conception (diagramme de classe et use case), d'une première version du code contenant les brouillons des classes de base, et des tests unitaires validant le comportement des classes `Card` et `Wonder`. Il est à noter que les fonctionnalités présentes dans les Pull Requests encore ouverts (e.g. classe `Deserializer`) ne font pas partie de ce livrable (qui est disponible sur la branche `main` avec le tag `sprint-1`). Les Tests peuvent être executé par la commande maven:

```bash
$ mvn test
```

## Prochaines Étapes

Une réunion se tiendra le 22, mercredi matin, avant le début du Sprint 2 (qui commencera l'après-midi). L'objectif sera de discuter des possibles mises à jour du fichier `PLANNING.md`, d'ajuster les User Stories si nécessaire, finaliser les Pull Request actuellement ouverts et de définir la répartition des tâches pour le prochain sprint en suivant le même modèle que pour le Sprint 1.
