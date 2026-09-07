# KPI et ledger — finance produit

## Compteurs distincts

| Compteur | Source | Unité | Destination dashboard |
|---|---|---|---|
| `shop.order.paid.v1` | Webhook Shopify | EUR cents | kvnbbg.fr / Budget |
| `shop.margin.estimated.v1` | Adapter POD | EUR cents | marge par SKU |
| `game.wallet.credited.v1` | DivisionRuleService | GOLD / CREDITS | économie de jeu |
| `game.wallet.debited.v1` | sinks | GOLD / CREDITS | économie de jeu |
| `ops.cost.recorded.v1` | saisie mensuelle | EUR cents | rentabilité studio |

Jamais additionner GOLD et EUR dans un même total.

## Schéma d’événement commande

```json
{
  "event_id": "uuid",
  "event_type": "shop.order.paid.v1",
  "occurred_at": "2026-09-07T12:30:00Z",
  "correlation_id": "shopify-order-id",
  "anonymous_id": "uuid",
  "player_id": "uuid-or-null",
  "order": {
    "sku": ["DBZ-TEE-SIGIL"],
    "currency": "EUR",
    "gross_cents": 3500,
    "fees_cents": 140,
    "cogs_cents": 1500,
    "contribution_cents": 1860
  },
  "game_bonus": {
    "status": "pending_server",
    "hint_item": "Boundary Sigil"
  }
}
```

Le bonus de jeu n’est appliqué qu’après idempotence `wallet_ledger_once`.
