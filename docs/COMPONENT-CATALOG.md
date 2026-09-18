# Division-by-Zero — component catalog

This catalog turns the repository into a set of small, independently verifiable integration points.

### Core

- SafeRatioTransform
- finite-value guard
- zero-division exception
- non-finite exception
- conversion component
- parser
- pipeline

### API

- measurement endpoint adapter
- validation adapter
- exception handler
- health/Actuator surface
- future OpenAPI description

### Web

- input component
- unit selector
- result renderer
- error renderer
- history panel
- keyboard controls
- accessibility announcer
- diagnostic parity panel

### Quality

- Java numeric parity test
- browser parity test
- API fixture tests
- security regression checks
- CI workflow

### Integration principle

Each component should have one responsibility, one stable input contract and one observable output. Branch new components at existing boundaries rather than reaching into implementation details.

### Completion definition

A component is considered integrated only when:

1. its consumer is identified;
2. its producer is identified;
3. invalid input behavior is documented;
4. at least one regression test exists;
5. CI invokes that test where practical.
