package org.bahmni.module.referencedata.addresshierarchy;

import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.events.ReferenceDataEventPublisher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddressHierarchyEntryEventInterceptorTest {
    @Mock
    private ReferenceDataEventPublisher eventPublisher;

    private AddressHierarchyEntryEventInterceptor publishedFeed;
    private AddressHierarchyEntry addressHierarchyEntry;

    @Before
    public void setUp() {
        addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setUuid("uuid");
        addressHierarchyEntry.setUserGeneratedId("707070");

        publishedFeed = new AddressHierarchyEntryEventInterceptor(eventPublisher);
    }

    @Test
    public void shouldPublishEventAfterSavingAddressHierarchyEntry() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntry", AddressHierarchyEntry.class);
        Object[] objects = new Object[]{addressHierarchyEntry};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(eventPublisher).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEventAfterSavingAddressHierarchyEntries() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(addressHierarchyEntry);
        entries.add(addressHierarchyEntry);
        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(eventPublisher, times(2)).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEventIfParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);

        publishedFeed.afterReturning(null, method, null, null);

        verify(eventPublisher, never()).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldNotPublishEventIfEntryInParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(null);

        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);

        verify(eventPublisher, never()).publishEvent(any(EMREvent.class));
    }
}
