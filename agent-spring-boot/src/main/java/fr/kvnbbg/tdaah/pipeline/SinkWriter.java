package fr.kvnbbg.tdaah.pipeline;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SinkWriter {

    private final List<PipelineModels.PipelineRecord> lastWritten = new ArrayList<>();

    public int write(PipelineModels.SinkKind kind, List<PipelineModels.PipelineRecord> records) {
        lastWritten.clear();
        lastWritten.addAll(records);
        return records.size();
    }

    public List<PipelineModels.PipelineRecord> lastWritten() {
        return List.copyOf(lastWritten);
    }
}
