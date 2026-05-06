package org.bahmni.module.bahmnicore.client;

import org.bahmni.webclients.HttpClient;
import org.bahmni.webclients.WebClientsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniOdooClientTest {

    private static final String TEST_URL = "http://odoo:8069/api/get-available-stocks?productUuid=abc";
    private static final String RESPONSE_BODY = "{\"count\":2,\"data\":[]}";

    @Mock
    private HttpClient httpClient;

    @Mock
    private BahmniOdooSessionManager sessionManager;

    private BahmniOdooClient bahmniOdooClient;

    @Before
    public void setUp() {
        bahmniOdooClient = new BahmniOdooClient(httpClient, sessionManager);
    }

    @Test
    public void get_shouldDelegateToHttpClientAndReturnBody() {
        when(httpClient.get(any(URI.class))).thenReturn(RESPONSE_BODY);

        String result = bahmniOdooClient.get(TEST_URL);

        assertEquals(RESPONSE_BODY, result);
        verify(httpClient, times(1)).get(URI.create(TEST_URL));
    }

    @Test
    public void get_shouldPropagateWebClientsExceptionFromHttpClient() {
        when(httpClient.get(any(URI.class)))
                .thenThrow(new WebClientsException(new RuntimeException("Connection refused")));

        try {
            bahmniOdooClient.get(TEST_URL);
            fail("Expected WebClientsException to be thrown");
        } catch (WebClientsException ex) {
            // expected
        }

        verify(httpClient, times(1)).get(URI.create(TEST_URL));
    }

    @Test
    public void constructor_shouldCreateClientWithSessionManager() {
        BahmniOdooClient client = new BahmniOdooClient(new BahmniOdooSessionManager());
        assertNotNull(client);
    }
}
