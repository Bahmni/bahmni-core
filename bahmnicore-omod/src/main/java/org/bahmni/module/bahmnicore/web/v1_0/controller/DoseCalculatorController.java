package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.util.WebUtils;
import org.openmrs.module.rulesengine.domain.DosageRequest;
import org.openmrs.module.rulesengine.engine.RulesEngine;
import org.openmrs.module.rulesengine.engine.RulesEngineImpl;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/calculateDose")
public class DoseCalculatorController extends BaseRestController {

    private static final Log log = LogFactory.getLog(DoseCalculatorController.class);

    private RulesEngine rulesEngine = new RulesEngineImpl();

    /**
     * The rules engine signals user-correctable problems (missing weight/height observation via
     * {@code org.openmrs.module.rulesengine.util.Validator}, unknown rule, drug missing from the
     * dosage CSV, malformed request JSON) with plain checked exceptions. Those are reported as
     * 400 with the engine's message so the UI can show it. Unchecked exceptions indicate a server
     * or configuration fault and are left to propagate as 500.
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> calculateDose(@RequestParam(value = "dosageRequest") String dosageRequest) {
        try {
            DosageRequest request = new DosageRequest(dosageRequest);
            return new ResponseEntity<>(rulesEngine.calculateDose(request), HttpStatus.OK);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Unable to calculate dose: " + e.getMessage());
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
