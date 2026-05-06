package org.bahmni.module.bahmnicore.properties;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OdooConfigPropertiesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public void tearDown() {
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

    @Test
    public void load_shouldLoadPropertiesFromFile() throws Exception {
        File tempDir = tempFolder.newFolder();
        File propsFile = new File(tempDir, "odoo-config.properties");
        Properties testProps = new Properties();
        testProps.setProperty("odoo.base_url", "http://odoo:8069");
        testProps.setProperty("odoo.database", "testdb");
        testProps.setProperty("odoo.username", "admin");
        try (FileOutputStream fos = new FileOutputStream(propsFile)) {
            testProps.store(fos, null);
        }

        OdooConfigProperties.initialize(null);
        OdooConfigProperties.load(tempDir.getAbsolutePath());

        assertEquals("http://odoo:8069", OdooConfigProperties.getProperty("odoo.base_url"));
        assertEquals("testdb", OdooConfigProperties.getProperty("odoo.database"));
        assertEquals("admin", OdooConfigProperties.getProperty("odoo.username"));
    }

    @Test
    public void load_shouldReturnEarlyWhenFileNotFound() {
        OdooConfigProperties.initialize(null);
        OdooConfigProperties.load("/non/existent/path");

        // Properties should remain null since file was not found
        assertNull(OdooConfigProperties.getProperty("odoo.base_url"));
    }

    @Test
    public void load_shouldLoadMultiplePropertiesFromFile() throws Exception {
        File tempDir = tempFolder.newFolder();
        File propsFile = new File(tempDir, "odoo-config.properties");
        Properties testProps = new Properties();
        testProps.setProperty("odoo.base_url", "http://localhost:8069");
        testProps.setProperty("odoo.database", "production");
        testProps.setProperty("odoo.username", "user");
        testProps.setProperty("odoo.password", "pass123");
        try (FileOutputStream fos = new FileOutputStream(propsFile)) {
            testProps.store(fos, null);
        }

        OdooConfigProperties.initialize(null);
        OdooConfigProperties.load(tempDir.getAbsolutePath());

        assertEquals("http://localhost:8069", OdooConfigProperties.getProperty("odoo.base_url"));
        assertEquals("production", OdooConfigProperties.getProperty("odoo.database"));
        assertEquals("user", OdooConfigProperties.getProperty("odoo.username"));
        assertEquals("pass123", OdooConfigProperties.getProperty("odoo.password"));
    }

    @Test
    public void getProperty_shouldReturnNullForMissingKeyAfterLoadFromFile() throws Exception {
        File tempDir = tempFolder.newFolder();
        File propsFile = new File(tempDir, "odoo-config.properties");
        Properties testProps = new Properties();
        testProps.setProperty("odoo.base_url", "http://odoo:8069");
        try (FileOutputStream fos = new FileOutputStream(propsFile)) {
            testProps.store(fos, null);
        }

        OdooConfigProperties.initialize(null);
        OdooConfigProperties.load(tempDir.getAbsolutePath());

        assertNotNull(OdooConfigProperties.getProperty("odoo.base_url"));
        assertNull(OdooConfigProperties.getProperty("non.existent.key"));
    }
}
