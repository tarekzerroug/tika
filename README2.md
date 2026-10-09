# Tache 2 - Tests unitaires et analyse de mutation

## Binome

- Nom 1 : a completer
- Nom 2 : a completer

## 1. Selection des classes a tester

Les classes retenues appartiennent toutes au module autorise `tika-core`, dans le package Java `org.apache.tika.mime`. Localement, ce package correspond au repertoire suivant :

```text
tika-core/src/main/java/org/apache/tika/mime/
```

La couverture initiale a ete mesuree avec JaCoCo en executant les tests originaux du module `tika-core` :

```bash
mvn -pl tika-core -am -Dspotless.apply.skip=true -Dspotless.check.skip=true test
```

Le rapport HTML utilise pour cette selection est :

```text
tika-core/target/site/jacoco/org.apache.tika.mime/index.html
```

### Classes choisies

Les valeurs ci-dessous sont recopiees depuis le rapport HTML JaCoCo, page `org.apache.tika.mime`. La colonne `Cov. instructions` correspond au premier `Cov.` affiche par JaCoCo, et la colonne `Cov. branches` correspond au deuxieme `Cov.`.

| Classe | Fichier | Tests originaux existants | Cov. instructions | Cov. branches | Lignes manquees / lignes totales | Methodes manquees / methodes totales | Justification |
| --- | --- | --- | ---: | ---: | ---: | ---: | --- |
| `MediaType` | `tika-core/src/main/java/org/apache/tika/mime/MediaType.java` | `MediaTypeTest` | 79 % | 74 % | 30 / 156 | 6 / 28 | Classe publique centrale pour la creation, la normalisation et l'analyse des types MIME. Elle est deja testee, mais le rapport HTML indique encore des lignes, branches et methodes non couvertes. |
| `Patterns` | `tika-core/src/main/java/org/apache/tika/mime/Patterns.java` | `PatternsTest` | 84 % | 65 % | 6 / 79 | 0 / 8 | Classe responsable de l'association entre patrons de noms de fichiers et types MIME. Elle est deja testee, mais le rapport HTML indique encore des lignes et branches non couvertes. |
| `MimeType` | `tika-core/src/main/java/org/apache/tika/mime/MimeType.java` | `MimeTypesReaderTest`, `MimeDetectionTest`, `PatternsTest` | 62 % | 35 % | 29 / 103 | 5 / 30 | Classe representant un type MIME complet. Elle est deja exercee indirectement par plusieurs tests du package MIME, mais le rapport HTML indique encore une couverture de branches faible. |

### Methodes et comportements vises

Pour `MediaType`, les tests generes devront viser principalement :

- `parse(String)` : chaines valides, invalides, avec espaces, avec parametres et avec `charset` place avant le type MIME.
- `getBaseType()`, `getType()`, `getSubtype()`, `hasParameters()` et `getParameters()` : verification de la decomposition du type MIME.
- Les constructeurs avec parametres : normalisation des cles, tri des parametres et echappement des valeurs speciales.
- `equals`, `hashCode` et `compareTo` : coherence entre egalite, representation textuelle et ordre naturel.

Pour `Patterns`, les tests generes devront viser principalement :

- `add(String, MimeType)` et `add(String, boolean, MimeType)` : ajout de noms exacts, extensions, glob patterns et expressions regulieres Java.
- `matches(String)` : priorite entre nom exact, extension la plus longue et glob pattern.
- Les cas d'erreur : patron nul, type MIME nul, nom nul et conflits entre patrons associes a des types incompatibles.

Pour `MimeType`, les tests generes devront viser principalement :

- `isValid(String)` : noms valides, noms sans slash, slash au debut ou a la fin, caracteres interdits et caracteres non ASCII.
- `getExtension()` et `getExtensions()` : absence d'extension, extension unique, plusieurs extensions et preservation de l'ordre.
- `setDescription`, `setAcronym`, `setUniformTypeIdentifier` et `addLink` : refus des valeurs nulles et conservation des valeurs valides.
- `matchesMagic(byte[])`, `matches(byte[])`, `hasMagic()` et `matchesXML(...)` : comportements vrais/faux selon les donnees configurees.



### Verification des mutants vivants

La presence de mutants vivants a ete verifiee avec PIT sur les tests originaux. Les trois classes ont des mutants vivants, ce qui confirme qu'elles sont pertinentes pour la generation de tests supplementaires.

## 2. Analyse PIT avec les tests originaux

PIT a ete ajoute au module `tika-core` avec le plugin Maven `org.pitest:pitest-maven` et le plugin JUnit 5 `org.pitest:pitest-junit5-plugin`.

Commande executee depuis `tika-core` :

```bash
mvn -Dspotless.apply.skip=true -Dspotless.check.skip=true -Dcheckstyle.skip=true -DskipITs=true test-compile org.pitest:pitest-maven:mutationCoverage
```

Rapport genere :

```text
tika-core/target/pit-reports/index.html
```

Resultat global pour les classes selectionnees :

| Nombre de classes | Couverture de lignes | Couverture de mutation | Test strength |
| ---: | ---: | ---: | ---: |
| 3 | 80 % (295 / 368) | 63 % (147 / 235) | 89 % (147 / 165) |

Resultat par classe :

| Classe | Couverture de mutation | Mutants generes | Mutants tues | Mutants vivants | Mutants sans couverture | Justification |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| `MediaType` | 78 % (66 / 85) | 85 | 66 | 3 | 16 | PIT confirme que les tests originaux ne detectent pas tous les changements artificiels. Les mutants vivants touchent notamment `parse`, `isSimpleName` et `union`. |
| `Patterns` | 65 % (31 / 48) | 48 | 31 | 10 | 7 | PIT confirme plusieurs mutants vivants dans la gestion des patrons, notamment `add`, `matches`, `compile` et `LengthComparator.compare`. |
| `MimeType` | 49 % (50 / 102) | 102 | 50 | 5 | 47 | PIT confirme des mutants vivants dans des comportements comme `hasRootXML`, `hasMagic` et `compareTo`. Plusieurs mutants sont aussi sans couverture, surtout dans `isValid` et `matchesMagic`. |

Exemples de mutants vivants observes :

- `MediaType.parse`, ligne 260 : mutation arithmetique survivante.
- `MediaType.isSimpleName`, ligne 293 : retour booleen force a `false` survivant.
- `Patterns.matches`, ligne 153 : mutation de borne conditionnelle survivante.
- `Patterns.compile`, ligne 179 : negation de condition survivante.
- `MimeType.hasRootXML`, ligne 268 : plusieurs mutations de retour booleen survivantes.
- `MimeType.hasMagic`, ligne 294 : retour booleen force a `true` survivant.

Ces resultats montrent que les tests originaux sont insuffisants pour tuer tous les mutants. Les classes choisies satisfont donc le critere : elles ne sont pas totalement couvertes par JaCoCo et elles possedent des mutants vivants dans le rapport PIT initial.
