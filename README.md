# Analyse Semgrep d’Apache Tika — Avant/Après

## Objectif

Ce travail étudie la capacité de Semgrep à détecter des vulnérabilités introduites volontairement dans le code source Java d’Apache Tika.

Sept vulnérabilités ont été ajoutées dans sept fichiers Java existants.

## Analyse initiale

Le rapport initial a été produit avant l’introduction des vulnérabilités :

```bash
semgrep --config p/security-audit \
  --json \
  --output semgrep-reports/before.json \
  .
```

Résultats initiaux :

- fichiers analysés : 2 164 ;
- règles exécutées : 140 ;
- détections : 16 ;
- rapport : `semgrep-reports/before.json`.

Ces 16 résultats constituent la référence.

## Vulnérabilités introduites

|  Nº | Fichier et méthode                                                                                                               | CWE                                                 | Modification introduite                                                                                           | Règle Semgrep                                                                                  |
| --: | -------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
|   1 | `tika-pipes/tika-async-cli/src/main/java/org/apache/tika/async/cli/TikaAsyncCLI.java`, `main()`                                  | CWE-276 — Incorrect Default Permissions             | Le fichier de configuration temporaire reçoit les permissions `rw-rw-rw-`.                                        | `java.lang.security.audit.overly-permissive-file-permission.overly-permissive-file-permission` |
|   2 | `tika-pipes/tika-httpclient-commons/src/main/java/org/apache/tika/client/HttpClientFactory.java`, `build()`                      | CWE-326 — Inadequate Encryption Strength            | Le contexte TLS utilise explicitement l’ancien protocole `TLSv1`.                                                 | `java.lang.security.audit.weak-ssl-context.weak-ssl-context`                                   |
|   3 | `tika-pipes/tika-pipes-core/src/main/java/org/apache/tika/pipes/core/SharedServerManager.java`, `startServer()`                  | CWE-330 — Use of Insufficiently Random Values       | Le jeton d’authentification est généré avec `java.util.Random` au lieu de `SecureRandom`.                         | `java.lang.security.audit.crypto.weak-random.weak-random`                                      |
|   4 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/TikaLoggingFilter.java`, `filter()`                      | CWE-1004 — Sensitive Cookie Without HttpOnly Flag   | Un cookie de session est ajouté sans appeler `setHttpOnly(true)`.                                                 | `java.lang.security.audit.cookie-missing-httponly.cookie-missing-httponly`                     |
|   5 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/resource/MetadataResource.java`, `getMetadata()`         | CWE-502 — Deserialization of Untrusted Data         | Le flux HTTP entrant est désérialisé directement avec `ObjectInputStream.readObject()`.                           | `java.lang.security.audit.object-deserialization.object-deserialization`                       |
|   6 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/ConfigEndpointSecurityFilter.java`, `filter()`           | CWE-614 — Sensitive Cookie Without Secure Attribute | Un cookie est marqué `HttpOnly`, mais il est ajouté sans appeler `setSecure(true)`.                               | `java.lang.security.audit.cookie-missing-secure-flag.cookie-missing-secure-flag`               |
|   7 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/resource/TikaMimeTypes.java`, `getMimeTypeDetailsHTML()` | CWE-22 — Path Traversal                             | Le paramètre JAX-RS `subtype` est utilisé dans un chemin sous `/tmp` sans validation avant la lecture du fichier. | `java.jax-rs.security.jax-rs-path-traversal.jax-rs-path-traversal`                             |

## Justification des vulnérabilités

1. **Permissions trop permissives (CWE-276).** `TikaAsyncCLI` a été choisi parce qu’il crée un fichier temporaire de configuration. Les permissions `rw-rw-rw-` permettent à tous les utilisateurs de le modifier, ce qui pourrait altérer la configuration utilisée par Tika. Semgrep détecte cette modification à la ligne 122.

2. **Version faible de TLS (CWE-326).** `HttpClientFactory` configure les connexions HTTPS sortantes. L’utilisation de `TLSv1`, un protocole ancien et déprécié, réduit la protection des communications réseau. Semgrep signale l’appel à `SSLContext.getInstance("TLSv1")` à la ligne 276.

3. **Générateur aléatoire insuffisant (CWE-330).** `SharedServerManager` génère un jeton d’authentification, qui doit être imprévisible. `java.util.Random` n’est pas adapté à la cryptographie et peut produire des valeurs prévisibles. Semgrep détecte son utilisation à la ligne 246.

4. **Cookie sans `HttpOnly` (CWE-1004).** `TikaLoggingFilter` traite les requêtes HTTP et ajoute un cookie de session. Sans l’attribut `HttpOnly`, ce cookie peut être lu par JavaScript et dérobé en présence d’une attaque XSS. Semgrep signale `response.addCookie(cookie)` à la ligne 51.

5. **Désérialisation non sécurisée (CWE-502).** `MetadataResource` reçoit directement le corps d’une requête HTTP. Le passage de ce flux à `ObjectInputStream.readObject()` permet de désérialiser un objet contrôlé par un client, avec un risque de déni de service ou d’exécution de code. Semgrep détecte l’opération à la ligne 100.

6. **Cookie sans `Secure` (CWE-614).** `ConfigEndpointSecurityFilter` protège des endpoints de configuration sensibles. Le cookie est `HttpOnly`, mais l’absence de l’attribut `Secure` permet sa transmission sur une connexion HTTP non chiffrée. Semgrep signale son ajout à la ligne 47.

7. **Traversée de répertoires (CWE-22).** `TikaMimeTypes` reçoit le paramètre d’URL `subtype`. Celui-ci est utilisé sans validation dans un chemin sous `/tmp`, ce qui peut permettre la lecture d’un autre fichier avec une valeur contenant `../`. Semgrep détecte la méthode à partir de la ligne 165 ; l’instruction vulnérable se trouve à la ligne 169.

## Analyse finale

Le rapport final est généré sur tout le dépôt avec la même configuration :

```bash
semgrep --config p/security-audit \
  --json \
  --output semgrep-reports/after.json \
  .
```

Résultats finaux :

- fichiers analysés : 2 166 ;
- règles exécutées : 140 ;
- lignes analysées : environ 99,9 % ;
- détections : 23 ;
- rapport : `semgrep-reports/after.json`.

## Comparaison des résultats

| Rapport                 | Détections |
| ----------------------- | ---------: |
| Avant les modifications |         16 |
| Après les modifications |         23 |
| Nouvelles détections    |      **7** |

## Conclusion

Le rapport initial contient 16 détections préexistantes. Après l’introduction des sept vulnérabilités, Semgrep produit 23 résultats. La différence correspond exactement aux sept CWE distinctes ajoutées dans les sept fichiers Java sélectionnés.

Semgrep a donc détecté chacune des vulnérabilités introduites tout en conservant les résultats qui existaient déjà dans Apache Tika.

## Ajout du contrôle Semgrep au workflow

### Fonctionnement

On a modifié le fichier `.github/workflows/main-jdk17-build.yml` pour ajouter un job `semgrep` avant le build Java 17. Le job `build` dépend maintenant de ce contrôle avec `needs: semgrep`. Si Semgrep détecte une nouvelle vulnérabilité, le contrôle échoue et le build ne démarre pas.

On utilise Semgrep en version `1.185.0` avec la configuration `p/security-audit`. La version de l’outil est fixée pour garder le même environnement entre les exécutions. Les règles sont téléchargées depuis le registre Semgrep et peuvent évoluer.

Le but est de bloquer les nouvelles vulnérabilités sans bloquer le projet à cause de celles qui existaient déjà. Pour cela, on utilise `--baseline-commit` pour comparer les changements avec une version de référence :

- pour une pull request, la référence est le commit de la branche cible, donné par `github.event.pull_request.base.sha` ;
- pour un push sur `main`, la référence est le commit précédent, donné par `github.event.before`.

Le checkout utilise `fetch-depth: 0` pour récupérer l’historique Git nécessaire à cette comparaison.

L’option `--error` fait retourner un code de sortie de `1` lorsqu’une nouvelle vulnérabilité est détectée. On utilise aussi `--strict` pour que certaines erreurs d’analyse fassent échouer le contrôle.

Le résultat est enregistré dans un rapport JSON. Une étape avec `if: always()` permet de conserver ce rapport comme artefact GitHub Actions, même lorsque le contrôle échoue.

### Validation du contrôle

On a vérifié les deux situations attendues : un changement sans nouvelle vulnérabilité et un changement qui en ajoute une.

| Situation testée                                                         | Résultat                                                                                           |
| ------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------- |
| Analyse locale sans nouvelle vulnérabilité                               | Le contrôle réussit avec un code de sortie de `0`.                                                 |
| Analyse locale après l’ajout de `java.util.Random` pour générer un jeton | Semgrep détecte `weak-random` et retourne un code de sortie de `1`.                                |
| Pull request nº 1, sans nouvelle vulnérabilité par rapport à `main`      | Le job Semgrep réussit et le build Maven démarre.                                                  |
| Pull request nº 2, avec un chiffrement DES ajouté pour le test           | Semgrep détecte la nouvelle vulnérabilité et échoue. Les jobs `build` et `e2e-tests` sont ignorés. |

Pour le test sur GitHub, on a créé la branche `validation-semgrep`. Dans `CryptoParser.java`, on a remplacé temporairement :

```java
cipher = Cipher.getInstance(transformation);
```

par :

```java
cipher = Cipher.getInstance("DES");
```

Semgrep a détecté la règle `java.lang.security.audit.crypto.des-is-deprecated.des-is-deprecated` à la ligne 79. Le rapport contient une nouvelle détection et aucune erreur d’analyse. Le job termine avec le code de sortie `1`, ce qui confirme que l’échec vient bien de la vulnérabilité détectée.

Cette modification sert uniquement à valider le contrôle et reste sur la branche de validation.

Les rapports conservés dans le dépôt sont :

- `semgrep-reports/validation-ci/sans-ajout.json` ;
- `semgrep-reports/validation-ci/avec-ajout.json` ;
- `semgrep-reports/validation-ci/validation-des-github.json`.

Les exécutions GitHub Actions sont disponibles ici :

- [Contrôle Semgrep réussi, suivi du build Maven](https://github.com/tarekzerroug/tika/actions/runs/43963638718?pr=1)
- [Contrôle Semgrep en échec après l’ajout de DES](https://github.com/tarekzerroug/tika/actions/runs/43948475261?pr=2)

## Rickroll après un échec de test

### Fonctionnement

On a créé une action composite dans `.github/actions/rickroll/action.yml`. Elle affiche un message et un lien vers la vidéo de Rick Astley dans les logs. Elle ajoute aussi un lien dans le résumé de l’exécution avec `GITHUB_STEP_SUMMARY`.

On voulait déclencher le rickroll lorsqu’un test échoue, plutôt que pour n’importe quelle erreur Maven. Après un échec de l’étape Maven, un script Python vérifie donc les rapports XML de Surefire. S’il trouve un élément `<failure>` ou `<error>`, il indique qu’un test a échoué et l’action rickroll est appelée.

Le même principe a été ajouté au job E2E, avec la lecture des rapports Surefire et Failsafe.

L’action affiche le lien sans ouvrir automatiquement la vidéo. Elle ne masque pas non plus l’échec du test : le build reste en échec après le rickroll.

### Validation

Pendant une exécution sur GitHub Actions, Maven a échoué à cause de tests dans le module `tika-server-core`. L’étape de vérification a trouvé les rapports contenant des échecs, puis l’action rickroll s’est exécutée.

On a vérifié que le message et le lien apparaissent dans les logs ainsi que dans le résumé du workflow. Le build est resté en échec et le job E2E n’a pas démarré, puisqu’il dépend du build.

Cette exécution valide le déclenchement du rickroll après un échec de test dans le job `build`.

On a aussi vérifié le cas sans échec sur la branche `validation-rickroll`, avec une exécution ciblée de `BOMDetectorTest`. Les deux tests ont réussi : 0 échec, 0 erreur et 0 test ignoré. Le build Maven a réussi et les étapes de vérification des échecs et de rickroll ont été ignorées, comme prévu. Le job E2E était désactivé pour cette validation.

[Exécution sans échec de test](https://github.com/tarekzerroug/tika/actions/runs/43997747838/job/133754120890)

Le déclenchement du rickroll dans le job E2E reste à vérifier.

[Exécution montrant le rickroll après un échec de test](https://github.com/tarekzerroug/tika/actions/runs/43963638718?pr=1)

On a également provoqué une erreur Maven avant l’exécution des tests avec la commande `mvn objectif-inexistant-validation -B`, sur la branche `validation-rickroll`. Maven a échoué avec le message `Unknown lifecycle phase`. La vérification des rapports s’est exécutée et le rickroll a été ignoré. Le build est resté en échec. Ce scénario confirme qu’une erreur Maven sans échec de test ne déclenche pas le rickroll.

[Exécution avec une erreur Maven avant les tests](https://github.com/tarekzerroug/tika/actions/runs/44002597841/job/133770354226)
