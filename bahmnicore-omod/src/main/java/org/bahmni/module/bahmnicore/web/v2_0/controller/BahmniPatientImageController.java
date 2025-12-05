package org.bahmni.module.bahmnicore.web.v2_0.controller;

import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller("bahmniPatientImageControllerV2")
@RequestMapping(value = "/rest/" + RestConstants.VERSION_2 + "/patientImage")
public class BahmniPatientImageController extends BaseRestController {

    private PatientDocumentService patientDocumentService;

    @Autowired
    public BahmniPatientImageController(PatientDocumentService patientDocumentService) {
        this.patientDocumentService = patientDocumentService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getImage(@RequestParam(value = "patientUuid", required = true) String patientUuid) {
        UserContext userContext = Context.getUserContext();
        if (userContext.isAuthenticated()) {
            return patientDocumentService.retriveImageWithoutDefault(patientUuid);
        }
        return new ResponseEntity<Object>(new Object(), HttpStatus.UNAUTHORIZED);
    }
}
