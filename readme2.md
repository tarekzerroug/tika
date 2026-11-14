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

| Nº | Fichier et méthode | CWE | Modification introduite | Règle Semgrep | Difficulté |
|---:|---|---|---|---|---|
| 1 | `tika-pipes/tika-async-cli/src/main/java/org/apache/tika/async/cli/TikaAsyncCLI.java`, `main()` | CWE-276 — Incorrect Default Permissions | Le fichier de configuration temporaire reçoit les permissions `rw-rw-rw-`. | `java.lang.security.audit.overly-permissive-file-permission.overly-permissive-file-permission` |
| 2 | `tika-pipes/tika-httpclient-commons/src/main/java/org/apache/tika/client/HttpClientFactory.java`, `build()` | CWE-326 — Inadequate Encryption Strength | Le contexte TLS utilise explicitement l’ancien protocole `TLSv1`. | `java.lang.security.audit.weak-ssl-context.weak-ssl-context` |
| 3 | `tika-pipes/tika-pipes-core/src/main/java/org/apache/tika/pipes/core/SharedServerManager.java`, `startServer()` | CWE-330 — Use of Insufficiently Random Values | Le jeton d’authentification est généré avec `java.util.Random` au lieu de `SecureRandom`. | `java.lang.security.audit.crypto.weak-random.weak-random` | 

| 4 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/TikaLoggingFilter.java`, `filter()` | CWE-1004 — Sensitive Cookie Without HttpOnly Flag | Un cookie de session est ajouté sans appeler `setHttpOnly(true)`. | `java.lang.security.audit.cookie-missing-httponly.cookie-missing-httponly` | 
| 5 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/resource/MetadataResource.java`, `getMetadata()` | CWE-502 — Deserialization of Untrusted Data | Le flux HTTP entrant est désérialisé directement avec `ObjectInputStream.readObject()`. | `java.lang.security.audit.object-deserialization.object-deserialization` | 
| 6 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/ConfigEndpointSecurityFilter.java`, `filter()` | CWE-614 — Sensitive Cookie Without Secure Attribute | Un cookie est marqué `HttpOnly`, mais il est ajouté sans appeler `setSecure(true)`. | `java.lang.security.audit.cookie-missing-secure-flag.cookie-missing-secure-flag` | 
| 7 | `tika-server/tika-server-core/src/main/java/org/apache/tika/server/core/resource/TikaMimeTypes.java`, `getMimeTypeDetailsHTML()` | CWE-22 — Path Traversal | Le paramètre JAX-RS `subtype` est utilisé dans un chemin sous `/tmp` sans validation avant la lecture du fichier. | `java.jax-rs.security.jax-rs-path-traversal.jax-rs-path-traversal` | 

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

| Rapport | Détections |
|---|---:|
| Avant les modifications | 16 |
| Après les modifications | 23 |
| Nouvelles détections | **7** |


## Conclusion

Le rapport initial contient 16 détections préexistantes. Après l’introduction des sept vulnérabilités, Semgrep produit 23 résultats. La différence correspond exactement aux sept CWE distinctes ajoutées dans les sept fichiers Java sélectionnés.

Semgrep a donc détecté chacune des vulnérabilités introduites tout en conservant les résultats qui existaient déjà dans Apache Tika.
