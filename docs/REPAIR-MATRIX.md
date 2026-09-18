# Division-by-Zero — repair matrix

## Existing → missing → safe repair

| Area | Existing behavior | Missing/weak boundary | Smallest safe repair |
|---|---|---|---|
| Numeric safety | finite checks | duplicated predicates | centralize only after parity is stable |
| Error transport | stable map | Problem Details compatibility | adapter, not replacement |
| OpenAPI | Spring endpoints | explicit contract document | generate/specify after endpoint inventory |
| Accessibility | minimal web UI | dynamic announcements | `aria-live` + keyboard tests |
| History | browser state | explicit lifecycle | document session semantics |
| Observability | Actuator dependency | correlation ID | servlet filter + tests |
| Conversion | existing pipeline | registry visibility | read-only unit registry |
| Tests | parity corpus | broader API fixture coverage | focused controller tests |

## Repair order

1. Preserve numeric semantics.
2. Add API contract fixtures.
3. Add accessibility regression tests.
4. Add correlation ID without changing response semantics.
5. Add OpenAPI documentation.
6. Add conversion/component integration only after the contracts are tested.

## Do not do

- Do not replace the numeric engine with a framework.
- Do not add persistence merely for local history.
- Do not duplicate the parity corpus.
- Do not silently convert invalid numbers into zero.
- Do not change error status codes without updating clients and tests.
