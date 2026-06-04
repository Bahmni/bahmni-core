package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.mapper.LabTestMapper;
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
@RequestMapping(value = "/rest/v1/reference-data/test")
public class TestController extends BaseRestController{
    private ConceptService conceptService;
    private final LabTestMapper testMapper;

    @Autowired
    public TestController(ConceptService conceptService) {
        testMapper = new LabTestMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public LabTest getTest(@PathVariable("uuid") String uuid) {
        final Concept test = conceptService.getConceptByUuid(uuid);
        if (test == null) {
            throw new ConceptNotFoundException("No test concept found with uuid " + uuid);
        }
        return testMapper.map(test);
    }
}
