package org.bahmni.module.referencedata.addresshierarchy;

import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.events.ReferenceDataEventPublisher;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Arrays.asList;

public class AddressHierarchyEntryEventInterceptor implements AfterReturningAdvice {

    private static final List<String> SAVE_ADDRESS_HIERARCHY_ENTRY_METHODS = asList("saveAddressHierarchyEntries", "saveAddressHierarchyEntry");
    private static final String TEMPLATE = "/openmrs/ws/rest/v1/addressHierarchy/%s";
    private static final String CATEGORY = "addressHierarchy";
    private static final String TITLE = "addressHierarchy";

    private ReferenceDataEventPublisher eventPublisher;

    public AddressHierarchyEntryEventInterceptor() {
    }

    public AddressHierarchyEntryEventInterceptor(ReferenceDataEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setEventPublisher(ReferenceDataEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (SAVE_ADDRESS_HIERARCHY_ENTRY_METHODS.contains(method.getName())) {
            publishEvents(arguments);
        }
    }

    private void publishEvents(Object[] arguments) {
        if (arguments == null) {
            return;
        }
        if (arguments[0] instanceof List) {
            List<AddressHierarchyEntry> entries = (List<AddressHierarchyEntry>) arguments[0];
            for (AddressHierarchyEntry entry : entries) {
                publishEvent(entry);
            }
            return;
        }
        publishEvent((AddressHierarchyEntry) arguments[0]);
    }

    private void publishEvent(AddressHierarchyEntry entry) {
        if (entry == null) {
            return;
        }
        String restUrl = String.format(TEMPLATE, entry.getUuid());
        EMREvent<AddressHierarchyEntry> event = new EMREvent<>(entry, CATEGORY, TITLE, null, restUrl);
        getEventPublisher().publishEvent(event);
    }

    private ReferenceDataEventPublisher getEventPublisher() {
        if (eventPublisher == null) {
            eventPublisher = ServiceContext.getInstance().getApplicationContext().getBean(ReferenceDataEventPublisher.class);
        }
        return eventPublisher;
    }
}
