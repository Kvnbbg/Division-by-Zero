package fr.kvnbbg.tdaah.security;

import fr.kvnbbg.tdaah.audit.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lightweight API-key gate. Prefer OIDC in production; this is a substrate for labs / agents.
 * Mutating methods require X-Ticket-Id (mode ticket, no thruster).
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HDR_KEY = "X-API-Key";
    public static final String HDR_TICKET = "X-Ticket-Id";

    private final String configuredKey;
    private final boolean authEnabled;
    private final AuditService audit;

    private static final Set<String> OPEN_PREFIXES = Set.of(
            "/actuator/health",
            "/v1/agent/tools",
            "/v1/device/helpers",
            "/v1/pipeline/kinds");

    public ApiKeyAuthFilter(
            @Value("${tdaah.security.api-key:}") String configuredKey,
            @Value("${tdaah.security.enabled:false}") boolean authEnabled,
            AuditService audit) {
        this.configuredKey = configuredKey == null ? "" : configuredKey;
        this.authEnabled = authEnabled && !this.configuredKey.isBlank();
        this.audit = audit;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!authEnabled || isOpen(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = request.getHeader(HDR_KEY);
        if (key == null || !key.equals(configuredKey)) {
            audit.record("unknown", "AUTH_DENIED", null, path, false, "missing or invalid API key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"unauthorized\",\"hint\":\"X-API-Key required\"}");
            return;
        }
        if (isMutating(request.getMethod())) {
            String ticket = request.getHeader(HDR_TICKET);
            if (ticket == null || ticket.isBlank()) {
                audit.record("api-key", "TICKET_REQUIRED", null, path, false, "no thruster without ticket");
                response.setStatus(422);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter()
                        .write(
                                "{\"error\":\"ticket_required\",\"message\":\"Mutating calls need X-Ticket-Id (mode ticket, no thruster)\"}");
                return;
            }
            request.setAttribute("tdaah.ticketId", ticket);
        }
        filterChain.doFilter(request, response);
    }

    private static boolean isOpen(String path) {
        for (String p : OPEN_PREFIXES) {
            if (path.equals(p) || path.startsWith(p + "/")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMutating(String method) {
        return HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.PATCH.matches(method)
                || HttpMethod.DELETE.matches(method);
    }
}
