package org.bahmni.module.bahmnicore.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class OdooConfigProperties {

    private static Properties properties;

    private static final Log log = LogFactory.getLog(OdooConfigProperties.class);

    private OdooConfigProperties() {}

    public static void load() {
        String propertyFilePath = new File(OpenmrsUtil.getApplicationDataDirectory(), "odoo-config.properties")
                .getAbsolutePath();
        File propertyFile = new File(propertyFilePath);

        if (!propertyFile.exists()) {
            log.warn(String.format("Property file not found: %s", propertyFilePath));
            return;
        }
        log.info(String.format("Reading odoo config properties from: %s", propertyFilePath));
        try (FileInputStream fis = new FileInputStream(propertyFile)) {
            properties = new Properties(System.getProperties());
            properties.load(fis);
        }
        catch (IOException e) {
            throw new OdooApiException("Failed to load Odoo config properties", e);
        }
    }

    public static String getProperty(String key) {
        if (properties == null) {
            load();
        }
        return properties != null ? properties.getProperty(key) : null;
    }

    public static void initialize(Properties props) {
        properties = props;
    }
}
