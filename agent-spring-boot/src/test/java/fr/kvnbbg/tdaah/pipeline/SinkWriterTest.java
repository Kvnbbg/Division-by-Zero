package fr.kvnbbg.tdaah.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class SinkWriterTest {

    private final SinkWriter writer = new SinkWriter();

    @Test
    void rejectsMissingSink() {
        assertThrows(IllegalArgumentException.class, () -> writer.write(null, List.of()));
    }

    @Test
    void rejectsMissingRecords() {
        assertThrows(
                IllegalArgumentException.class,
                () -> writer.write(PipelineModels.SinkKind.FILE, null));
    }

    @Test
    void storesOnlyTheCurrentBatch() {
        PipelineModels.PipelineRecord record =
                new PipelineModels.PipelineRecord(java.util.Map.of("ratio", 5.0));

        assertEquals(1, writer.write(PipelineModels.SinkKind.FILE, List.of(record)));
        assertEquals(List.of(record), writer.lastWritten());

        assertEquals(0, writer.write(PipelineModels.SinkKind.FILE, List.of()));
        assertEquals(List.of(), writer.lastWritten());
    }
}
