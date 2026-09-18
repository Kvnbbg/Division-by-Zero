package fr.kvnbbg.tdaah.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class NumericParityCorpusTest {

    private final SafeRatioTransform transform = new SafeRatioTransform();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void sharedCorpusMatchesJavaPipelineContract() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/numeric-parity.json")) {
            if (input == null) {
                throw new IllegalStateException("numeric-parity.json is missing from test resources");
            }

            JsonNode root = mapper.readTree(input);
            for (JsonNode c : root.path("cases")) {
                String id = c.path("id").asText();
                String expected = c.path("expected").asText();
                Object numerator = jsonNumber(c.path("numerator"));
                Object denominator = jsonNumber(c.path("denominator"));
                var record = new PipelineModels.PipelineRecord(
                        java.util.Map.of("numerator", numerator, "denominator", denominator));

                switch (expected) {
                    case "finite" -> {
                        Object actual = transform.apply(record, "numerator", "denominator")
                                .orElseThrow()
                                .fields()
                                .get("ratio");
                        assertEquals(
                                c.path("value").asDouble(),
                                ((Number) actual).doubleValue(),
                                Math.max(1e-12, Math.abs(c.path("value").asDouble()) * 1e-12),
                                id);
                    }
                    case "zero_division" -> assertThrows(
                            ZeroDivisionMeasurementException.class,
                            () -> transform.apply(record, "numerator", "denominator"),
                            id);
                    case "non_finite_operand", "non_finite_result" -> assertThrows(
                            NonFiniteMeasurementException.class,
                            () -> transform.apply(record, "numerator", "denominator"),
                            id);
                    default -> throw new AssertionError("Unknown parity expectation: " + expected);
                }
            }
        }
    }

    private static Object jsonNumber(JsonNode node) {
        if (node.isNumber()) {
            return node.doubleValue();
        }
        return node.asText();
    }
}
