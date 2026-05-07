package org.bahmni.module.bahmnicore.web.filter;

import org.bahmni.module.bahmnicore.web.v1_0.client.TemplateServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class TemplateProxyFilterTest {

    @Mock private TemplateServiceClient templateServiceClient;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;
    @Mock private ServletOutputStream outputStream;

    private TemplateProxyFilter filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        filter = new TemplateProxyFilter();
        filter.setTemplateServiceClient(templateServiceClient);
        when(response.getOutputStream()).thenReturn(outputStream);
    }

    @Test
    public void nonTemplateRequestPassesThroughToChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/encounter");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyZeroInteractions(templateServiceClient);
    }

    @Test
    public void templateRequestDoesNotPassToChain() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/reports");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);
        when(templateServiceClient.forward(any(), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        filter.doFilter(request, response, chain);

        verifyZeroInteractions(chain);
    }

    @Test
    public void getRequestForwardedToTemplateService() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/reports");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn("format=pdf");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JSESSIONID", "session-abc")});
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        ResponseEntity<String> proxyResponse = new ResponseEntity<>("<html/>", HttpStatus.OK);
        when(templateServiceClient.forward(eq(HttpMethod.GET), eq("/reports"), eq("format=pdf"),
                any(HttpHeaders.class), isNull(String.class))).thenReturn(proxyResponse);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(200);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(captor.capture());
        assertEquals("<html/>", new String(captor.getValue(), StandardCharsets.UTF_8));
    }

    @Test
    public void postRequestForwardsBody() throws Exception {
        String body = "{\"templateId\":\"abc\"}";
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/render");
        when(request.getMethod()).thenReturn("POST");
        when(request.getQueryString()).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        ResponseEntity<String> proxyResponse = new ResponseEntity<>("rendered", HttpStatus.CREATED);
        when(templateServiceClient.forward(eq(HttpMethod.POST), eq("/render"), isNull(String.class),
                any(HttpHeaders.class), eq(body))).thenReturn(proxyResponse);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(201);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(captor.capture());
        assertEquals("rendered", new String(captor.getValue(), StandardCharsets.UTF_8));
    }

    @Test
    public void nestedPathForwardedCorrectly() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/reports/pdf/monthly");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        ResponseEntity<String> proxyResponse = new ResponseEntity<>("data", HttpStatus.OK);
        when(templateServiceClient.forward(eq(HttpMethod.GET), eq("/reports/pdf/monthly"),
                isNull(String.class), any(HttpHeaders.class), isNull(String.class))).thenReturn(proxyResponse);

        filter.doFilter(request, response, chain);

        verify(templateServiceClient).forward(eq(HttpMethod.GET), eq("/reports/pdf/monthly"),
                isNull(String.class), any(HttpHeaders.class), isNull(String.class));
    }

    @Test
    public void responseContentTypeSetFromProxyResponse() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/pdf");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_PDF);
        ResponseEntity<String> proxyResponse = new ResponseEntity<>("pdf-bytes", responseHeaders, HttpStatus.OK);
        when(templateServiceClient.forward(any(), any(), any(), any(), any())).thenReturn(proxyResponse);

        filter.doFilter(request, response, chain);

        verify(response).setContentType("application/pdf");
    }

    @Test
    public void buildSessionHeadersExtractsJsessionId() {
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("JSESSIONID", "abc123"),
                new Cookie("bahmni.user", "admin")
        });
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = filter.buildSessionHeaders(request);

        assertEquals("abc123", headers.getFirst(TemplateProxyFilter.SESSION_ID_HEADER));
        assertNull(headers.getFirst(TemplateProxyFilter.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersExtractsAuthorizationHeader() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = filter.buildSessionHeaders(request);

        assertNull(headers.getFirst(TemplateProxyFilter.SESSION_ID_HEADER));
        assertEquals("Basic dXNlcjpwYXNz", headers.getFirst(TemplateProxyFilter.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersForwardsBothSessionAndAuth() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JSESSIONID", "session-xyz")});
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = filter.buildSessionHeaders(request);

        assertEquals("session-xyz", headers.getFirst(TemplateProxyFilter.SESSION_ID_HEADER));
        assertEquals("Basic dXNlcjpwYXNz", headers.getFirst(TemplateProxyFilter.AUTH_HEADER));
    }

    @Test
    public void errorStatusFromTemplateServicePassedThrough() throws Exception {
        when(request.getRequestURI()).thenReturn("/openmrs/ws/rest/v1/bahmnicore/template/missing");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        ResponseEntity<String> errorResponse = new ResponseEntity<>("not found", HttpStatus.NOT_FOUND);
        when(templateServiceClient.forward(any(), any(), any(), any(), any())).thenReturn(errorResponse);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(404);
        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(captor.capture());
        assertEquals("not found", new String(captor.getValue(), StandardCharsets.UTF_8));
    }
}
