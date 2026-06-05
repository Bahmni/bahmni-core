package org.bahmni.module.admin.config.dao;

import org.bahmni.module.admin.config.model.BahmniConfig;

import java.util.List;

public interface BahmniConfigDao {
    BahmniConfig get(String appName, String configName);

    BahmniConfig get(String uuid);

    List<BahmniConfig> getAllFor(String appName);

    BahmniConfig save(BahmniConfig bahmniConfig);

    BahmniConfig update(BahmniConfig existingConfig);

    List<String> getAll();
}
