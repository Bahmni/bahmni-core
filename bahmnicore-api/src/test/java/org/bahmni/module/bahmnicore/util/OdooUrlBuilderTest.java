package org.bahmni.module.bahmnicore.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OdooUrlBuilderTest {

    @Test
    public void buildAuthenticationUrl_shouldReturnUrlWithSessionAuthenticateEndpoint() {
        String url = OdooUrlBuilder.buildAuthenticationUrl();

        assertTrue(url.endsWith("/web/session/authenticate"));
        assertFalse(url.isEmpty());
    }

    @Test
    public void buildAvailableStocksUrl_shouldIncludeproduct_uuidParam() {
        String product_uuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(product_uuid, null);

        assertTrue(url.contains("/api/get-available-stocks"));
        assertTrue(url.contains("product_uuid=" + product_uuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldIncludeBothParamsWhenlocation_uuidProvided() {
        String product_uuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";
        String location_uuid = "7672b695-1872-40de-9ae8-a2bb38038208";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(product_uuid, location_uuid);

        assertTrue(url.contains("product_uuid=" + product_uuid));
        assertTrue(url.contains("location_uuid=" + location_uuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitlocation_uuidWhenNull() {
        String product_uuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(product_uuid, null);

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + product_uuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitlocation_uuidWhenBlankString() {
        String product_uuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(product_uuid, "   ");

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + product_uuid));
    }

    @Test
    public void buildAvailableStocksUrl_shouldOmitlocation_uuidWhenEmptyString() {
        String product_uuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

        String url = OdooUrlBuilder.buildAvailableStocksUrl(product_uuid, "");

        assertFalse(url.contains("location_uuid"));
        assertTrue(url.contains("product_uuid=" + product_uuid));
    }
}
