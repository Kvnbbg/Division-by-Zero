package fr.kvnbbg.tdaah.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiErrorContractFixtureTest {

    @Test
    void errorFixtureDefinesStableStatusAndCodes() throws Exception {
        try (InputStream input =
                getClass().getResourceAsStream("/api-error-fixture.json")) {
            Map<String, Map<String, Object>> fixture =
                    new ObjectMapper().readValue(input, new TypeReference<>() {});

            assertNotNull(fixture);
            assertEquals(6, fixture.size());
            assertEquals(400, fixture.get("malformed_body").get("status"));
            assertEquals(422, fixture.get("validation").get("status"));
            assertEquals("zero_division_measurement",
                    fixture.get("zero_division_measurement").get("error"));
            assertEquals("non_finite_measurement",
                    fixture.get("non_finite_measurement").get("error"));
        }
    }
}
