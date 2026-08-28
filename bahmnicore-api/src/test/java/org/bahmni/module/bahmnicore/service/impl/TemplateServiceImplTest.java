package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.client.TemplateServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceImplTest {

    @Mock
    private TemplateServiceClient templateServiceClient;

    private TemplateServiceImpl templateService;

    @Before
    public void setUp() {
        templateService = new TemplateServiceImpl(templateServiceClient);
    }

    @Test
    public void rendersByDelegatingToClient() {
        HttpHeaders headers = new HttpHeaders();
        String body = "{\"templateId\":\"abc\"}";
        ResponseEntity<String> expected = new ResponseEntity<>("rendered", HttpStatus.OK);
        when(templateServiceClient.render(eq(headers), eq(body))).thenReturn(expected);

        ResponseEntity<String> result = templateService.render(headers, body);

        assertEquals(expected, result);
        verify(templateServiceClient).render(eq(headers), eq(body));
    }
}
