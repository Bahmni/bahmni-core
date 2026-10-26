package org.bahmni.module.bahmnicore.web.v1_0.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Pass-through client for the bahmni-ai service. Unlike the template proxy it streams the response
 * (the chat endpoints answer with Server-Sent Events), flushing every chunk to the browser.
 */
@Component
public class AiServiceClient {

    private static final Log log = LogFactory.getLog(AiServiceClient.class);

    private static final String URL_KEY = "ai.service.url";
    private static final String CONNECT_TIMEOUT_KEY = "ai.service.connectTimeoutInMilliseconds";
    private static final String READ_TIMEOUT_KEY = "ai.service.readTimeoutInMilliseconds";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    // Long: a chat turn can include several model and tool calls. The timeout applies between chunks.
    private static final int DEFAULT_READ_TIMEOUT = 120000;

    private final String baseUrl;
    private final int connectTimeout;
    private final int readTimeout;

    public AiServiceClient() {
        this.baseUrl = BahmniCoreProperties.getProperty(URL_KEY);
        this.connectTimeout = parseTimeout(CONNECT_TIMEOUT_KEY, DEFAULT_CONNECT_TIMEOUT);
        this.readTimeout = parseTimeout(READ_TIMEOUT_KEY, DEFAULT_READ_TIMEOUT);
    }

    AiServiceClient(String baseUrl, int connectTimeout, int readTimeout) {
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /** Forwards the request and copies status, content type and body (streamed) to the response. */
    public void forward(String method, String path, Map<String, String> headers, String body,
                        HttpServletResponse response) throws IOException {
        if (StringUtils.isBlank(baseUrl)) {
            log.error("ai.service.url is not configured");
            writeError(response, HttpServletResponse.SC_BAD_GATEWAY, "AI service is not configured");
            return;
        }
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(baseUrl + path).openConnection();
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod(method);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            if (body != null) {
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }
            int status = connection.getResponseCode();
            response.setStatus(status);
            String contentType = connection.getContentType();
            if (contentType != null) {
                response.setContentType(contentType);
            }
            // Keep Apache/Tomcat from holding SSE chunks back.
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("X-Accel-Buffering", "no");
            InputStream in = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (in != null) {
                try (InputStream stream = in) {
                    copyAndFlush(stream, response.getOutputStream());
                }
            }
        } catch (IOException e) {
            if (response.isCommitted()) {
                // Mid-stream failure (service died or the browser went away): nothing more can be sent.
                log.warn("AI service stream ended unexpectedly: " + e.getClass().getSimpleName());
            } else {
                log.error("AI service unreachable at " + baseUrl, e);
                writeError(response, HttpServletResponse.SC_BAD_GATEWAY, "AI service is unavailable");
            }
        } finally {
            if (connection != null) {
                connection.disconnect(); // also cancels the upstream run when the browser disconnects
            }
        }
    }

    private void copyAndFlush(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[2048];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            out.flush();
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"detail\":{\"code\":\"ai_service_unavailable\",\"message\":\"" + message + "\"}}");
    }

    private int parseTimeout(String key, int defaultValue) {
        int value = NumberUtils.toInt(StringUtils.trim(BahmniCoreProperties.getProperty(key)), defaultValue);
        return value > 0 ? value : defaultValue;
    }
}
