# 7 Wonders

<img align="right" width="100px" src="./assets/logo.jpg">

![Version Badge](https://img.shields.io/badge/version-v0.0.0+build-red)

Ce projet est une implémentation numérique du jeu de société [7 Wonders](https://www.rprod.com/fr/games/7-wonders), réalisé dans le cadre de la troisième année de Licence MIAGE à l'Université Côte d'Azur. L'implémentation se fera en Java, avec des détails techniques et une architecture à définir selon les directives du professeur.

Le projet vise à créer une simulation non-interactive du jeu, entièrement pilotée par des robots. Le développement se concentrera sur des parties sans intervention humaine, avec un affichage exclusivement en mode texte. Le cœur du travail consistera à modéliser les composants du jeu, à construire un moteur de règles complet, et à développer plusieurs types de bots, d'un simple IA jouant au hasard à des stratégies plus avancées. La priorité sera donnée à une configuration incluant deux bots intelligents et un bot aléatoire.

L'application finale devra proposer deux modes d'exécution via [Maven](https://maven.apache.org/). Le premier mode, `une`, lancera une seule partie avec un déroulé détaillé pour l'analyse. Le second, `cinqcents`, simulera 500 parties en silence pour générer un rapport statistique sur les performances des IA, incluant les victoires et les scores moyens.

## Utilisation

Clonez le dépôt sur votre machine:

```bash
$ git clone git@github.com:UCA-DS4H-MIAGE-L3/projet7wonders-groupe-a.git
$ cd projet7wonders-groupe-a
```

Le projet peut être compilé et exécuté directement avec Maven en utilisant les profiles pré-configurés:

```bash
$ mvn compile exec:java -P une
ou
$ mvn compile exec:java -P cinqcents
```

Si vous souhaitez compiler le projet et créer un fichier `.jar` exécutable:

```bash
$ mvn clean package
```

Le fichier JAR sera généré dans `target/`.

## Contribution

Afin d'assurer la clarté et la cohérance de l'historique du projet, nous allons suivre des règles de contribution précises.

- **commits:** L'utilisation de la spécification [Conventional Commits 1.0.0](https://www.conventionalcommits.org/en/v1.0.0/) est obligatoire pour tous les commits.

- **schéma de versionnement:** Le projet suivra le [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

- **modèle de branches**: Nous proposons d'adopter le modèle de branches « [A Successful Git Branching Model](https://nvie.com/posts/a-successful-git-branching-model/) », aussi connu sous le nom de Git Flow. Cette approche est actuellement en discussion et son adoption n'est pas encore définitive.

## Structure du projet

Le projet suit l'organisation standard des répertoires Maven:

```
.
├── assets
│   └── Logo.jpg
├── AUTHORS
├── LICENSE
├── pom.xml
├── README.md
├── data
│   └── [...]
├── doc
│   └── [...]
└── src
    ├── main
    │   ├── java/fr/uca/miage/sevenwonders
    │   │   ├── Main.java
    │   │   └── [...]
    │   └── resources
    └── test
        └── java/fr/uca/miage/sevenwonders
            └── [...]
```

## LICENSE

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT). Feel free to use, modify, and distribute the code as needed. See the [LICENSE](LICENSE) file for more information.

## Copyright Disclaimer

This project is an unofficial, non-commercial implementation created for academic purposes. It is not affiliated with, sponsored, or endorsed by Repos Production or Asmodee.

All trademarks and copyrights for the "7 Wonders" board game, including its name, artwork, and game mechanics, are the property of their respective owners.
