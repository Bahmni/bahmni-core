package org.bahmni.module.admin.config.dao.impl;

import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.config.dao.BahmniConfigDao;
import org.bahmni.module.admin.config.model.BahmniConfig;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BahmniConfigDaoImplIT extends BaseIntegrationTest {

    @Autowired
    private BahmniConfigDao bahmniConfigDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("configDataSetup.xml");
    }

    @Test
    public void getConfigFromByAppAndConfigName() throws Exception {
        BahmniConfig clinical = bahmniConfigDao.get("clinical", "app.json");
        assertNotNull(clinical);
        assertEquals("clinical", clinical.getAppName());
        assertEquals("app.json", clinical.getConfigName());
        assertEquals("0aa1efd4-6eeb-4cea-bd4b-94af86f24d97", clinical.getUuid());
        assertFalse(StringUtils.isEmpty(clinical.getConfig()));
    }

    @Test
    public void getConfigFromByUuid() throws Exception {
        BahmniConfig clinical = bahmniConfigDao.get("0aa1efd4-6eeb-4cea-bd4b-94af86f24d97");
        assertNotNull(clinical);
        assertEquals("clinical", clinical.getAppName());
        assertEquals("app.json", clinical.getConfigName());
        assertEquals("0aa1efd4-6eeb-4cea-bd4b-94af86f24d97", clinical.getUuid());
        assertFalse(StringUtils.isEmpty(clinical.getConfig()));
    }

    @Test
    public void returnNullIfConfigNotAvailable() throws Exception {
        BahmniConfig clinical = bahmniConfigDao.get("notclinical", "app.json");
        assertNull(clinical);
    }

    @Test
    public void getAllConfigsFor() throws Exception {
        List<BahmniConfig> clinical = bahmniConfigDao.getAllFor("clinical");
        assertEquals(2, clinical.size());
    }

    @Test
    public void insertNewConfig() throws Exception {
        BahmniConfig bahmniConfig = new BahmniConfig();
        bahmniConfig.setConfig("New Config");
        bahmniConfig.setAppName("registration");
        bahmniConfig.setConfigName("app.json");
        bahmniConfig.setCreator(Context.getUserContext().getAuthenticatedUser());
        bahmniConfig.setDateCreated(new Date());
        BahmniConfig add = bahmniConfigDao.save(bahmniConfig);
        assertEquals("registration", add.getAppName());
        assertEquals("app.json", add.getConfigName());
        assertEquals("New Config", add.getConfig());
    }

    @Test
    public void updateConfig() throws Exception {
        BahmniConfig clinical = bahmniConfigDao.get("clinical", "app.json");
        clinical.setConfig("Modified Config");
        BahmniConfig add = bahmniConfigDao.update(clinical);
        BahmniConfig modifiedClinical = bahmniConfigDao.get("clinical", "app.json");
        assertEquals("clinical", modifiedClinical.getAppName());
        assertEquals("app.json", modifiedClinical.getConfigName());
        assertEquals("Modified Config", modifiedClinical.getConfig());
        assertEquals(add, modifiedClinical);
    }
}
