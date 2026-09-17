package fr.kvnbbg.tdaah.audit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final List<AuditEvent> events = Collections.synchronizedList(new ArrayList<>());

    public AuditEvent record(
            String actor, String action, String ticketId, String resource, boolean success, String detail) {
        AuditEvent e = new AuditEvent(
                Instant.now(),
                actor == null ? "anonymous" : actor,
                action,
                ticketId,
                resource,
                success,
                detail);
        events.add(e);
        return e;
    }

    public List<AuditEvent> recent(int limit) {
        int n = Math.min(Math.max(limit, 1), 200);
        synchronized (events) {
            int from = Math.max(0, events.size() - n);
            return List.copyOf(events.subList(from, events.size()));
        }
    }
}
