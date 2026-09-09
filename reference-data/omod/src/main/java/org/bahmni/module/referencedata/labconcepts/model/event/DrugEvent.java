package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Drug;

import java.util.List;

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
    public EMREvent<Drug> asEMREvent(Object[] arguments) {
        Drug drug = (Drug) arguments[0];
        String restUrl = String.format(this.url, title, drug.getUuid());
        return new EMREvent<>(drug, category, title, null, restUrl);
    }
}
