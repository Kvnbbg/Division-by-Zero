package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.pipeline.BatchPipeline;
import fr.kvnbbg.tdaah.pipeline.PipelineModels;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/pipeline")
public class PipelineController {

    private final BatchPipeline pipeline;

    public PipelineController(BatchPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @GetMapping("/kinds")
    public Map<String, Object> kinds() {
        return Map.of(
                "author", "Kevin Marville",
                "runtime", "spring-boot",
                "lifeCycle", "short-lived-batch",
                "sources", List.of("FILE", "JDBC", "API"),
                "sinks", List.of("FILE", "JDBC", "API"),
                "notes", "Batch jobs start on schedule or POST /v1/pipeline/run and terminate after completion.");
    }

    @PostMapping("/run")
    public ResponseEntity<PipelineModels.BatchResult> run(@RequestBody(required = false) PipelineModels.RunRequest request) {
        PipelineModels.RunRequest safe = request == null
                ? new PipelineModels.RunRequest(
                        PipelineModels.SourceKind.FILE,
                        PipelineModels.SinkKind.FILE,
                        "distance",
                        "hours")
                : request;
        return ResponseEntity.ok(pipeline.run(safe));
    }
}
