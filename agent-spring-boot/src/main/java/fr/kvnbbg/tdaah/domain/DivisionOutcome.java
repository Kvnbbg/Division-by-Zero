package fr.kvnbbg.tdaah.domain;

import java.math.BigDecimal;

public record DivisionOutcome(
    DivisionStatus status,
    BigDecimal value,
    String gameplayEvent
) {}
