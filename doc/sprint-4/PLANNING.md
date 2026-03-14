# Découpage du Projet en Sprints

### Semaine 0: Initialisation et Planification

**Objectif :*** mettre en place l'environnement github et définir le cadre du projet pour assurer un démarrage efficace.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 0.1 | En tant que membre de l'équipe, je veux mettre en place le dépôt GitHub avec les branches et les règles de contribution | 2 |
| 0.2 | En tant que membre de l'équipe, je veux étudier les règles complètes du jeu 7 Wonders | 5 |
| 0.3 | En tant que membre de l'équipe, je veux décomposer la vision du projet en sprints et User Stories | 8 |
| 0.4 | En tant que membre de l'équipe, je veux que les rôles et responsabilités soient clairement définis | 2 |
| **Total** | - | **17** |

### Sprint 1: Foundations du Moteur de Jeu

**Objectif :** construire le squelette du moteur de jeu avec les classes et la gestion de données initiales.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 1.1 | En tant que dévéloppeur, je veux créer les classes _Card_, _Wonder_, _Player_, _Deck_ et _Session_ pour modéliser le jeu | 8 |
| 1.2 | En tant que dévéloppeur, je veux une prèmiere version de la logique du jeu qui gère les tours et les phases d'un Âge | 3 |
| 1.3 | En tant que dévéloppeur, je veux que les données des cartes et des merveilles soient chargées depuis des fichier JSON | 5 |
| **Total** | - | 16 |

### Sprint 2: Actions du Joueur et Cartes de Base

**Objectif :** permettre au joueur d'interagir avec le jeu via les actions.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 2.1 | En tant que joueur, je veux pouvoir jouer une carte de ma main pour construire un bâtiment | 1 |
| 2.2 | En tant que joueur, je veux un système de gestion de pièces d'or (banque) | 3 |
| 2.3 | En tant que joueur, je veux pouvoir défausser une carte pour gagne 3 pièces d'or | 1 |
| 2.4 | En tant que joueur, je veux pouvoir utiliser une carte pour construire une étape de ma merveille | 8 |
| 2.5 | En tant que joueur, je veux que ma production de ressources (maron et grise) soit comptabilisée | 1 |
| 2.6 | En tant que joueur, je veux pouvoir construire des bâtiments civils (blue) pour gagne des points de victoire | 1 |
| **Total** | - | 15 |

### Sprints 3 : Conflicts Militaires et Premier Livrable Fonctionnel

**Objectif :** : introduire les conflits militaires, le comptage de points et produire une version minimale jouable de l'Âge I avec un bot aléatoire.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 3.1 | En tant que joueur, je veux avoir un voisin de gauche et de droite pour les interactions futures | 3 |
| 3.2 | En tant que joueur, je veux que les conflits militaires (rouge) soient résolus à la fin de l'Âge | 5 |
| 3.3 | En tant que joueur, je veux que mon score final soit calculé en fonction de mes points de victoire, militaires, etc | 3 |
| 3.4 | En tant que développeur, je veux définir un protocole de communication pour que le moteur puisse interagir avec un bot externe | 5 |
| 3.5 | En tant que développeur, je veux un bot basique qui choisit ses actions de forme aléatoire | 3 |
| Tâche | Rapport Intermédiaire | (21) |
| **Total** | - | **19** (+21) |

### Sprint 4 : Commerce, Science et Âge II

**Objectif :** toffer les stratégies possibles en ajoutant le commerce, la science, et en intégrant l'Âge II.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 4.1 | En tant que joueur, je veux pouvoir construire des bâtiments commerciaux (jaune) | 8 |
| 4.2 | En tant que joueur, je veux pouvoir acheter des ressources à mes voisins directs | 13 |
| 4.3 | En tant que joueur, je veux collecter des symboles via les bâtiments scientifiques (vert) pour marquer des points | 3 |
| 4.4 | En tant que joueur, je veux que le jeu se poursuive avec les cartes et les mécaniques de l'Âge II | 5 |
| **Total** | - | **29** |

### Sprint 5 : Chaînage et Âge III

**Objectif :** finaliser les mécaniques de jeu avec le chaînage, les guildes, l'Âge III et améliorer la qualité du code existant.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 5.1 | En tant que joueur, je veux pouvoir construire des bâtiments gratuitement grâce à la mécanique de chaînage | 5 |
| 5.2 | En tant que joueur, je veux pouvoir construire des guildes (violet) durant l'Âge III | 3 |
| 5.3 | En tant que joueur, je veux que le jeu intègre les cartes et les règles de l'Âge III | 5 |
| 5.4 | En tant que développeur, je veux relire et améliorer la qualité du code existant (refactoring) | 8 |
| **Total** | - | **21** |

### Sprint 6 : Intelligence Artificielle

**Objectif :** chaque membre doit développer son prôpre robot avec une stratégie unique.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 6.1 | En tant que développeur, je veux créer des bot "méta" qui analyse l'état du jeu pour prendre des décisions stratégiques | 13 |
| **Total** | - | **13** |

### Sprint 7 : Finalisation et Robustesse

**Objectif :** modifier l'architecture du projet pour la rendre plus robuste et modulaire.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 7.1 | En tant que développeur, je veux faire évoluer l'architecture monolithique vers un système de microservices (moteur/bots) | 21 |
| 7.2 | En tant qu'utilisateur, je veux que les textes du jeu puissent être traduits dans d'autres langues (i18n/l10n) | 2 |
| **Total** | - | **23** |

### Sprint 8 : Tests et Livraison Finale

**Objectif :** valider la qualité et la stabilité du jeu à travers des tests utilisateurs et préparer le code final pour la livraison.

| ID | User Story | Story Points |
|:--:|:-----------|:-------------|
| 8.1 | En tant que testeur, je veux analyser des sessions de bêta-test pour identifier et remonter les derniers bugs et suggestions | 3 |
| Tâche | Préparation du livrable et rédaction du Rapport final | (21) |
| **Total** | - | **3** (+21) |
