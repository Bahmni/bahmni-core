package org.bahmni.module.bahmnicore.util;

import org.bahmni.module.bahmnicore.properties.OdooConfigProperties;

/**
 * Builds Odoo API URLs from base URL and endpoint paths.
 */
public class OdooUrlBuilder {

    private static final String BASE_URL = OdooConfigProperties.getProperty("odoo.base_url");
    private static final String SESSION_AUTHENTICATE_API = "/web/session/authenticate";
    private static final String GET_AVAILABLE_STOCKS_API = "/api/get-available-stocks";

    private OdooUrlBuilder() {
        // Utility class — prevent instantiation
    }

    public static String buildAuthenticationUrl() {
        return BASE_URL + SESSION_AUTHENTICATE_API;
    }

    public static String buildAvailableStocksUrl(String productUuid, String locationUuid) {
        String url = BASE_URL + GET_AVAILABLE_STOCKS_API + "?product_uuid=" + productUuid;
        if (locationUuid != null && !locationUuid.trim().isEmpty()) {
            url += "&location_uuid=" + locationUuid;
        }
        return url;
    }
}
