package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.mapper.AllSamplesMapper;
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
@RequestMapping(value = "/rest/v1/reference-data/all-samples")
public class AllSamplesController  extends BaseRestController {
    private ConceptService conceptService;
    private final AllSamplesMapper allSamplesMapper;

    @Autowired
    public AllSamplesController(ConceptService conceptService) {
        this.conceptService = conceptService;
        this.allSamplesMapper = new AllSamplesMapper();
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public AllSamples getAllTestsAndPanels(@PathVariable("uuid") String uuid) {
        final Concept allSamples = conceptService.getConceptByUuid(uuid);
        if (allSamples == null) {
            throw new ConceptNotFoundException("All tests and panels concept set not found with uuid " + uuid);
        }
        return allSamplesMapper.map(allSamples);
    }

}