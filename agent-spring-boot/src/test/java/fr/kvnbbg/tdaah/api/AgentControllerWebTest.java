package fr.kvnbbg.tdaah.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AgentController.class)
@Import(CorrelationIdFilter.class)
class AgentControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("le registre agent expose les outils sans exécution")
    void toolsContract() throws Exception {
        mvc.perform(get("/v1/agent/tools").header("X-Correlation-Id", "agent-tools"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "agent-tools"))
                .andExpect(jsonPath("$.runtime").value("spring-boot"))
                .andExpect(jsonPath("$.llm").value(false))
                .andExpect(jsonPath("$.tools").isArray())
                .andExpect(jsonPath("$.tools[0]").value("convert_measurement"))
                .andExpect(jsonPath("$.tools[4]").value("refuse_zero_division"));
    }
}
