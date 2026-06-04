package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BahmniAddressHierarchyControllerIT extends BaseIntegrationTest {

    @Autowired
    BahmniAddressHierarchyController bahmniAddressHierarchyController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("addressHierarchy.xml");
    }

    @Test
    public void shouldGetAddressHierarchyByUuid() throws Exception {
        BahmniAddressHierarchyEntry bahmniAddressHierarchyEntries = bahmniAddressHierarchyController.get("22e41146-e162-11df-9195-001e378eb67f");
        assertNotNull(bahmniAddressHierarchyEntries);
        assertEquals("United States", bahmniAddressHierarchyEntries.getName());
        assertEquals("Country", bahmniAddressHierarchyEntries.getAddressHierarchyLevel().getName());
    }


    @Test
    public void shouldGetAddressHierarchyEntriesByUuidIncludingParent() throws Exception {
        List<BahmniAddressHierarchyEntry> addressHierarchyEntries = bahmniAddressHierarchyController.getAddressHierarchyEntriesByUuid(Arrays.asList("22e41146-e134-11df-9195-001e378eb67f","22e41146-e162-11df-9195-001e378eb67f"));
        assertNotNull(addressHierarchyEntries);
        assertEquals("United States", addressHierarchyEntries.get(0).getName());
        assertEquals("Country", addressHierarchyEntries.get(0).getAddressHierarchyLevel().getName());
        assertEquals("New York", addressHierarchyEntries.get(1).getName());
        assertEquals("State", addressHierarchyEntries.get(1).getAddressHierarchyLevel().getName());
        assertEquals((Integer) 1, addressHierarchyEntries.get(1).getParentId());
    }
}