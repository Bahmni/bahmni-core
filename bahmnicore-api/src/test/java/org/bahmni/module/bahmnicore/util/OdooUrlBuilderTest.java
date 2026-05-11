package org.bahmni.module.bahmnicore.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OdooUrlBuilderTest {

    @Test
    public void buildAuthenticationUrl_shouldReturnUrlWithSessionAuthenticateEndpoint() {
        String url = OdooUrlBuilder.buildAuthenticationUrl();

        assertNotNull(url);
        assertTrue(url.contains("/web/session/authenticate"));
    }

    @Test
    public void buildAvailableStocksUrl_shouldContainGetAvailableStocksEndpoint() {
        String url = OdooUrlBuilder.buildAvailableStocksUrl("test-uuid", null);

        assertTrue(url.contains("/api/get-available-stocks"));
    }

    @Test
    public void buildAvailableStocksUrl_shouldIncludeProductUuidParam() {
        String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, null);

        assertTrue(url.contains("product_uuid=" + productUuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldIncludeBothParamsWhenLocationUuidProvided() {
        String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";
        String locationUuid = "7672b695-1872-40de-9ae8-a2bb38038208";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, locationUuid);

        assertTrue(url.contains("product_uuid=" + productUuid));
        assertTrue(url.contains("location_uuid=" + locationUuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitLocationUuidWhenNull() {
        String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, null);

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + productUuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitLocationUuidWhenEmpty() {
        String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, "");

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + productUuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitLocationUuidWhenBlank() {
        String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, "   ");

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + productUuid));
    }
}
