package fr.kvnbbg.tdaah.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void preservesSafeCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "tdaah-2026.09");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("tdaah-2026.09");
    }

    @Test
    void replacesUnsafeCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "bad value");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        String id = response.getHeader(CorrelationIdFilter.HEADER);
        assertThat(id).matches("[0-9a-f-]{36}");
    }

    @Test
    void generatesIdWhenHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (FilterChain) (req, res) -> {});

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
    }
}
