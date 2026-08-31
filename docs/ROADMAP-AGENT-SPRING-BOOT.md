# Feuille de route — Agent TDAAH (Java Spring Boot)

**Pile :** Java 21 · Spring Boot 3.4+ · Spring AI (option) · Maven
**Auteur :** Kevin Marville (Kvnbbg)

L’agent exécute des outils de mesure (conversion, calcul, rejet de la division par zéro). Ce n’est pas un chatbot décoratif.

## Principes

- CSS puis contrat, Java ensuite.
- Chaque capacité est une Tool typée.
- ZeroDivisionMeasurementException → HTTP 422.
- Attribution Kevin Marville ; pas de mint NFT dans le runtime.

## Architecture

web CSS+JS → Agent Spring Boot → domaine Measurement + orchestrateur + audit
LLM facultatif derrière tdaah.agent.llm.enabled

Modules : tdaah-domain, tdaah-api, tdaah-agent, tdaah-persistence, tdaah-boot

## Jalons

A Fondations (S1) : Java 21, Boot 3.4, Actuator, parité des tests Python (cm→m, 32F→0C, 10km/2h, 5m/0s).
B Contrat API (S2) : POST /v1/convert, POST /v1/calc, GET /v1/units, GET /v1/agent/tools, POST /v1/agent/invoke, OpenAPI, RFC 7807.
C Agent outillé (S3–4) : convert_measurement, evaluate_expression, list_units, explain_dimension, refuse_zero_division. Sans LLM d’abord.
D Persistance (S5) : MeasurementEvent, AgentInvocation, ToolCall, PostgreSQL / H2.
E Sécurité (S6–7) : API key, rate limit, Micrometer, image Temurin 21.
F Front (S8) : TDAAH_API_BASE, mode dégradé navigateur, CI mvn verify.

## Hors périmètre

Mint NFT, paiement, scraping des sites marque, copie de CSS tiers.

## Definition of Done

- Tests d’acceptance partagés
- 422 sur division par zéro
- OpenAPI
- Orchestrateur sans LLM
- Attribution visible
- Front online et offline
