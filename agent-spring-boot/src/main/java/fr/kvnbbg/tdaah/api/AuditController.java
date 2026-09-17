package fr.kvnbbg.tdaah.api;

import fr.kvnbbg.tdaah.audit.AuditEvent;
import fr.kvnbbg.tdaah.audit.AuditService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audit")
public class AuditController {

    private final AuditService audit;

    public AuditController(AuditService audit) {
        this.audit = audit;
    }

    @GetMapping("/events")
    public Map<String, Object> events(@RequestParam(defaultValue = "50") int limit) {
        List<AuditEvent> recent = audit.recent(limit);
        return Map.of(
                "mode", "ticket",
                "thruster", false,
                "count", recent.size(),
                "events", recent);
    }
}
