package org.bahmni.module.admin.config;

import org.bahmni.module.bahmnicommons.api.configuration.ModuleAppConfig;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BahmniCoreModuleConfig implements ModuleAppConfig {
    @Override
    public String getModuleName() {
        return "core";
    }

    @Override
    public List<String> getGlobalAppProperties() {
        return Arrays.asList(
                "bahmni.encounterType.default",
                "clinic.helpDeskNumber",
                "cdss.enable",
                "obs.conceptMappingsForOT", //should be moved to OT module
                "drugOrder.drugOther",
                "concept.reasonForDeath",
                "bahmni.relationshipTypeMap",
                "bahmni.primaryIdentifierType");
    }
}
