package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.admin.config.model.BahmniConfig;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;


public class BahmniConfigControllerIT extends BaseIntegrationTest {
    @Before
    public void setUp() throws Exception {
        executeDataSet("configDataSetup.xml");
    }

    @Test
    public void deserializationToJsonOfConfig() throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        BahmniConfig bahmniConfig = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/config", headers, new Parameter("appName", "clinical"), new Parameter("configName", "app.json"))), new TypeReference<BahmniConfig>() {
        });
        assertEquals("app.json", bahmniConfig.getConfigName());
        assertEquals("clinical", bahmniConfig.getAppName());
    }

    @Test
    public void getConfigByPath() throws Exception {
        String bahmniConfig = handle(newGetRequest("/rest/v1/bahmnicore/config/clinical/app.json")).getContentAsString();
        assertFalse(bahmniConfig.isEmpty());
        assertTrue(bahmniConfig.contains("bahmni.registration"));
    }

    @Test
    public void strippedDownJsonOfAllConfigsUnderAnApp() throws Exception {
        HashMap<String, String> headers = new HashMap<>();
        List<BahmniConfig> bahmniConfigs = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/config/all", headers, new Parameter("appName", "clinical"))), new TypeReference<List<BahmniConfig>>() {
        });
        assertEquals(2, bahmniConfigs.size());
        assertNull(bahmniConfigs.get(0).getConfig());
        assertNull(bahmniConfigs.get(1).getConfig());
    }

    @Test
    public void createNewConfig() throws Exception {
        BahmniConfig bahmniConfig = new BahmniConfig();
        bahmniConfig.setConfig("New Config");
        bahmniConfig.setAppName("registration");
        bahmniConfig.setConfigName("app.json");
        BahmniConfig savedConfig = deserialize(handle(newPostRequest("/rest/v1/bahmnicore/config", bahmniConfig)), BahmniConfig.class);
        BahmniConfig getConfig = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/config", new Parameter("appName", "registration"), new Parameter("configName", "app.json"))), BahmniConfig.class);
        assertEquals(savedConfig, getConfig);
        assertNotNull(getConfig.getDateCreated());
        assertEquals("New Config", getConfig.getConfig());
    }

    @Test
    public void updateExistingConfig() throws Exception {
        BahmniConfig getConfig = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/config", new Parameter("appName", "clinical"), new Parameter("configName", "app.json"))), BahmniConfig.class);
        getConfig.setConfig("Updated Config");
        BahmniConfig savedConfig = deserialize(handle(newPutRequest("/rest/v1/bahmnicore/config", getConfig)), BahmniConfig.class);
        getConfig = deserialize(handle(newGetRequest("/rest/v1/bahmnicore/config", new Parameter("appName", "clinical"), new Parameter("configName", "app.json"))), BahmniConfig.class);
        assertEquals(savedConfig, getConfig);
        assertNotNull(getConfig.getDateCreated());
        assertEquals("Updated Config", getConfig.getConfig());
    }
}