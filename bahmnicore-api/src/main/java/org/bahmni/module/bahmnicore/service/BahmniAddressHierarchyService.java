package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;

import java.util.List;

public interface BahmniAddressHierarchyService {
    List<BahmniAddressHierarchyEntry> getAddressHierarchyEntriesByUuid(List<String> uuids);
}
