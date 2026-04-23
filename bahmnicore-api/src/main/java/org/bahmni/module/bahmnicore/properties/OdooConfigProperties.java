package org.bahmni.module.bahmnicore.properties;

/**
 * Provides Odoo configuration properties.
 * Currently uses hardcoded values for development.
 *
 * TODO: Replace hardcoded values with Docker secrets / env variables / properties file loading.
 */
public class OdooConfigProperties {

    private static final String ODOO_DATABASE = "tempdbname";
    private static final String ODOO_USERNAME = "tempusername";
    private static final String ODOO_PASSWORD = "temppassword";
    private static final String ODOO_BASE_URL = "http://odoo:8069";

    public static String getProperty(String key) {
        switch (key) {
            case "odoo.database":
                return ODOO_DATABASE;
            case "odoo.username":
                return ODOO_USERNAME;
            case "odoo.password":
                return ODOO_PASSWORD;
            case "odoo.base_url":
                return ODOO_BASE_URL;
            default:
                return null;
        }
    }
}
