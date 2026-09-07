# Architecture — monolithe modulaire Spring Boot

Pile cible : Java 21 · Spring Boot 3.4+ · PostgreSQL · RabbitMQ · GraphQL (starter) · AMQP

```text
Web apps
├── techandstream.com
├── kvnbbg.fr
└── kvnbbg-creations.io
        │
        ▼
Spring Boot API — Division by Zero / TDAAH
├── REST     commandes simples, webhooks Shopify
├── GraphQL  dashboards UX box
├── SOAP     adapter anti-corruption uniquement si partenaire legacy
├── PostgreSQL  vérité transactionnelle
├── RabbitMQ    événements versionnés
├── Prompt service   lore, pas le ledger
└── Shopify adapter  commandes / stock
```

## Modules Maven visés

| Module | Responsabilité |
|---|---|
| `tdaah-domain` | `DivisionRuleService`, statuts, exceptions 422 |
| `tdaah-application` | cas d’usage attempts, wallet, webhooks |
| `tdaah-api` | REST + GraphQL |
| `tdaah-messaging` | producteurs / consommateurs AMQP |
| `tdaah-persistence` | JPA, ledger append-only |
| `tdaah-boot` | assemblage, Actuator, config |

Aujourd’hui le code vit dans `agent-spring-boot` (artefact `tdaah-agent`). La découpe ci-dessus est la cible sans microservices prématurés.

## Contrats

| Besoin | Protocole | Endpoint / file |
|---|---|---|
| Soumettre une réponse | REST | `POST /v1/exercise-attempts` |
| Convertir une grandeur | REST | `POST /v1/convert`, `POST /v1/calc` |
| Dashboard | GraphQL | `player`, `economy`, `quest`, `bleeding` |
| Événements | RabbitMQ | `game.exercise.resolved.v1`, `shop.order.paid.v1` |
| Shopify | Webhook REST | adapter idempotent |

## Règle métier centrale

`DivisionRuleService.resolve(numerator, denominator)` est le seul point qui décide `ANSWER` | `UNDEFINED` | `INDETERMINATE`. REST, GraphQL et les consumers appellent ce service. Pas de `double` pour l’argent : `BigDecimal` + `bigint` ledger.

## Erreurs

Division par zéro métrologique → `ZeroDivisionMeasurementException` → HTTP 422 + RFC 7807.  
Ce n’est pas un 500. Ce n’est pas `Infinity`.
