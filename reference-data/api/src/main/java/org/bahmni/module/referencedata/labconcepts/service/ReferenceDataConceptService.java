package org.bahmni.module.referencedata.labconcepts.service;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;

public interface ReferenceDataConceptService {
    public org.openmrs.Concept saveConcept(Concept concept);

    public org.openmrs.Concept saveConcept(ConceptSet conceptSet);

    public org.bahmni.module.referencedata.labconcepts.contract.Concepts getConcept(String conceptName);
}
