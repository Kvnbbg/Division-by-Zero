package fr.kvnbbg.tdaah.pipeline;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PipelineModels {

    private PipelineModels() {}

    public enum SourceKind {
        FILE, JDBC, API
    }

    public enum SinkKind {
        FILE, JDBC, API
    }

    public enum JobStatus {
        COMPLETED, FAILED
    }

    public record PipelineRecord(Map<String, Object> fields) {
        public PipelineRecord {
            fields = new LinkedHashMap<>(fields);
        }
    }

    public record RunRequest(
            SourceKind source,
            SinkKind sink,
            String numeratorField,
            String denominatorField) {}

    public record BatchResult(
            Instant startedAt,
            Instant endedAt,
            JobStatus status,
            int readCount,
            int writeCount,
            String message,
            List<PipelineRecord> sample) {}
}
