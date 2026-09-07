# Fiche de poste — Développeur / Développeuse Java Spring Boot (France)

**Employeur :** Kevin Marville / Tech & Stream (`techandstream.com`, `kvnbbg.fr`)  
**Produit :** Division by Zero — TDAAH, monolithe modulaire Spring Boot  
**Contrat :** CDI ou freelance portage, selon profil  
**Lieu :** France — remote-first, ponctuel présentiel possible (Île-de-France / visio)  
**Expérience :** 3 à 7 ans (confirmé)  
**Rémunération indicative CDI :** 48–65 k€ brut / an selon île-de-France vs régions, à négocier  
**TJ indicative freelance :** 450–650 € HT / jour

Les fourchettes sont des **ordres de grandeur de recrutement**, pas une offre ferme.

## Mission

Faire passer `agent-spring-boot` d’un agent de mesure à la plateforme Division by Zero :

- centraliser `DivisionRuleService` (DRY) ;
- exposer REST et, ensuite, GraphQL ;
- historiser attempts + `wallet_ledger` append-only dans PostgreSQL ;
- publier des événements RabbitMQ versionnés ;
- brancher les webhooks Shopify sans mélanger EUR et GOLD ;
- garantir HTTP 422 + Problem Details sur division par zéro ;
- écrire les tests d’acceptance (dont `5 / 0` → `UNDEFINED`, `0 / 0` → `INDETERMINATE`).

## Fonction (quotidien)

1. Implémenter les cas d’usage dans `tdaah-application`.
2. Maintenir le domaine pur (pas d’annotation web dans le cœur métier).
3. Relire les PR contre `docs/ANTI-BIAIS.md` et `docs/architecture/SPRING-BOOT.md`.
4. Mesurer Actuator / Micrometer, pas seulement « ça compile ».
5. Documenter OpenAPI. Ne jamais laisser un LLM écrire le ledger.

## Compétences requises

- Java 21, Spring Boot 3, Spring Web, Validation, Actuator
- JPA / PostgreSQL, transactions, indexes d’idempotence
- Tests JUnit 5, Testcontainers souhaité
- Git, PR courtes, français écrit clair
- Comprendre qu’une division par zéro est un **état de domaine**, pas un crash fatal ni une preuve physique

## Compétences appréciées

- `spring-boot-starter-amqp`, `spring-boot-starter-graphql`
- BigDecimal et comptabilité événementielle
- Shopify webhooks, RFC 7807
- Bases d’accessibilité front si collaboration avec `web/`

## Hors périmètre du poste

Mint NFT, copy de CSS tiers, attribution de crédits par prompt, usage de marques protégées.

## Candidature

CV + lien GitHub + un exemple de gestion d’erreur métier (idéalement 4xx documenté).  
Contact public : contact@techandstream.com · https://kvnbbg.fr

## Conformité France

- Égalité de traitement des candidatures.
- Aucun critère hors compétences et disponibilité légale de travailler en France.
- Télétravail : matériel à la charge du candidat en freelance ; à préciser en CDI.
- Données de candidature : finalité recrutement uniquement, durée limitée.
