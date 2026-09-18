# Division-by-Zero — API contracts

## Scope

This document records the stable boundary between the Spring Boot agent, numerical pipeline and web client.

## Pipeline boundary

- POST /v1/pipeline/run starts one short-lived batch.
- An absent request body preserves the historical defaults: FILE -> FILE, distance / hours.
- When a request body is supplied, source and sink are required. Explicit null values return 422 validation before the batch executes.
- Unknown enum values or unreadable JSON return 400 malformed_body.
- GET /v1/pipeline/kinds exposes the supported source and sink kinds.
- X-Correlation-Id is echoed when it matches the safe correlation-id format; otherwise a new identifier is generated.

## Agent and device surfaces

- GET /v1/agent/tools is a read-only capability registry. It does not execute a tool.
- GET /v1/device/helpers exposes the native-helper allowlist.
- GET /v1/device/bus delegates only to NativeBridge.Helper.BUS_INVENTORY.
- Native helper execution remains allowlisted and bounded by the bridge timeout; controller tests mock the bridge rather than invoking host binaries.

## Error contract

The current API error body contains:

```json
{
  "error": "non_finite_measurement",
  "message": "…",
  "status": 422
}
```

Known semantic categories include:

- zero_division_measurement -> 422
- non_finite_measurement -> 422
- validation -> 422
- malformed_body -> 400
- missing_parameter -> 400
- invalid_parameter -> 400

Consumers should branch on error and status, not on localized message text.

## Numeric contract

A ratio operation must reject:

- zero denominator;
- negative zero denominator;
- NaN numerator/denominator;
- positive or negative infinity;
- non-finite result.

Finite results remain ordinary numeric responses.

## Parity corpus

The canonical corpus lives under:

agent-spring-boot/src/test/resources/numeric-parity.json

Java and browser tests consume the same semantic cases. The corpus is the source of truth for cross-runtime behavior.

## API evolution

1. Preserve existing success payload fields.
2. Add error codes before introducing new transport shapes.
3. Keep HTTP status and semantic error category aligned.
4. Never serialize NaN or Infinity as a successful measurement.
5. Document dimension/conversion failures separately from arithmetic failures.
