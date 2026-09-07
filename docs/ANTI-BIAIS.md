# Anti-biais — Division by Zero

Document opérationnel. Le GDD définit la vision ; ce fichier contraint le code, les prompts et les événements.

Référence canonique : `https://github.com/Kvnbbg/GDD/blob/main/docs/ANTI-BIAIS.md`

## Contrat mathématique

| Entrée | Statut | Événement gameplay | Réponse joueur attendue |
|---|---|---|---|
| `d ≠ 0` | `ANSWER` | `MATH_ANSWER` | valeur calculée |
| `n ≠ 0` et `d = 0` | `UNDEFINED` | `NULL_RIFT_UNDEFINED` | « indéfini » |
| `n = 0` et `d = 0` | `INDETERMINATE` | `NULL_RIFT_INDETERMINATE` | « indéterminé » + justification |

Le flottant IEEE (`Infinity`, `-Infinity`, `NaN`) est un artefact d’implémentation. Il n’est pas une vérité de jeu.

## Anti-biais technique

- Un seul `DivisionRuleService` pour REST, GraphQL et RabbitMQ (DRY).
- `BigDecimal` pour les calculs ; `bigint` pour le ledger.
- Le LLM génère titre, lore et énoncé. Le serveur recalcule le statut et les récompenses.
- Aucune PII dans les payloads AMQP (`anonymous_id`, `player_id`, `event_id`, `correlation_id`).

## Anti-biais narratif

- Lore : anomalie de domaine, pas preuve cosmologique.
- Quêtes : branches déterminées par le statut, pas par une « conscience qui crée le monde ».
- Items : `Boundary Sigil` signale une frontière de système, pas un artefact physique réel.
