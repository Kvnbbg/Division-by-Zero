# TDAAH agent — Spring Boot 3.4 / Java 21

© 2026 Kevin Marville · https://techandstream.com · https://kvnbbg.fr · https://kvnbbg-creations.io

Short-lived **batch** data pipeline plus measurement tools.

- Sources: `FILE` | `JDBC` | `API`
- Sinks: `FILE` | `JDBC` | `API`
- Batch starts on schedule or `POST /v1/pipeline/run` and **terminates after completion**.
- Zero denominator → named refusal (`ZeroDivisionMeasurementException`), never `Infinity`.

```bash
mvn spring-boot:run
curl -s localhost:8080/v1/agent/tools
curl -s localhost:8080/v1/pipeline/kinds
curl -s -X POST localhost:8080/v1/pipeline/run -H 'Content-Type: application/json' \
  -d '{"source":"FILE","sink":"FILE","numeratorField":"distance","denominatorField":"hours"}'
```

Architecture: [docs/PIPELINE.md](../docs/PIPELINE.md)
