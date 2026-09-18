package fr.kvnbbg.tdaah.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.kvnbbg.tdaah.pipeline.BatchPipeline;
import fr.kvnbbg.tdaah.pipeline.PipelineModels;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PipelineController.class)
@Import({ApiExceptionHandler.class, CorrelationIdFilter.class})
class PipelineControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BatchPipeline pipeline;

    @Test
    @DisplayName("les kinds exposent le contrat public du pipeline")
    void kinds() throws Exception {
        mvc.perform(get("/v1/pipeline/kinds")
                        .header("X-Correlation-Id", "pipeline-kinds"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "pipeline-kinds"))
                .andExpect(jsonPath("$.runtime").value("spring-boot"))
                .andExpect(jsonPath("$.lifeCycle").value("short-lived-batch"))
                .andExpect(jsonPath("$.sources[0]").value("FILE"))
                .andExpect(jsonPath("$.sinks[0]").value("FILE"));
    }

    @Test
    @DisplayName("une requête valide est transmise intacte au batch")
    void runValide() throws Exception {
        PipelineModels.BatchResult result = result();

        when(pipeline.run(any(PipelineModels.RunRequest.class))).thenReturn(result);

        mvc.perform(post("/v1/pipeline/run")
                        .header("X-Correlation-Id", "pipeline-42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"source":"JDBC","sink":"API","numeratorField":"distance","denominatorField":"hours"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "pipeline-42"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.readCount").value(1))
                .andExpect(jsonPath("$.writeCount").value(1));

        verify(pipeline).run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.JDBC,
                PipelineModels.SinkKind.API,
                "distance",
                "hours"));
    }

    @Test
    @DisplayName("un corps absent conserve les valeurs par défaut historiques")
    void runSansCorps() throws Exception {
        when(pipeline.run(any(PipelineModels.RunRequest.class))).thenReturn(result());

        mvc.perform(post("/v1/pipeline/run")
                        .header("X-Correlation-Id", "pipeline-defaults"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "pipeline-defaults"));

        verify(pipeline).run(new PipelineModels.RunRequest(
                PipelineModels.SourceKind.FILE,
                PipelineModels.SinkKind.FILE,
                "distance",
                "hours"));
    }

    @Test
    @DisplayName("un JSON malformé reste une erreur HTTP stable")
    void runCorpsIllisible() throws Exception {
        mvc.perform(post("/v1/pipeline/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ pipeline"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("malformed_body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("une valeur d'enum inconnue est rejetée sans exécuter le batch")
    void runEnumInvalide() throws Exception {
        mvc.perform(post("/v1/pipeline/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"source":"UNKNOWN","sink":"FILE","numeratorField":"distance","denominatorField":"hours"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("malformed_body"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("une source nulle est rejetée avant le batch")
    void runSourceNulle() throws Exception {
        mvc.perform(post("/v1/pipeline/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"source":null,"sink":"FILE","numeratorField":"distance","denominatorField":"hours"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("validation"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("source")));
    }

    private static PipelineModels.BatchResult result() {
        Instant start = Instant.parse("2026-09-18T08:00:00Z");
        Instant end = Instant.parse("2026-09-18T08:00:01Z");
        return new PipelineModels.BatchResult(
                start,
                end,
                PipelineModels.JobStatus.COMPLETED,
                1,
                1,
                "ok",
                List.of());
    }
}
