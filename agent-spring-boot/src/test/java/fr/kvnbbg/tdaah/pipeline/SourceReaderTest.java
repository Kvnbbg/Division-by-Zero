package fr.kvnbbg.tdaah.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SourceReaderTest {

    private final SourceReader reader = new SourceReader();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void sourceDataMatchesCanonicalFixture() throws Exception {
        try (InputStream input =
                getClass().getResourceAsStream("/pipeline-source-fixture.json")) {
            Map<String, List<Map<String, Double>>> fixture =
                    mapper.readValue(input, new TypeReference<>() {});

            for (PipelineModels.SourceKind kind : PipelineModels.SourceKind.values()) {
                List<Map<String, Double>> expected = fixture.get(kind.name());
                List<PipelineModels.PipelineRecord> actual = reader.read(kind);

                assertEquals(expected.size(), actual.size(), kind.name());
                for (int i = 0; i < expected.size(); i++) {
                    assertEquals(expected.get(i), actual.get(i).fields(), kind.name() + "[" + i + "]");
                }
            }
        }
    }
}
