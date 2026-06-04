package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.labconcepts.mapper.DrugMapper;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.bahmni.module.referencedata.labconcepts.service.DrugMetaDataService;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataDrugService;
import org.bahmni.module.referencedata.labconcepts.validator.DrugValidator;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataDrugServiceImpl implements ReferenceDataDrugService {

    private final DrugValidator drugValidator;
    private final ConceptService conceptService;
    private final DrugMetaDataService drugMetaDataService;
    private DrugMapper drugMapper;


    @Autowired
    public ReferenceDataDrugServiceImpl(ConceptService conceptService, DrugMetaDataService drugMetaDataService) {
        drugValidator = new DrugValidator();
        this.conceptService = conceptService;
        this.drugMapper = new DrugMapper();
        this.drugMetaDataService = drugMetaDataService;
    }

    @Override
    public Drug saveDrug(org.bahmni.module.referencedata.labconcepts.contract.Drug drug) {
        DrugMetaData drugMetaData = drugMetaDataService.getDrugMetaData(drug);
        drugValidator.validate(drug, drugMetaData);
        Drug conceptDrug = drugMapper.map(drug, drugMetaData);
        return conceptService.saveDrug(conceptDrug);
    }
}
