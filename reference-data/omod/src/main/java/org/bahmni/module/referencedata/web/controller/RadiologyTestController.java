package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest;
import org.bahmni.module.referencedata.labconcepts.mapper.RadiologyTestMapper;
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
@RequestMapping(value = "/rest/v1/reference-data/radiology")
public class RadiologyTestController extends BaseRestController {
    private ConceptService conceptService;
    private final RadiologyTestMapper radiologyTestMapper;

    @Autowired
    public RadiologyTestController(ConceptService conceptService) {
        radiologyTestMapper = new RadiologyTestMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public RadiologyTest getRadiologyTest(@PathVariable("uuid") String uuid) {
        final Concept test = conceptService.getConceptByUuid(uuid);
        if (test == null) {
            throw new ConceptNotFoundException("No radiology test concept found with uuid " + uuid);
        }
        return radiologyTestMapper.map(test);
    }
}
