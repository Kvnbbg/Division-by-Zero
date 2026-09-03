# TDAAH data pipeline — Spring Boot architecture

© 2026 Kevin Marville · [techandstream.com](https://techandstream.com) · [kvnbbg.fr](https://kvnbbg.fr) · [kvnbbg-creations.io](https://kvnbbg-creations.io)

Public document. No hostnames, credentials, or rack identifiers.

## What a pipeline is

A data pipeline is a short chain of applications that **move and transform** records between systems.

### Read (sources)

- File from a source system (CSV, JSON, local path configured by env).
- Database through a query interface (JDBC later; interface only in v0.2).
- Any HTTP API that returns records.

### Write (sinks)

- File on the target system.
- Database.
- Another API.

### Application kinds

| Kind | Life cycle | Start | Stop |
|---|---|---|---|
| **Batch** | Short-lived | Schedule or manual `POST /v1/pipeline/run` | Terminates after completion |
| Streaming / long-lived | Out of scope for this milestone | — | — |

TDAAH batch jobs must **end**. They do not mint NFTs and they do not keep an LLM loop open.

## Engineering rules

1. Named failure: a zero denominator in a ratio step raises `ZeroDivisionMeasurementException` (HTTP 422). No silent `Infinity`.
2. Secrets only via environment (`TDAAH_SOURCE_PATH`, later `SPRING_DATASOURCE_*`). Never in Git.
3. One transform, one metric. Counters: records in, records out, zero-division refusals.
4. Batch contract: start timestamp, end timestamp, status `COMPLETED` or `FAILED`.

## Package map (`agent-spring-boot`)

```
fr.kvnbbg.tdaah
  TdaahAgentApplication
  api/          REST: /v1/agent/tools, /v1/pipeline/run
  pipeline/
    SourceReader      file | jdbc | api
    SinkWriter        file | jdbc | api
    RecordTransform   including SafeRatioTransform
    BatchPipeline     orchestrates read → transform → write → exit
```

## Run

```bash
cd agent-spring-boot
mvn spring-boot:run
curl -s localhost:8080/v1/pipeline/kinds
curl -s -X POST localhost:8080/v1/pipeline/run -H 'Content-Type: application/json' \
  -d '{"source":"file","sink":"file","numeratorField":"distance","denominatorField":"hours"}'
```

Code: [github.com/Kvnbbg/Division-by-Zero](https://github.com/Kvnbbg/Division-by-Zero)
