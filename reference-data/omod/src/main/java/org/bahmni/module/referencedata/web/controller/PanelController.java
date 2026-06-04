package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.mapper.PanelMapper;
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
@RequestMapping(value = "/rest/v1/reference-data/panel")
public class PanelController extends BaseRestController {
    private ConceptService conceptService;
    private final PanelMapper panelMapper;

    @Autowired
    public PanelController(ConceptService conceptService) {
        panelMapper = new PanelMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Panel getPanel(@PathVariable("uuid") String uuid) {
        final Concept panel = conceptService.getConceptByUuid(uuid);
        if (panel == null) {
            throw new ConceptNotFoundException("No panel concept found with uuid " + uuid);
        }
        return panelMapper.map(panel);
    }
}
