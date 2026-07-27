package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.client.TemplateServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateRenderControllerTest {

    @Mock private TemplateServiceClient templateServiceClient;
    @Mock private HttpServletRequest request;

    private TemplateRenderController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new TemplateRenderController(templateServiceClient);
    }

    @Test
    public void renderForwardsBodyAndReturnsServiceResponse() {
        String body = "{\"templateId\":\"abc\"}";
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn("application/json");
        when(templateServiceClient.render(any(HttpHeaders.class), eq(body)))
                .thenReturn(new ResponseEntity<>("rendered", HttpStatus.OK));

        ResponseEntity<String> result = controller.render(request, body);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("rendered", result.getBody());
        verify(templateServiceClient).render(any(HttpHeaders.class), eq(body));
    }

    @Test
    public void renderPropagatesContentTypeFromServiceResponse() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);
        HttpHeaders serviceHeaders = new HttpHeaders();
        serviceHeaders.setContentType(MediaType.APPLICATION_PDF);
        when(templateServiceClient.render(any(), any()))
                .thenReturn(new ResponseEntity<>("pdf", serviceHeaders, HttpStatus.OK));

        ResponseEntity<String> result = controller.render(request, "{}");

        assertEquals(MediaType.APPLICATION_PDF, result.getHeaders().getContentType());
    }

    @Test
    public void renderOmitsContentTypeWhenServiceResponseHasNone() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);
        when(templateServiceClient.render(any(), any()))
                .thenReturn(new ResponseEntity<>("body", HttpStatus.OK));

        ResponseEntity<String> result = controller.render(request, "{}");

        assertNull(result.getHeaders().getContentType());
    }

    @Test
    public void renderPropagates4xxStatusFromService() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);
        when(templateServiceClient.render(any(), any()))
                .thenReturn(new ResponseEntity<>("not found", HttpStatus.NOT_FOUND));

        ResponseEntity<String> result = controller.render(request, "{}");

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("not found", result.getBody());
    }

    @Test
    public void renderPropagates5xxStatusFromService() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);
        when(templateServiceClient.render(any(), any()))
                .thenReturn(new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = controller.render(request, "{}");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void buildSessionHeadersExtractsJsessionId() {
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("JSESSIONID", "abc123"),
                new Cookie("bahmni.user", "admin")
        });
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertEquals("abc123", headers.getFirst(TemplateRenderController.SESSION_ID_HEADER));
        assertNull(headers.getFirst(TemplateRenderController.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersExtractsAuthorizationHeader() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertNull(headers.getFirst(TemplateRenderController.SESSION_ID_HEADER));
        assertEquals("Basic dXNlcjpwYXNz", headers.getFirst(TemplateRenderController.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersForwardsBothSessionAndAuth() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("JSESSIONID", "session-xyz")});
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertEquals("session-xyz", headers.getFirst(TemplateRenderController.SESSION_ID_HEADER));
        assertEquals("Basic dXNlcjpwYXNz", headers.getFirst(TemplateRenderController.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersForwardsContentType() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn("application/json");

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    public void buildSessionHeadersHandlesNoCookies() {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertNull(headers.getFirst(TemplateRenderController.SESSION_ID_HEADER));
        assertNull(headers.getFirst(TemplateRenderController.AUTH_HEADER));
    }

    @Test
    public void buildSessionHeadersIgnoresNonJsessionIdCookies() {
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("bahmni.user", "admin")});
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(request.getContentType()).thenReturn(null);

        HttpHeaders headers = controller.buildSessionHeaders(request);

        assertNull(headers.getFirst(TemplateRenderController.SESSION_ID_HEADER));
    }
}
