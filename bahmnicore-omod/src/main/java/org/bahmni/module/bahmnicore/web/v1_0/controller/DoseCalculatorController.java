package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.openmrs.module.rulesengine.domain.DosageRequest;
import org.openmrs.module.rulesengine.domain.Dose;
import org.openmrs.module.rulesengine.engine.RulesEngine;
import org.openmrs.module.rulesengine.engine.RulesEngineImpl;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/calculateDose")
public class DoseCalculatorController extends BaseRestController {


    private RulesEngine rulesEngine = new RulesEngineImpl();

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Dose calculateDose(@RequestParam(value = "dosageRequest") String dosageRequest) throws Exception {
        DosageRequest request= new DosageRequest(dosageRequest);
        return rulesEngine.calculateDose(request);
    }
}
