package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.mapper.ConceptMapper;
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
public class ConceptController extends BaseRestController {
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;
    private final ConceptMapper conceptMapper;

    public ConceptController() {
        conceptMapper = new ConceptMapper();
    }

    @RequestMapping(value = "/rest/v1/reference-data/concept", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Concept> create(@RequestBody Concept concept) {
        try {
            org.openmrs.Concept savedConcept = referenceDataConceptService.saveConcept(concept);
            return new ResponseEntity<>(conceptMapper.map(savedConcept), HttpStatus.CREATED);
        } catch (Throwable error) {
            return new ResponseEntity<>(new Concept(), HttpStatus.BAD_REQUEST);
        }
    }
}
