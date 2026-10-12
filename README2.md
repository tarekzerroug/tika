# Tâche 2 — Génération et amélioration de tests avec ChatUniTest

## 1. Classe choisie

La classe choisie est :

```text
org.apache.tika.mime.Patterns
```

Elle possède déjà des tests (`PatternsTest`), mais sa couverture initiale n'est pas complète.

### Couverture JaCoCo initiale

| Instructions | Branches | Lignes manquées |
| ---: | ---: | ---: |
| 84 % | 65 % | 6 / 79 |

Les méthodes principalement visées sont `add(...)`, `matches(String)`, `compile(String)` et `LengthComparator.compare(...)`.

### PIT initial

Commande :

```bash
mvn -Dspotless.apply.skip=true -Dspotless.check.skip=true -Dcheckstyle.skip=true -DskipITs=true test-compile org.pitest:pitest-maven:mutationCoverage
```

Résultat :

| Mutants | Tués | Survivants | Sans couverture | Mutation coverage |
| ---: | ---: | ---: | ---: | ---: |
| 48 | 31 | 10 | 7 | 65 % |

La classe possède donc une couverture incomplète et des mutants vivants, ce qui justifie son choix.

---

## 2. Intégration de ChatUniTest

ChatUniTest `2.1.1` a été ajouté au `pom.xml` de `tika-core`.

Le modèle utilisé est `code-llama`, exécuté localement avec Ollama :

```xml
<properties>
    <chatunitest.model>code-llama</chatunitest.model>
    <chatunitest.url>http://localhost:11434/v1/chat/completions</chatunitest.url>
</properties>
```

Génération des tests :

```bash
mvn chatunitest:class -DselectClass=org.apache.tika.mime.Patterns
```

ChatUniTest termine correctement la génération :

```text
[CHATUNITEST] Generation finished
[CHATUNITEST] Test processed
[INFO] BUILD SUCCESS
```

Les sorties brutes sont conservées dans :

```text
chatunitest-generated/Patterns/
```

---

## 3. Tests générés et critique

Les tests générés automatiquement ne compilaient pas directement.

Les principaux problèmes observés étaient :

- accès aux attributs privés (`names`, `extensions`, `globs`) ;
- données manquantes avant certains appels à `matches()` ;
- mauvaise interprétation de certains patterns ;
- oracles incorrects.

Par exemple, ChatUniTest supposait que :

```java
patterns.matches(null);
```

retournait `null`, alors que la méthode lève une `IllegalArgumentException`.

Quatre scénarios ont été retenus et corrigés :

| Test | Objectif |
| --- | --- |
| `testMatchesExactName` | Nom exact |
| `testMatchesExtension` | Extension |
| `testAddGlobPattern` | Glob pattern |
| `testMatchesNullName` | Argument `null` |

Ils se trouvent dans :

```text
src/test/java/org/apache/tika/mime/PatternsChatUnitTest.java
```

Les corrections ont principalement consisté à supprimer les accès privés, préparer correctement les données, corriger les patterns et corriger les résultats attendus.

Après correction :

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

---

## 4. Analyse PIT après ChatUniTest

PIT a été relancé après l'ajout des quatre tests corrigés.

| Mesure | Tests originaux | + ChatUniTest |
| --- | ---: | ---: |
| Mutants générés | 48 | 48 |
| Mutants tués | 31 | 38 |
| Survivants | 10 | 4 |
| Sans couverture | 7 | 6 |
| Mutation coverage | 65 % | 79 % |

Les tests ChatUniTest permettent donc de tuer **7 mutants supplémentaires**.

PIT attribue notamment :

- `testMatchesExactName` → mutations dans `add()`, `addName()` et `matches()` ;
- `testMatchesExtension` → mutations dans `add()` et `matches()` ;
- `testAddGlobPattern` → mutations dans `add()`, `addGlob()`, `compile()` et `matches()`.

Ces mutants sont détectés car les tests vérifient le résultat observable : si une mutation empêche l'ajout, la compilation ou la reconnaissance d'un pattern, l'assertion échoue.

`testMatchesNullName` vérifie le comportement avec `null`, mais PIT ne lui attribue pas directement de mutant tué.

---

## 5. Tests supplémentaires manuels

Après ChatUniTest, quatre mutants avaient encore le statut `SURVIVED`.

Quatre tests manuels ont été ajoutés dans :

```text
src/test/java/org/apache/tika/mime/PatternsManualTest.java
```

| Test | Objectif |
| --- | --- |
| `testExplicitJavaRegex` | Expression régulière explicite |
| `testQuestionMarkPattern` | Pattern contenant `?` |
| `testBracketPattern` | Pattern contenant `[` |
| `testLongerGlobHasPriority` | Priorité entre glob patterns |

Résultat :

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Après ces tests :

```text
Line Coverage: 80/84 (95%)
Generated 48 mutations Killed 41 (85%)
Mutations with no coverage 6
Test strength 98%
```

Les tests manuels tuent donc **3 mutants supplémentaires**.

---

## 6. Résultats finaux

| Étape | Mutants tués | Mutation coverage |
| --- | ---: | ---: |
| Tests originaux | 31 / 48 | 65 % |
| + ChatUniTest | 38 / 48 | 79 % |
| + tests manuels | 41 / 48 | 85 % |

ChatUniTest a proposé des scénarios utiles, mais une correction manuelle était nécessaire. L'utilisation de PIT a ensuite permis de cibler les faiblesses restantes avec des tests supplémentaires.

---

## 7. GitHub Actions
