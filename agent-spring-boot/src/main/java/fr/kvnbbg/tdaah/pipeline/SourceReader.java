package fr.kvnbbg.tdaah.pipeline;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SourceReader {

    public List<PipelineModels.PipelineRecord> read(PipelineModels.SourceKind kind) {
        return switch (kind) {
            case FILE -> List.of(
                    rec(10.0, 2.0),
                    rec(5.0, 1.0),
                    rec(3.0, 0.0));
            case JDBC -> List.of(rec(42.0, 6.0));
            case API -> List.of(rec(100.0, 4.0));
        };
    }

    private static PipelineModels.PipelineRecord rec(double distance, double hours) {
        return new PipelineModels.PipelineRecord(
                Map.of("distance", distance, "hours", hours));
    }
}
