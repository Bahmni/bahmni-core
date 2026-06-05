package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.mapper.SampleMapper;
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
@RequestMapping(value = "/rest/v1/reference-data/sample")
public class SampleController extends BaseRestController {
    private ConceptService conceptService;
    private final SampleMapper sampleMapper;

    @Autowired
    public SampleController(ConceptService conceptService) {
        sampleMapper = new SampleMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Sample getSample(@PathVariable("uuid") String uuid) {
        final Concept sample = conceptService.getConceptByUuid(uuid);
        if (sample == null) {
            throw new ConceptNotFoundException("No sample concept found with uuid " + uuid);
        }
        return sampleMapper.map(sample);
    }
}
