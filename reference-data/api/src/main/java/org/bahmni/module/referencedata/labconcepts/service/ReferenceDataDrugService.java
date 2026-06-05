package org.bahmni.module.referencedata.labconcepts.service;


import org.bahmni.module.referencedata.labconcepts.contract.Drug;

public interface ReferenceDataDrugService {
    public org.openmrs.Drug saveDrug(Drug drug);
}
