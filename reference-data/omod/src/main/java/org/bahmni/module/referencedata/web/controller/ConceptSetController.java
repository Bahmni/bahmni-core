package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.mapper.ConceptSetMapper;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ConceptSetController extends BaseRestController {
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;
    private final ConceptSetMapper conceptSetMapper;

    public ConceptSetController() {
        conceptSetMapper = new ConceptSetMapper();
    }

    @RequestMapping(value = "/rest/v1/reference-data/conceptset", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ConceptSet> create(@RequestBody ConceptSet concept) {
        try {
            org.openmrs.Concept savedConcept = referenceDataConceptService.saveConcept(concept);
            return new ResponseEntity<>(conceptSetMapper.map(savedConcept), HttpStatus.CREATED);
        } catch (Throwable error) {
            return new ResponseEntity<>(new ConceptSet(), HttpStatus.BAD_REQUEST);
        }
    }
}
