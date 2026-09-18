package fr.kvnbbg.tdaah.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String REQUEST_ATTRIBUTE = CorrelationIdFilter.class.getName() + ".id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String supplied = request.getHeader(HEADER);
        String correlationId = isSafeId(supplied) ? supplied : UUID.randomUUID().toString();

        response.setHeader(HEADER, correlationId);
        request.setAttribute(REQUEST_ATTRIBUTE, correlationId);
        filterChain.doFilter(request, response);
    }

    private static boolean isSafeId(String value) {
        return value != null
                && value.length() <= 128
                && value.matches("[A-Za-z0-9._:-]+");
    }
}
