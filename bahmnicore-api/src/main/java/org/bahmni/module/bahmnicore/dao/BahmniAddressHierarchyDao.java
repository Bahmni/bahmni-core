package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;

import java.util.List;

public interface BahmniAddressHierarchyDao {
    List<BahmniAddressHierarchyEntry> getAddressHierarchyEntriesByUuid(List<String> uuids);
}
