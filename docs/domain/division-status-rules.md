# Règles de domaine — statut de division

```text
Division by Zero = anomalie détectée
                 + contexte capturé
                 + conséquence explicite
                 + résolution jouable ou métier
```

## Invariant SQL

Une ligne `math_exercise` avec `denominator = 0` doit avoir
`expected_status IN ('UNDEFINED', 'INDETERMINATE')`.
Sinon `expected_status = 'ANSWER'`.

## Invariant Java

`DivisionRuleService.resolve` est la seule fonction autorisée à produire un `DivisionOutcome`.
Les contrôleurs et consommateurs n’implémentent pas de copie de cette logique.

## Invariant événementiel

`event_type` versionné (`game.exercise.resolved.v1`).
Déduplication par `(player_id, currency, reason, correlation_id)` sur `wallet_ledger`.
