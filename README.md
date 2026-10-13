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

## 7. GitHub Actions



-------------



## 8. Travail complémentaire — classe `MediaType`

### Classe choisie

La classe choisie est :

```text
org.apache.tika.mime.MediaType
```

Cette classe possède déjà des tests (`MediaTypeTest`), mais sa couverture initiale n'est pas complète.

### Couverture JaCoCo initiale

| Instructions | Branches | Lignes manquées |
| ---: | ---: | ---: |
| 79 % | 74 % | 30 / 156 |

### PIT initial

| Mutants | Tués | Survivants | Sans couverture | Mutation coverage |
| ---: | ---: | ---: | ---: | ---: |
| 85 | 66 | 3 | 16 | 78 % |

Nous avons notamment ciblé `hasParameters()`, qui n'avait pas d'assertions directes dans les tests originaux.

---

## 8. Génération avec ChatUniTest pour `MediaType`

ChatUniTest `2.1.1` a été utilisé avec Ollama et le modèle local `codeqwen:v1.5-chat`.

La première tentative, sur toute la classe `MediaType`, a duré environ 30 minutes. ChatUniTest a tenté de générer 11 méthodes de test, mais aucune n'a été validée automatiquement : 9 erreurs de compilation et 2 erreurs d'extraction.

Une deuxième tentative a ensuite ciblé `hasParameters()`. Cette tentative a duré environ 10 minutes, mais les deux essais de compilation ont également échoué.

Les tests générés utilisaient notamment Mockito (`@Mock`, `MockitoExtension`, `when(...)`), ce qui provoquait des erreurs dans la configuration du projet.

Les journaux d'erreur ont été enregistrés dans :

```text
C:\tmp\chatunitest-info\tika-parent\tika-core\error-message\
```

---

## 9. Tests générés pour `MediaType` et critique des oracles

Deux scénarios proposés par ChatUniTest ont été retenus, puis réécrits manuellement. Les principales corrections ont consisté à supprimer les mocks Mockito, à créer de vrais objets `MediaType` et à utiliser des assertions sur leur comportement réel.

| Test | Objectif |
| --- | --- |
| `testHasParametersTrue` | Vérifier que `hasParameters()` retourne `true` lorsqu'il y a des paramètres |
| `testHasParametersFalse` | Vérifier que `hasParameters()` retourne `false` lorsqu'il n'y a aucun paramètre |

Le premier test utilise `charset=UTF-8`, tandis que le deuxième utilise un type MIME sans paramètres.

Les oracles `assertTrue()` et `assertFalse()` proposés étaient adaptés à une méthode booléenne. Cependant, les mocks utilisés par ChatUniTest ne vérifiaient pas directement la logique de `MediaType`. Les tests originaux, eux, vérifiaient des valeurs concrètes après les opérations.

Après correction, les deux tests ont réussi :

```text
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Aucun test généré n'a donc fonctionné sans intervention manuelle.

---

## 10. Analyse PIT après ChatUniTest pour `MediaType`

| Mesure | Tests originaux | + ChatUniTest corrigé |
| --- | ---: | ---: |
| Mutants générés | 85 | 85 |
| Mutants tués | 66 | 66 |
| Survivants | 3 | 3 |
| Sans couverture | 16 | 16 |
| Mutation coverage | 78 % | 78 % |

Les deux tests corrigés n'ont pas éliminé de mutants supplémentaires.

Trois mutants restaient survivants :

- **Ligne 260 (`MathMutator`)** : modification de `slash + 1` dans `parse()`.
- **Ligne 293 (`BooleanFalseReturnValsMutator`)** : `isSimpleName()` retourne toujours `false`.
- **Ligne 349 (`NegateConditionalsMutator`)** : inversion d'une condition dans `union()`.

Les deux premiers peuvent modifier le chemin d'exécution de `parse()` sans nécessairement changer le résultat. Le troisième peut modifier la fusion des paramètres.

---

## 11. Tests supplémentaires manuels pour `MediaType`

Quatre tests ont été ajoutés dans :

```text
MediaTypeMutationTest.java
```

| Test | Objectif |
| --- | --- |
| `testUnionPreservesExistingAndNewParameters` | Vérifier que la fusion conserve les paramètres `charset=UTF-8` et `version=1` |
| `testParseSimpleMediaType` | Vérifier qu'un type MIME simple comme `application/json` est correctement analysé |
| `testParseSimpleNameWithValidCharacters` | Vérifier qu'un sous-type contenant des lettres, des chiffres et un trait d'union est reconnu |
| `testParseCachesSimpleMediaTypes` | Vérifier que deux appels à `parse()` sur le même type simple retournent la même instance |

Le test `testUnionPreservesExistingAndNewParameters` vérifie les deux valeurs de paramètres et le nombre total de paramètres avec `assertEquals()`.

Le test `testParseSimpleMediaType` vérifie le type, le sous-type et la représentation complète.

Le test `testParseSimpleNameWithValidCharacters` utilise `application/x-test123` et compare les résultats aux valeurs attendues.

Le test `testParseCachesSimpleMediaTypes` analyse deux fois `application/x-ift3913-cache-test-2026`, une valeur spécifique choisie pour limiter les collisions avec d'autres tests. L'assertion `assertSame()` vérifie que les deux appels retournent la même instance.

Le premier test a permis d'éliminer le mutant de `union()` à la ligne 349. Les trois autres visaient `parse()` et son mécanisme de cache, mais les deux mutants restants n'ont pas été éliminés.

Les quatre tests ont réussi :

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 12. Résultats finaux pour `MediaType`

| Étape | Mutants tués | Mutation coverage |
| --- | ---: | ---: |
| Tests originaux | 66 / 85 | 78 % |
| + ChatUniTest corrigé | 66 / 85 | 78 % |
| + tests manuels | 71 / 85 | 84 % |

Après l'ajout des tests manuels, la couverture de mutation est passée de **78 % à 84 %**, soit cinq mutants supplémentaires éliminés. La couverture des lignes est passée de 80 % à 83 %, et le nombre de mutants sans couverture est passé de 16 à 12.

Le mutant de `union()` à la ligne 349 a été éliminé. Les deux mutants de `parse()` aux lignes 260 et 293 survivent toujours malgré les tests ajoutés. Ils semblent surtout toucher le chemin optimisé de `parse()` sans modifier le résultat pour les entrées testées.

ChatUniTest a proposé des scénarios utiles, mais des corrections manuelles ont été nécessaires. L'amélioration du score de mutation vient des tests manuels ajoutés après l'analyse PIT.

### Commandes utilisées

Exécution des tests corrigés de ChatUniTest :

```powershell
.\mvnw.cmd -pl tika-core "-Dtest=MediaTypeChatUniTestTest" test
```

Exécution des tests manuels :

```powershell
.\mvnw.cmd -pl tika-core "-Dtest=MediaTypeMutationTest" test
```

Analyse de mutation avec PIT :

```powershell
.\mvnw.cmd -pl tika-core "-DtargetClasses=org.apache.tika.mime.MediaType" org.pitest:pitest-maven:1.19.4:mutationCoverage
```

---

## 13. GitHub Actions

