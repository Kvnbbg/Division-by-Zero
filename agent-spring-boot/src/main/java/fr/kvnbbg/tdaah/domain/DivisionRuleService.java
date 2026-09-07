package fr.kvnbbg.tdaah.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Unique point de vérité pour a / b. Appelé par REST, GraphQL et AMQP.
 */
@Service
public class DivisionRuleService {

    public DivisionOutcome resolve(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) != 0) {
            return new DivisionOutcome(
                DivisionStatus.ANSWER,
                numerator.divide(denominator, 6, RoundingMode.HALF_UP),
                "MATH_ANSWER"
            );
        }
        if (numerator.compareTo(BigDecimal.ZERO) == 0) {
            return new DivisionOutcome(
                DivisionStatus.INDETERMINATE,
                null,
                "NULL_RIFT_INDETERMINATE"
            );
        }
        return new DivisionOutcome(
            DivisionStatus.UNDEFINED,
            null,
            "NULL_RIFT_UNDEFINED"
        );
    }
}
