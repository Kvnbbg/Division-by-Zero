package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.domain.DivisionOutcome;
import fr.kvnbbg.tdaah.domain.DivisionRuleService;
import fr.kvnbbg.tdaah.domain.DivisionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class DivisionController {

    private final DivisionRuleService rules;

    public DivisionController(DivisionRuleService rules) {
        this.rules = rules;
    }

    public record DivisionRequest(
        @NotNull BigDecimal numerator,
        @NotNull BigDecimal denominator
    ) {}

    public record DivisionResponse(
        DivisionStatus status,
        BigDecimal value,
        String gameplayEvent
    ) {}

    @PostMapping("/exercise-attempts")
    public ResponseEntity<DivisionResponse> attempt(@Valid @RequestBody DivisionRequest request) {
        DivisionOutcome outcome = rules.resolve(request.numerator(), request.denominator());
        HttpStatus http = outcome.status() == DivisionStatus.ANSWER
            ? HttpStatus.OK
            : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(http).body(
            new DivisionResponse(outcome.status(), outcome.value(), outcome.gameplayEvent())
        );
    }
}
