package org.bahmni.module.admin.config.service;

import org.bahmni.module.admin.config.model.BahmniConfig;

import java.util.List;

public interface BahmniConfigService {
    BahmniConfig get(String appName, String configName);

    List<BahmniConfig> getAllFor(String appName);

    BahmniConfig save(BahmniConfig bahmniConfig);

    BahmniConfig update(BahmniConfig bahmniConfig);

    List<String> getAll();
}
