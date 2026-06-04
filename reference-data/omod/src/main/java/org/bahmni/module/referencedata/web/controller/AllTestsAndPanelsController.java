package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.mapper.AllTestsAndPanelsMapper;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/rest/v1/reference-data/all-tests-and-panels")
public class AllTestsAndPanelsController extends BaseRestController {

    private ConceptService conceptService;
    private final AllTestsAndPanelsMapper allTestsAndPanelsMapper;

    @Autowired
    public AllTestsAndPanelsController(ConceptService conceptService) {
        this.conceptService = conceptService;
        this.allTestsAndPanelsMapper = new AllTestsAndPanelsMapper();
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public AllTestsAndPanels getAllTestsAndPanels(@PathVariable("uuid") String uuid) {
        final Concept allTestsAndPanelsConceptSet = conceptService.getConceptByUuid(uuid);
        if (allTestsAndPanelsConceptSet == null) {
            throw new ConceptNotFoundException("All tests and panels concept set not found with uuid " + uuid);
        }
        return allTestsAndPanelsMapper.map(allTestsAndPanelsConceptSet);
    }
}