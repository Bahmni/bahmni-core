package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/diagnosis")
public class BahmniDiagnosisController extends BaseRestController {

    @Autowired
    private BahmniDiagnosisService bahmniDiagnosisService;

    @RequestMapping(method = RequestMethod.GET, value = "search")
    @ResponseBody
    public List<BahmniDiagnosisRequest> search(@RequestParam("patientUuid") String patientUuid, @RequestParam(value = "fromDate", required = false) String date, @RequestParam(value = "visitUuid", required = false) String visitUuid) throws Exception {
        if (visitUuid != null) {
            return bahmniDiagnosisService.getBahmniDiagnosisByPatientAndVisit(patientUuid, visitUuid);
        } else {
            return bahmniDiagnosisService.getBahmniDiagnosisByPatientAndDate(patientUuid, date);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "delete")
    @ResponseBody
    public boolean delete(@RequestParam(value = "obsUuid", required = true) String obsUuid) throws Exception {
        bahmniDiagnosisService.delete(obsUuid);
        return true;
    }
}
