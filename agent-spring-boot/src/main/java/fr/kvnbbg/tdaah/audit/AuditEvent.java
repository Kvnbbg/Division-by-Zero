package fr.kvnbbg.tdaah.audit;

import java.time.Instant;

public record AuditEvent(
        Instant at,
        String actor,
        String action,
        String ticketId,
        String resource,
        boolean success,
        String detail) {}
