package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;
import org.bahmni.module.bahmnicore.service.BahmniAddressHierarchyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniAddressHierarchyControllerTest {
    @Mock
    BahmniAddressHierarchyService bahmniAddressHierarchyService;

    private BahmniAddressHierarchyController bahmniAddressHierarchyController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bahmniAddressHierarchyController = new BahmniAddressHierarchyController(bahmniAddressHierarchyService);
    }

    @Test
    public void shouldGetAddressHierarchyEntryByUuid() throws Exception {
        List<BahmniAddressHierarchyEntry> addressHierarchyEntries = new ArrayList<>();
        BahmniAddressHierarchyEntry addressHierarchyEntry = new BahmniAddressHierarchyEntry();
        addressHierarchyEntry.setName("test");
        addressHierarchyEntries.add(addressHierarchyEntry);
        when(bahmniAddressHierarchyService.getAddressHierarchyEntriesByUuid(Arrays.asList("uuid"))).thenReturn(addressHierarchyEntries);
        BahmniAddressHierarchyEntry hierarchyEntry = bahmniAddressHierarchyController.get("uuid");

        verify(bahmniAddressHierarchyService, times(1)).getAddressHierarchyEntriesByUuid(Arrays.asList("uuid"));
        assertNotNull(hierarchyEntry);
        assertEquals("test", addressHierarchyEntries.get(0).getName());
    }

    @Test
    public void shouldReturnNullIfUuidIsNull() throws Exception {
        BahmniAddressHierarchyEntry hierarchyEntry = bahmniAddressHierarchyController.get(null);

        verify(bahmniAddressHierarchyService, never()).getAddressHierarchyEntriesByUuid(Arrays.asList(anyString()));
        assertNull(hierarchyEntry);
    }
}