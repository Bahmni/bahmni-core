package org.bahmni.module.bahmnicore.contract.drugorder;


import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class ConceptData {
    private String name;
    private String uuid;
    private String rootConcept;

    public ConceptData() {
    }

    public ConceptData(Concept concept) {
        if(concept != null){
            this.name = concept.getName(Context.getLocale()).getName();
            this.uuid = concept.getUuid();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootConcept() {
        return rootConcept;
    }

    public void setRootConcept(String rootConcept) {
        this.rootConcept = rootConcept;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
