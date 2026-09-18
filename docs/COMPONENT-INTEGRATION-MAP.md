# Division-by-Zero — component integration map

The application is intentionally small. The integration plan keeps the numerical core independent from UI and transport.

## Numerical core

| Component | Responsibility | Connects to |
|---|---|---|
| Safe ratio transform | finite division | pipeline |
| Zero-division exception | arithmetic contract | API handler |
| Non-finite exception | representability contract | API handler |
| Unit conversion | dimensional conversion | pipeline |
| Parser/DSL | input normalization | pipeline |
| Pipeline | ordered transformations | API/CLI |
| Numeric parity corpus | cross-runtime contract | Java + JS tests |

## API layer

| Component | Responsibility |
|---|---|
| REST controller | transport |
| validation | request constraints |
| ApiExceptionHandler | stable error envelope |
| Actuator | operational health |
| future Problem Details adapter | standards-compatible error transport |

## Web layer

| Component | Integration target |
|---|---|
| Measurement input | parser/pipeline |
| Result panel | finite result contract |
| Error panel | error-code mapping |
| Unit selector | conversion registry |
| History | session-only state |
| Accessibility announcer | `aria-live` result/error updates |
| Keyboard command layer | parser entry points |
| Numeric status badge | finite/invalid state |
| parity diagnostics | development/test surface |

## Components to branch next

1. **MeasurementControllerAdapter** — isolate HTTP mapping from domain logic.
2. **ProblemDetailsMapper** — optional standards layer without removing current fields.
3. **NumericContractGuard** — one reusable finite-number predicate.
4. **UnitRegistry** — centralize supported units and dimensions.
5. **ConversionPreview** — show source/target dimensions before execution.
6. **ResultAnnouncer** — accessible dynamic result announcements.
7. **ErrorCodePresenter** — map stable codes to user-facing text.
8. **HistoryStore** — keep session history independent from calculation.
9. **ParityRunner** — execute the canonical corpus from a single command.
10. **PipelineTrace** — optional diagnostic representation of transformation steps.
11. **HealthBadge** — show backend availability without coupling to calculations.
12. **API contract fixture loader** — reusable JSON fixtures for controller tests.

## Architecture rule

Domain calculations must not import web/UI classes. UI components must not duplicate arithmetic validation. API adapters translate; they do not calculate.
