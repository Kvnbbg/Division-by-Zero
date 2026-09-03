package fr.kvnbbg.tdaah.pipeline;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BatchPipeline {

    private final SourceReader reader;
    private final SafeRatioTransform transform;
    private final SinkWriter writer;

    public BatchPipeline(SourceReader reader, SafeRatioTransform transform, SinkWriter writer) {
        this.reader = reader;
        this.transform = transform;
        this.writer = writer;
    }

    public PipelineModels.BatchResult run(PipelineModels.RunRequest request) {
        Instant start = Instant.now();
        String num = request.numeratorField() == null ? "distance" : request.numeratorField();
        String den = request.denominatorField() == null ? "hours" : request.denominatorField();
        List<PipelineModels.PipelineRecord> incoming = reader.read(request.source());
        List<PipelineModels.PipelineRecord> outgoing = new ArrayList<>();
        int refusals = 0;
        for (PipelineModels.PipelineRecord rec : incoming) {
            var out = transform.applyOrSkip(rec, num, den);
            if (out.isPresent()) {
                outgoing.add(out.get());
            } else {
                refusals++;
            }
        }
        int written = writer.write(request.sink(), outgoing);
        Instant end = Instant.now();
        boolean failed = written == 0;
        return new PipelineModels.BatchResult(
                start,
                end,
                failed ? PipelineModels.JobStatus.FAILED : PipelineModels.JobStatus.COMPLETED,
                incoming.size(),
                written,
                "batch terminated after completion; zero-division refusals=" + refusals,
                outgoing.stream().limit(5).toList());
    }
}
