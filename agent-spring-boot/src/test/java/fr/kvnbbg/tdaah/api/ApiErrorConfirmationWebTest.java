package fr.kvnbbg.tdaah.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import fr.kvnbbg.tdaah.audit.AuditService;

@WebMvcTest(AuditController.class)
@Import({ApiExceptionHandler.class, CorrelationIdFilter.class})
class ApiErrorConfirmationWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuditService audit;

    @Test
    @DisplayName("une erreur API confirme le même identifiant de corrélation dans le header et le corps")
    void errorConfirmsCorrelationId() throws Exception {
        mvc.perform(get("/v1/audit/events")
                        .param("limit", "not-a-number")
                        .header("X-Correlation-Id", "audit-error-1"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "audit-error-1"))
                .andExpect(jsonPath("$.error").value("invalid_parameter"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.correlationId").value("audit-error-1"));
    }

    @Test
    @DisplayName("un corps JSON illisible conserve aussi la corrélation")
    void malformedBodyConfirmsCorrelationId() throws Exception {
        mvc.perform(post("/v1/pipeline/run")
                        .contentType("application/json")
                        .content("{not-json}")
                        .header("X-Correlation-Id", "audit-malformed"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("X-Correlation-Id", "audit-malformed"))
                .andExpect(jsonPath("$.correlationId").value("audit-malformed"));
    }
}
