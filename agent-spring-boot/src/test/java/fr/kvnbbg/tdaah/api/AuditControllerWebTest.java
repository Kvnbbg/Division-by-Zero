package fr.kvnbbg.tdaah.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.kvnbbg.tdaah.audit.AuditEvent;
import fr.kvnbbg.tdaah.audit.AuditService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuditController.class)
@Import({ApiExceptionHandler.class, CorrelationIdFilter.class})
class AuditControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuditService audit;

    @Test
    @DisplayName("les événements récents respectent la limite demandée")
    void recentEvents() throws Exception {
        AuditEvent event = new AuditEvent(
                Instant.parse("2026-09-18T08:00:00Z"),
                "tester",
                "division",
                "ticket-1",
                "exercise-1",
                true,
                "ok");
        when(audit.recent(2)).thenReturn(List.of(event));

        mvc.perform(get("/v1/audit/events")
                        .param("limit", "2")
                        .header("X-Correlation-Id", "audit-2"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", "audit-2"))
                .andExpect(jsonPath("$.mode").value("ticket"))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.events[0].actor").value("tester"))
                .andExpect(jsonPath("$.events[0].success").value(true));

        verify(audit).recent(2);
    }

    @Test
    @DisplayName("une limite invalide reste une erreur de paramètre stable")
    void invalidLimit() throws Exception {
        mvc.perform(get("/v1/audit/events")
                        .param("limit", "not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("invalid_parameter"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
