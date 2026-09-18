# Correlation ID

`CorrelationIdFilter` adds a bounded `X-Correlation-Id` response header to every servlet request.

## Rules

- A supplied identifier is preserved only when it is 1–128 characters and matches `[A-Za-z0-9._:-]+`.
- Missing or unsafe identifiers are replaced with a UUID.
- The value is response metadata only; it does not alter numeric semantics.
- The filter uses `OncePerRequestFilter`, so downstream handlers see one correlation boundary per request.

## Why this is additive

Existing API response fields (`error`, `message`, `status`) are unchanged. Clients that do not know the header continue to work.

The identifier is intended for log correlation and future observability wiring, not for authentication or authorization.
