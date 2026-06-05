package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.bahmni.module.referencedata.labconcepts.service.DrugMetaDataService;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrugMetaDataServiceImpl implements DrugMetaDataService {

    private final ConceptService conceptService;

    @Autowired
    public DrugMetaDataServiceImpl(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @Override
    public DrugMetaData getDrugMetaData(org.bahmni.module.referencedata.labconcepts.contract.Drug drug) {
        Concept dosageFormConcept = conceptService.getConceptByName(drug.getDosageForm());
        Drug existingDrug = getExistingDrug(drug, dosageFormConcept);

        Concept drugConcept = conceptService.getConceptByName(drug.getGenericName());
        ConceptClass drugConceptClass = conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID);
        return new DrugMetaData(existingDrug, drugConcept, dosageFormConcept, drugConceptClass);
    }

    private Drug getExistingDrug(org.bahmni.module.referencedata.labconcepts.contract.Drug drug,Concept dosageFormConcept) {
        if (!StringUtils.isBlank(drug.getUuid())) {
            return conceptService.getDrugByUuid(drug.getUuid());
        }

        if(dosageFormConcept == null){
            return null;
        }

        List<Drug> drugs = conceptService.getDrugs(drug.getName());
        for(Drug mrsDrug: drugs){
            if(mrsDrug.getStrength().equals(drug.getStrength()) &&
                    mrsDrug.getDosageForm().getConceptId().equals(dosageFormConcept.getConceptId()) &&
                    mrsDrug.getName().equals(drug.getName())){
                return mrsDrug;
            }
        }
        return null;
    }
}