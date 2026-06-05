package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniAddressHierarchyDao;
import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;
import org.bahmni.module.bahmnicore.service.BahmniAddressHierarchyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniAddressHierarchyServiceImplTest {
    private BahmniAddressHierarchyService bahmniAddressHierarchyService;

    @Mock
    private BahmniAddressHierarchyDao bahmniAddressHierarchyDao;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bahmniAddressHierarchyService = new BahmniAddressHierarchyServiceImpl(bahmniAddressHierarchyDao);
    }

    @Test
    public void shouldGetAddressHierarchyEntryByUuid() throws Exception {
        List<BahmniAddressHierarchyEntry> addressHierarchyEntries = new ArrayList<>();
        BahmniAddressHierarchyEntry addressHierarchyEntry = new BahmniAddressHierarchyEntry();
        addressHierarchyEntry.setName("test");
        addressHierarchyEntries.add(addressHierarchyEntry);
        List<String> uuids = new ArrayList<>();
        uuids.add("uuid");
        when(bahmniAddressHierarchyDao.getAddressHierarchyEntriesByUuid(uuids)).thenReturn(addressHierarchyEntries);

        List<BahmniAddressHierarchyEntry> hierarchyEntriesByUuid = bahmniAddressHierarchyService.getAddressHierarchyEntriesByUuid(uuids);

        verify(bahmniAddressHierarchyDao, times(1)).getAddressHierarchyEntriesByUuid(uuids);
        assertEquals(addressHierarchyEntry.getName(), hierarchyEntriesByUuid.get(0).getName());
    }
}