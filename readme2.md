## Analyse Semgrep — Avant/Après

### Objectif
Comparer les rapports Semgrep avant et après l'introduction de 7 vulnérabilités de types différents dans Apache Tika afin de vérifier leur détection.

### Analyse initiale

Avant toute modification du code, nous exécutons :

```bash
semgrep scan --config p/security-audit --json --output semgrep-reports/before.json
```

### Analyse finale

Après l'introduction des 7 vulnérabilités :

```bash
semgrep scan --config p/security-audit --json --output semgrep-reports/after.json
```

### Comparaison des résultats
### Résultats de l'analyse initiale

L'analyse Semgrep du code original d'Apache Tika a été
réalisée avec la configuration `p/security-audit`.

- Fichiers analysés : 2 164
- Règles exécutées : 140
- Détections initiales : 16
- Rapport généré : `semgrep-reports/before.json`

Ces 16 détections constituent notre référence pour
identifier les nouvelles vulnérabilités introduites.

### Conclusion

À compléter après l'analyse finale.