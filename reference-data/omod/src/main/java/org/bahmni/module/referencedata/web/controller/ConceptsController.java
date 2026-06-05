package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.contract.ConceptDetails;
import org.bahmni.module.referencedata.contract.ConceptName;
import org.bahmni.module.referencedata.helper.ConceptHelper;
import org.bahmni.module.referencedata.labconcepts.contract.Concepts;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.openmrs.Concept;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Controller

@RequestMapping(value = "/rest/v1/reference-data/")
public class ConceptsController extends BaseRestController {
    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    @Autowired
    ConceptHelper conceptHelper;

    @RequestMapping(value = "/concepts", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Concepts> create(@RequestParam(value = "conceptName", required = true) String conceptName) {
        try {
            Concepts concepts = referenceDataConceptService.getConcept(conceptName);
            return new ResponseEntity<>(concepts, HttpStatus.OK);
        } catch (Throwable error) {
            return new ResponseEntity<>(new Concepts(), HttpStatus.NOT_FOUND);
        }
    }


    @RequestMapping(value = "/leafConcepts", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Set<ConceptDetails>> getLeafConcepts(@RequestParam(value = "conceptName", required = true) String conceptName) {
        List<Concept> concepts = conceptHelper.getConceptsForNames(Collections.singletonList(conceptName));
        Set<ConceptDetails> leafConceptDetails = conceptHelper.getLeafConceptDetails(concepts, true);
        return new ResponseEntity<>(leafConceptDetails, HttpStatus.OK);
    }

    @RequestMapping(value = "/leafConceptNames", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Set<ConceptName>> getLeafConcepts(@RequestParam(value = "conceptNames", required = true) List<String> conceptNames) {
        List<Concept> concepts = conceptHelper.getConceptsForNames(conceptNames);
        Set<ConceptName> leafConceptDetails = conceptHelper.getLeafConceptNames(concepts);
        return new ResponseEntity<>(leafConceptDetails, HttpStatus.OK);
    }

    @RequestMapping(value = "/getChildConcepts", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Set<String>> getChildConcepts(@RequestParam(value = "conceptNames", required = true) List<String> conceptNames) {
        List<Concept> conceptsForNames = conceptHelper.getConceptsForNames(conceptNames);
        Set<String> childConceptNames = conceptHelper.getChildConceptNames(conceptsForNames);
        return new ResponseEntity<>(childConceptNames, HttpStatus.OK);
    }

    @RequestMapping(value = "/getConceptId", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Set<Integer>> getConceptId(@RequestParam(value = "conceptNames", required = true) List<String> conceptNames) {
        List<Concept> conceptsForNames = conceptHelper.getConceptsForNames(conceptNames);
        Set<Integer> conceptIds=conceptHelper.getConceptIds(conceptsForNames);
        return new ResponseEntity<>(conceptIds, HttpStatus.OK);
    }
}
