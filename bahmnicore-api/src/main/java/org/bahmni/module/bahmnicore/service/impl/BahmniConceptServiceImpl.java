package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@Transactional
public class BahmniConceptServiceImpl implements BahmniConceptService{

    private ConceptService conceptService;
    private ConceptMapper conceptMapper;
    private BahmniConceptDao bahmniConceptDao;

    @Autowired
    public BahmniConceptServiceImpl(ConceptService conceptService, BahmniConceptDao bahmniConceptDao) {
        this.conceptService = conceptService;
        this.bahmniConceptDao = bahmniConceptDao;
        this.conceptMapper = new ConceptMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterTransaction.Concept getConceptByName(String conceptName) {
        Concept concept = conceptByName(conceptName);
        if (concept == null) {
            return new EncounterTransaction.Concept(null, conceptName, false, null, null, null, null,null);
        }
        return convertToContract(concept);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<ConceptAnswer> searchByQuestion(String questionConceptName, String query) {
        return bahmniConceptDao.searchByQuestion(getConcept(questionConceptName), query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Drug> getDrugsByConceptSetName(String conceptSetName, String searchTerm) {
        return bahmniConceptDao.searchDrugsByDrugName(getConcept(conceptSetName).getId(), searchTerm);
    }

    @Override
    @Transactional(readOnly = true)
    public Concept getConceptByFullySpecifiedName(String drug) {
        return bahmniConceptDao.getConceptByFullySpecifiedName(drug);
    }

    @Override
    public List<Concept> getConceptsByFullySpecifiedName(List<String> conceptNames) {
        if(conceptNames == null || conceptNames.isEmpty()){
         return Collections.EMPTY_LIST;
        }
        return bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);
    }

    private Concept getConcept(String conceptSetName) {
        Concept conceptSet = bahmniConceptDao.getConceptByFullySpecifiedName(conceptSetName);
        if (conceptSet == null) {
            throw new ConceptNotFoundException("Concept '" + conceptSetName + "' not found");
        }
        return conceptSet;
    }

    private EncounterTransaction.Concept convertToContract(Concept concept) {
        return conceptMapper.map(concept);
    }

    private Concept conceptByName(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }
}
