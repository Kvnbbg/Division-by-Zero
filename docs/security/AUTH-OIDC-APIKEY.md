# Auth — API Key (lab) + OIDC (production)

© 2026 Kevin Marville · Tech & Stream

## Mode ticket, no thruster

| Méthode | Headers |
|---|---|
| GET ouverts | — (health, tools, kinds, helpers) |
| GET protégés | `X-API-Key: …` |
| POST / PUT / PATCH / DELETE | `X-API-Key: …` **et** `X-Ticket-Id: TCK-…` |

Sans ticket sur une mutation → **422** `ticket_required`.

## Lab (API key)

```yaml
tdaah:
  security:
    enabled: true
    api-key: ${TDAAH_API_KEY}   # never commit the real value
```

```bash
export TDAAH_API_KEY='rotate-me'
curl -s -X POST localhost:8080/v1/pipeline/run \
  -H "X-API-Key: $TDAAH_API_KEY" \
  -H "X-Ticket-Id: TCK-2026-0901-001" \
  -H 'Content-Type: application/json' \
  -d '{}'
```

## Production (OIDC)

Préférer un IdP (Keycloak, Entra ID, Okta…) :

1. Ajouter `spring-boot-starter-oauth2-resource-server`.
2. Configurer :

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://idp.example.com/realms/entreprise
```

3. Mapper les claims → rôles `IT_OPERATOR`, `AUDITOR`.
4. Conserver **X-Ticket-Id** pour les mutations (politique métier, indépendante de l’IdP).
5. MFA côté IdP (recommandation ANSSI / hygiène admin).

## Journal d’audit

`GET /v1/audit/events` — derniers événements (mémoire processus ; brancher JPA/PostgreSQL en étape D de la roadmap agent).

Champs : `at`, `actor`, `action`, `ticketId`, `resource`, `success`, `detail`.
