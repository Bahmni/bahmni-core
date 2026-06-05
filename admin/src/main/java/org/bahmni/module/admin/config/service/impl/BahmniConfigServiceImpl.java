package org.bahmni.module.admin.config.service.impl;

import org.bahmni.module.admin.config.dao.BahmniConfigDao;
import org.bahmni.module.admin.config.model.BahmniConfig;
import org.bahmni.module.admin.config.service.BahmniConfigService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class BahmniConfigServiceImpl implements BahmniConfigService {
    private BahmniConfigDao bahmniConfigDao;

    @Autowired
    public BahmniConfigServiceImpl(BahmniConfigDao bahmniConfigDao) {
        this.bahmniConfigDao = bahmniConfigDao;
    }

    @Override
    public BahmniConfig get(String appName, String configName) {
        return bahmniConfigDao.get(appName, configName);
    }

    @Override
    public List<BahmniConfig> getAllFor(String appName) {
        return bahmniConfigDao.getAllFor(appName);
    }

    @Override
    public BahmniConfig save(BahmniConfig bahmniConfig) {
        createNewConfig(bahmniConfig);
        return bahmniConfigDao.save(bahmniConfig);
    }

    @Override
    public BahmniConfig update(BahmniConfig configUpdate) {
        BahmniConfig existingConfig = bahmniConfigDao.get(configUpdate.getUuid());
        updateExistingConfig(configUpdate, existingConfig);
        BahmniConfig updatedConfig = bahmniConfigDao.update(existingConfig);
        return bahmniConfigDao.get(updatedConfig.getUuid());
    }

    @Override
    public List<String> getAll() {
        return bahmniConfigDao.getAll();
    }

    private void createNewConfig(BahmniConfig bahmniConfig) {
        bahmniConfig.setDateCreated(new Date());
        bahmniConfig.setCreator(Context.getAuthenticatedUser());
        bahmniConfig.setUuid(UUID.randomUUID().toString());
        bahmniConfig.setConfigId(null);
    }

    private void updateExistingConfig(BahmniConfig updatedConfig, BahmniConfig existingConfig) {
        existingConfig.setConfig(updatedConfig.getConfig());
        existingConfig.setChangedBy(Context.getAuthenticatedUser());
        existingConfig.setDateChanged(new Date());
    }
}
