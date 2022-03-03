package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;
import java.time.LocalDateTime;
import org.openmrs.Drug;

import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

public class DrugEvent implements ConceptServiceOperationEvent {
    protected String url;
    protected String category;
    protected String title;

    public DrugEvent(String url, String category, String title) {
        this.url = url;
        this.category = category;
        this.title = title;
    }

    public DrugEvent() {
    }


    List<String> operations() {
        return asList("saveDrug", "purgeDrug");
    }

    private boolean isValid(Object[] arguments) {
        return arguments != null && arguments.length > 0;
    }

    @Override
    public Boolean isApplicable(String operation, Object[] arguments) {
        return this.operations().contains(operation) && isValid(arguments) && arguments[0] instanceof Drug;
    }

    @Override
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException {
        Drug drug = (Drug) arguments[0];
        String url = String.format(this.url, title, drug.getUuid());
        return new Event(UUID.randomUUID().toString(), title, LocalDateTime.now(), url, url, category);
    }

}
