package fr.kvnbbg.tdaah.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/agent")
public class AgentController {

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> tools() {
        return ResponseEntity.ok(Map.of(
                "author", "Kevin Marville",
                "runtime", "spring-boot",
                "llm", false,
                "tools", List.of(
                        "convert_measurement",
                        "evaluate_expression",
                        "list_units",
                        "explain_dimension",
                        "refuse_zero_division"
                )
        ));
    }
}
