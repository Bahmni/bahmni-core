package org.bahmni.module.bahmnicore.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.properties.OdooConfigProperties;

public class OdooUrlBuilder {

    private static final String BASE_URL = OdooConfigProperties.getProperty("odoo.base_url");
    private static final String SESSION_AUTHENTICATE_API = "/web/session/authenticate";
    private static final String GET_AVAILABLE_STOCKS_API = "/api/get-available-stocks";

    private static final Logger logger = LogManager.getLogger(OdooUrlBuilder.class);

    private OdooUrlBuilder() {}

    public static String buildAuthenticationUrl() {
        if (BASE_URL == null || BASE_URL.trim().isEmpty()) {
            logger.info("Base URL is not set in Odoo config properties.");
        }
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
