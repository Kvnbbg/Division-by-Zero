package fr.kvnbbg.tdaah.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.kvnbbg.tdaah.domain.DivisionOutcome;
import fr.kvnbbg.tdaah.domain.DivisionRuleService;
import fr.kvnbbg.tdaah.domain.DivisionStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DivisionController.class)
class DivisionControllerWebTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DivisionRuleService rules;

    @Test
    void returns422ForUndefinedDivision() throws Exception {
        given(rules.resolve(BigDecimal.ONE, BigDecimal.ZERO))
            .willReturn(new DivisionOutcome(
                DivisionStatus.UNDEFINED,
                null,
                "NULL_RIFT_UNDEFINED"
            ));

        mvc.perform(post("/v1/exercise-attempts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"numerator\":1,\"denominator\":0}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value("UNDEFINED"))
            .andExpect(jsonPath("$.value").doesNotExist())
            .andExpect(jsonPath("$.gameplayEvent").value("NULL_RIFT_UNDEFINED"));
    }

    @Test
    void returns200ForNormalDivision() throws Exception {
        given(rules.resolve(new BigDecimal("10"), new BigDecimal("4")))
            .willReturn(new DivisionOutcome(
                DivisionStatus.ANSWER,
                new BigDecimal("2.500000"),
                "MATH_ANSWER"
            ));

        mvc.perform(post("/v1/exercise-attempts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"numerator\":10,\"denominator\":4}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ANSWER"))
            .andExpect(jsonPath("$.value").value(2.5))
            .andExpect(jsonPath("$.gameplayEvent").value("MATH_ANSWER"));
    }
}
