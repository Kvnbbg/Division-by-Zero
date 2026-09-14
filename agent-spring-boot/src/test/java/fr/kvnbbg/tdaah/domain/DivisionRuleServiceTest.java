package fr.kvnbbg.tdaah.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DivisionRuleServiceTest {

    private final DivisionRuleService service = new DivisionRuleService();

    @Test
    void returnsRoundedAnswerForFiniteNonZeroDenominator() {
        DivisionOutcome outcome = service.resolve(new BigDecimal("10"), new BigDecimal("4"));

        assertEquals(DivisionStatus.ANSWER, outcome.status());
        assertEquals(new BigDecimal("2.500000"), outcome.value());
        assertEquals("MATH_ANSWER", outcome.gameplayEvent());
    }

    @Test
    void returnsUndefinedForNonZeroNumeratorAndZeroDenominator() {
        DivisionOutcome outcome = service.resolve(BigDecimal.ONE, BigDecimal.ZERO);

        assertEquals(DivisionStatus.UNDEFINED, outcome.status());
        assertNull(outcome.value());
        assertEquals("NULL_RIFT_UNDEFINED", outcome.gameplayEvent());
    }

    @Test
    void returnsIndeterminateForZeroOverZero() {
        DivisionOutcome outcome = service.resolve(BigDecimal.ZERO, BigDecimal.ZERO);

        assertEquals(DivisionStatus.INDETERMINATE, outcome.status());
        assertNull(outcome.value());
        assertEquals("NULL_RIFT_INDETERMINATE", outcome.gameplayEvent());
    }
}
