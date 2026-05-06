package org.bahmni.module.bahmnicore.properties;

import org.junit.After;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class OdooConfigPropertiesTest {

    @After
    public void tearDown() {
        // Reset properties to null after each test via initialize
        OdooConfigProperties.initialize(null);
    }

    @Test
    public void initialize_shouldSetPropertiesAndMakeThemAvailableViaGetProperty() {
        Properties props = new Properties();
        props.setProperty("odoo.base_url", "http://odoo:8069");
        props.setProperty("odoo.database", "testdb");

        OdooConfigProperties.initialize(props);

        assertEquals("http://odoo:8069", OdooConfigProperties.getProperty("odoo.base_url"));
        assertEquals("testdb", OdooConfigProperties.getProperty("odoo.database"));
    }

    @Test
    public void getProperty_shouldReturnNullForMissingKey() {
        Properties props = new Properties();
        props.setProperty("odoo.base_url", "http://odoo:8069");

        OdooConfigProperties.initialize(props);

        assertNull(OdooConfigProperties.getProperty("non.existent.key"));
    }

    @Test
    public void getProperty_shouldReturnNullWhenPropertiesNotInitializedAndFileNotFound() {
        // properties is null, getProperty will call load() which won't find the file
        // In test context, OpenmrsUtil.getApplicationDataDirectory() may throw or return a path
        // where odoo-config.properties doesn't exist, so properties stays null
        OdooConfigProperties.initialize(null);

        // This will attempt to call load(), which will either set properties to null
        // (file not found) or throw. Either way, for a missing key it should handle gracefully.
        String result = OdooConfigProperties.getProperty("odoo.base_url");
        // Result is null because properties file doesn't exist in test environment
        assertNull(result);
    }

    @Test
    public void initialize_shouldOverwritePreviousProperties() {
        Properties firstProps = new Properties();
        firstProps.setProperty("odoo.database", "first_db");
        OdooConfigProperties.initialize(firstProps);

        assertEquals("first_db", OdooConfigProperties.getProperty("odoo.database"));

        Properties secondProps = new Properties();
        secondProps.setProperty("odoo.database", "second_db");
        OdooConfigProperties.initialize(secondProps);

        assertEquals("second_db", OdooConfigProperties.getProperty("odoo.database"));
    }

    @Test
    public void getProperty_shouldReturnValueForExistingKey() {
        Properties props = new Properties();
        props.setProperty("odoo.username", "admin");
        props.setProperty("odoo.password", "secret");

        OdooConfigProperties.initialize(props);

        assertEquals("admin", OdooConfigProperties.getProperty("odoo.username"));
        assertEquals("secret", OdooConfigProperties.getProperty("odoo.password"));
    }
}
