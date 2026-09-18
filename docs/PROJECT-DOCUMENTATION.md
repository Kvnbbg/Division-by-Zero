# Division-by-Zero — Documentation technique et produit

## 1. Vue d’ensemble

**Division-by-Zero** est le laboratoire TDAAH de Kevin Marville : un appareil de mesure, de conversion et de calcul dimensionnel accompagné d’un backend Java/Spring Boot orienté contrats, validation et refus explicite des opérations mathématiquement ou numériquement non représentables.

Le projet est volontairement hybride :

- **Web statique** : HTML + CSS + JavaScript sans framework.
- **Moteur TDAAH** : conversions d’unités, dimensions physiques et expressions.
- **Backend** : Java 21 / Spring Boot dans `agent-spring-boot`.
- **API** : contrats `/v1`, erreurs métier et audit.
- **Pipeline** : lecture → transformation → écriture, avec rejet nommé de la division par zéro.
- **Documentation** : architecture, sécurité, feuille de route et provenance.
- **CLI / agent** : surface destinée aux usages locaux et à l’orchestration outillée.

Le README actuel décrit le produit comme un appareil de mesure métrique et de conversion, avec une interface CSS-first, une logique JavaScript et une CLI Python historique.

## 2. Philosophie d’architecture

### CSS d’abord, contrat ensuite, code ensuite

L’interface doit rester lisible avant d’être interactive. Le navigateur fournit une surface minimale : convertir, calculer, afficher le résultat, afficher l’historique.

Le code métier ne doit pas être mélangé au rendu.

### Une règle mathématique, un point de vérité

Le backend possède `DivisionRuleService`, qui centralise la règle `a / b` :

- dénominateur non nul → résultat ;
- `0 / 0` → état `INDETERMINATE` ;
- `a / 0` avec `a ≠ 0` → état `UNDEFINED`.

Le contrôleur REST transforme les états non résolus en HTTP 422.

### Refus explicite plutôt que valeur silencieusement fausse

Le pipeline utilise `ZeroDivisionMeasurementException`. Le frontend dispose d’un `ZeroDivisionMeasurementError`. Cette symétrie est importante : une valeur flottante `Infinity` ne doit jamais devenir une réponse métier valide.

## 3. Architecture

```
Browser
  │
  ├── web/index.html
  ├── web/css/tdaah.css
  └── web/js/
        ├── convertisseur.js
        └── app.js
             │
             └── TDAAH
                 ├── lookup()
                 ├── convert()
                 ├── calculate()
                 ├── dimensions
                 └── finite/representable guards

agent-spring-boot
  │
  ├── api/
  │    ├── DivisionController
  │    ├── AgentController
  │    ├── PipelineController
  │    ├── AuditController
  │    └── DeviceController
  │
  ├── domain/
  │    ├── DivisionRuleService
  │    ├── DivisionOutcome
  │    ├── DivisionStatus
  │    └── SideHustleState
  │
  ├── pipeline/
  │    ├── SourceReader
  │    ├── SafeRatioTransform
  │    ├── BatchPipeline
  │    └── SinkWriter
  │
  ├── security/
  │    ├── ApiKeyAuthFilter
  │    └── SecurityConfig
  │
  └── audit/
       ├── AuditEvent
       └── AuditService
```

## 4. Moteur de mesure

Le JavaScript maintient une représentation dimensionnelle vectorielle pour longueur, masse, temps, courant, température, surface, volume, vitesse, énergie, puissance, pression et force.

Les conversions utilisent un facteur et, pour les températures, un offset.

Exemples de familles présentes :

- longueur : m, km, cm, mm, in, ft, mi ;
- masse : kg, g, t, lb ;
- temps : s, min, h ;
- température : K, C, F ;
- surface : m2, ha ;
- volume : m3, L, mL ;
- vitesse : m/s, km/h, mph ;
- énergie : J, kWh, cal ;
- puissance : W, kW ;
- pression : Pa, bar, atm, psi ;
- force : N.

Les additions et soustractions exigent des dimensions compatibles. Multiplication et division combinent les vecteurs dimensionnels.

## 5. Parser d’expressions

Le moteur utilise trois niveaux :

1. `parseExpr` pour `+` et `-` ;
2. `parseTerm` pour `*` et `/` ;
3. `parseFactor` pour nombres, signes et parenthèses.

Cela donne une précédence classique sans dépendance externe.

Exemples conceptuels :

```
10 km / 2 h
5 m / 0 s
(10 m + 2 m) / 2 s
```

Le parser ne doit cependant pas être considéré comme un langage mathématique complet. Il s’agit d’un DSL de mesure volontairement borné.

## 6. Sécurité numérique

Le frontend contient déjà deux protections distinctes :

- `requireFinite` rejette NaN et Infinity en entrée ;
- `requireRepresentable` rejette les résultats non finis ;
- la division rejette un dénominateur nul ;
- la division rejette également les rapports qui deviennent non représentables avec un dénominateur non nul extrêmement petit.

C’est un point important pour les valeurs du type `1 / 1e-300`.

### Point d’amélioration backend

`SafeRatioTransform` utilise encore `double` et vérifie seulement `d == 0.0d`. Il faut aligner cette couche sur la politique de représentation du moteur TDAAH :

- rejeter NaN ;
- rejeter Infinity ;
- contrôler le numérateur et le dénominateur avant calcul ;
- contrôler le ratio après calcul ;
- idéalement utiliser `BigDecimal` lorsqu’une exactitude décimale est attendue.

## 7. API

Le contrat actuellement visible pour la division est :

```
POST /v1/exercise-attempts
Content-Type: application/json

{
  "numerator": 10,
  "denominator": 4
}
```

Réponse normale :

```json
{
  "status": "ANSWER",
  "value": 2.500000,
  "gameplayEvent": "MATH_ANSWER"
}
```

Pour une division indéfinie ou indéterminée, le backend répond HTTP 422.

Cette séparation est saine : HTTP 422 indique que la requête est syntaxiquement exploitable mais ne produit pas une mesure métier acceptable.

## 8. Pipeline de données

Le pipeline suit le contrat :

```
Source
  ↓
Read
  ↓
Transform
  ↓
SafeRatioTransform
  ↓
Write
  ↓
Completed / Failed
```

Les métriques prévues sont notamment :

- records in ;
- records out ;
- refus de division par zéro ;
- timestamps de début et de fin ;
- état de batch.

Les batchs doivent se terminer. Aucun LLM ne doit maintenir une boucle infinie de traitement.

## 9. Tests

Les tests Spring Boot couvrent déjà :

- division normale ;
- division par zéro ;
- zéro sur zéro ;
- mapping HTTP 422 ;
- réponse HTTP 200 pour une division valide.

La suite doit être étendue aux cas limites numériques et aux contrats de parité frontend/backend.

### Matrice de tests recommandée

| Cas | Résultat attendu |
|---|---|
| 10 / 4 | 2.5 |
| 1 / 0 | UNDEFINED + 422 |
| 0 / 0 | INDETERMINATE + 422 |
| 1 / 1e-300 | résultat contrôlé ou refus explicite |
| NaN | rejet |
| +Infinity | rejet |
| -Infinity | rejet |
| conversion valide | résultat fini |
| conversion overflow | rejet |
| dimensions incompatibles | erreur métier |
| expression incomplète | erreur utilisateur |
| parenthèse absente | erreur utilisateur |

## 10. Audit d’amélioration

### A — Cohérence numérique : priorité haute

Aligner `SafeRatioTransform` sur les contrôles `requireFinite` / `requireRepresentable` du frontend.

**Pourquoi :** le frontend a déjà une politique plus stricte que le pipeline Java.

### B — Contrat partagé frontend/backend : priorité haute

Créer une spécification machine-readable, par exemple OpenAPI + exemples contractuels, puis vérifier les mêmes scénarios dans JS et Java.

### C — Precision policy : priorité haute

Documenter explicitement :

- précision maximale ;
- arrondi ;
- unités avec offset ;
- comportement en overflow ;
- comportement pour les très petits dénominateurs.

### D — API errors : priorité haute

Uniformiser les erreurs autour de RFC 7807 / Problem Details, avec :

- type ;
- title ;
- status ;
- detail ;
- instance ;
- code métier.

### E — Observabilité : priorité moyenne/haute

Ajouter métriques Micrometer et corrélation des requêtes :

```
request_id
operation
unit/source
unit/target
duration_ms
status
failure_code
```

Ne jamais enregistrer les secrets ou données sensibles.

### F — Validation de configuration : priorité moyenne

Refuser au démarrage les configurations impossibles plutôt que de découvrir l’erreur à la première requête.

### G — Documentation utilisateur : priorité moyenne

Ajouter une documentation interactive courte : « convertir », « calculer », « pourquoi 422 ? », « exemples de dimensions ».

### H — Frontend accessibility : priorité moyenne

Ajouter :

- focus visible ;
- annonces ARIA pour les résultats ;
- navigation clavier complète ;
- messages d’erreur associés aux champs ;
- unités avec labels explicites ;
- tests d’accessibilité.

## 11. Ce qui doit rester hors périmètre

Le projet ne doit pas devenir simultanément :

- une marketplace ;
- un système de paiement ;
- un moteur NFT runtime ;
- un scraper de plateformes ;
- un agent autonome avec exécution arbitraire ;
- une copie de design tiers.

La séparation des responsabilités protège le cœur TDAAH.

## 12. Roadmap technique proposée

### Phase 1 — Hardening

- tests non-finis ;
- tests overflow ;
- parity JS/Java ;
- Problem Details ;
- validation stricte des entrées.

### Phase 2 — Contrat

- OpenAPI ;
- schémas partagés ;
- exemples curl ;
- documentation générée ;
- versionnement `/v1`.

### Phase 3 — Observabilité

- Actuator ;
- Micrometer ;
- request correlation ;
- audit structuré ;
- métriques pipeline.

### Phase 4 — Production

- image Temurin 21 minimale ;
- configuration par environnement ;
- PostgreSQL si persistance activée ;
- health/readiness ;
- CI `mvn verify` ;
- SBOM et scan de dépendances.

### Phase 5 — Produit

- interface de mesure plus pédagogique ;
- historique persistant optionnel ;
- import/export de mesures ;
- visualisation de pipeline ;
- agent outillé sans LLM obligatoire.

## 13. Critères de qualité

Une évolution TDAAH est considérée complète lorsqu’elle :

1. possède un contrat explicite ;
2. ne produit pas silencieusement Infinity/NaN ;
3. possède des tests de frontière ;
4. reste observable ;
5. conserve l’attribution et la licence ;
6. ne mélange pas UI, domaine et infrastructure ;
7. fonctionne en mode dégradé lorsque le backend n’est pas disponible ;
8. documente ses limites.

## 14. Références du dépôt

- `README.md`
- `docs/PIPELINE.md`
- `docs/ROADMAP-AGENT-SPRING-BOOT.md`
- `SECURITY.md`
- `NOTICE`
- `LICENSE`

Dernière vérification documentaire : 18 septembre 2026.
