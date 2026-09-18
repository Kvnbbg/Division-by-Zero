package fr.kvnbbg.tdaah.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatchPipelineTest {

    private final SourceReader reader = new SourceReader();
    private final SafeRatioTransform transform = new SafeRatioTransform();
    private final SinkWriter writer = new SinkWriter();
    private final BatchPipeline pipeline = new BatchPipeline(reader, transform, writer);

    @Test
    @DisplayName("le batch FILE conserve les lignes valides et refuse le zéro")
    void fileBatch() {
        PipelineModels.BatchResult result = pipeline.run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.FILE,
                PipelineModels.SinkKind.FILE,
                "distance",
                "hours"));

        assertEquals(PipelineModels.JobStatus.COMPLETED, result.status());
        assertEquals(3, result.readCount());
        assertEquals(2, result.writeCount());
        assertEquals(2, writer.lastWritten().size());
        assertEquals(2.5, writer.lastWritten().get(0).fields().get("ratio"));
        assertEquals(5.0, writer.lastWritten().get(1).fields().get("ratio"));
        assertNotNull(result.startedAt());
        assertNotNull(result.endedAt());
    }

    @Test
    @DisplayName("une mesure textuelle invalide est refusée sans interrompre le batch")
    void malformedMeasurementIsRefused() {
        PipelineModels.PipelineRecord malformed = new PipelineModels.PipelineRecord(
                java.util.Map.of("distance", "not-a-number", "hours", 2.0));
        java.util.List<PipelineModels.PipelineRecord> records = java.util.List.of(malformed);
        SourceReader customReader = new SourceReader() {
            @Override
            public java.util.List<PipelineModels.PipelineRecord> read(PipelineModels.SourceKind kind) {
                return records;
            }
        };
        BatchPipeline customPipeline = new BatchPipeline(customReader, transform, writer);

        PipelineModels.BatchResult result = customPipeline.run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.FILE,
                PipelineModels.SinkKind.FILE,
                "distance",
                "hours"));

        assertEquals(PipelineModels.JobStatus.FAILED, result.status());
        assertEquals(1, result.readCount());
        assertEquals(0, result.writeCount());
        assertEquals(0, writer.lastWritten().size());
    }

    @Test
    @DisplayName("JDBC et API gardent leurs chemins de source sans modifier le contrat")
    void otherSources() {
        PipelineModels.BatchResult jdbc = pipeline.run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.JDBC,
                PipelineModels.SinkKind.FILE,
                "distance",
                "hours"));

        assertEquals(PipelineModels.JobStatus.COMPLETED, jdbc.status());
        assertEquals(1, jdbc.readCount());
        assertEquals(1, jdbc.writeCount());

        PipelineModels.BatchResult api = pipeline.run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.API,
                PipelineModels.SinkKind.FILE,
                "distance",
                "hours"));

        assertEquals(PipelineModels.JobStatus.COMPLETED, api.status());
        assertEquals(1, api.readCount());
        assertEquals(1, api.writeCount());
    }
}
