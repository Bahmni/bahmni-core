package org.bahmni.module.bahmnicore.web.filter;

import org.bahmni.module.bahmnicore.web.v1_0.client.TemplateServiceClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Intercepts requests to /ws/rest/v1/bahmnicore/template/** before Spring MVC routing
 * and proxies them to the configured template service, forwarding the OpenMRS session
 * so the template service can call back into OpenMRS on behalf of the user.
 */
public class TemplateProxyFilter implements Filter {

    static final String PATH_MARKER = "/bahmnicore/template";
    static final String SESSION_ID_HEADER = "X-OpenMRS-Session-Id";
    static final String AUTH_HEADER = "X-OpenMRS-Authorization";

    private FilterConfig filterConfig;
    private volatile TemplateServiceClient templateServiceClient;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;

        int markerIdx = request.getRequestURI().indexOf(PATH_MARKER);
        if (markerIdx < 0) {
            chain.doFilter(req, res);
            return;
        }

        HttpServletResponse response = (HttpServletResponse) res;
        try {
            String subPath = request.getRequestURI().substring(markerIdx + PATH_MARKER.length());
            String body = hasBody(request.getMethod()) ? readBody(request) : null;

            ResponseEntity<String> proxyResponse = client().forward(
                    HttpMethod.valueOf(request.getMethod()),
                    subPath,
                    request.getQueryString(),
                    buildSessionHeaders(request),
                    body);

            writeResponse(proxyResponse, response);
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY,
                        "Template service unavailable: " + e.getMessage());
            }
        }
    }

    HttpHeaders buildSessionHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JSESSIONID".equals(c.getName())) {
                    headers.set(SESSION_ID_HEADER, c.getValue());
                    break;
                }
            }
        }

        // Forward Basic Auth for non-browser clients
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) {
            headers.set(AUTH_HEADER, auth);
        }

        String contentType = request.getContentType();
        if (contentType != null) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        }

        return headers;
    }

    private boolean hasBody(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void writeResponse(ResponseEntity<String> proxyResponse, HttpServletResponse response)
            throws IOException {
        response.setStatus(proxyResponse.getStatusCodeValue());

        MediaType contentType = proxyResponse.getHeaders().getContentType();
        if (contentType != null) {
            response.setContentType(contentType.toString());
        }

        String body = proxyResponse.getBody();
        if (body != null) {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } else {
            response.setContentLength(0);
        }
    }

    private TemplateServiceClient client() {
        if (templateServiceClient == null) {
            synchronized (this) {
                if (templateServiceClient == null) {
                    WebApplicationContext ctx = WebApplicationContextUtils
                            .getRequiredWebApplicationContext(filterConfig.getServletContext());
                    templateServiceClient = ctx.getBean(TemplateServiceClient.class);
                }
            }
        }
        return templateServiceClient;
    }

    void setTemplateServiceClient(TemplateServiceClient client) {
        this.templateServiceClient = client;
    }

    @Override
    public void destroy() {
        templateServiceClient = null;
    }
}
