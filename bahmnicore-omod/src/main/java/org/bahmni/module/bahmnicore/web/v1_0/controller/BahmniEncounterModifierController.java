package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData;
import org.bahmni.module.bahmnicore.service.BahmniEncounterModifierService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencountermodifier")
public class BahmniEncounterModifierController extends BaseRestController {

    private static final Logger log = LogManager.getLogger(BahmniEncounterModifierController.class);


    @Autowired
    private BahmniEncounterModifierService bahmniEncounterModifierService;

    @Autowired
    public BahmniEncounterModifierController(BahmniEncounterModifierService bahmniEncounterModifierService) {
        this.bahmniEncounterModifierService = bahmniEncounterModifierService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public EncounterModifierData get(@RequestBody EncounterModifierData encounterModifierData) throws Exception {
        EncounterModifierData encounterTransaction;
        try {
            encounterTransaction = bahmniEncounterModifierService.getModifiedEncounter(encounterModifierData);
        } catch (Throwable e) {
            log.error("Error in running groovy script: " + e.getMessage(), e);
            throw e;
        }
        return encounterTransaction;
    }
}
