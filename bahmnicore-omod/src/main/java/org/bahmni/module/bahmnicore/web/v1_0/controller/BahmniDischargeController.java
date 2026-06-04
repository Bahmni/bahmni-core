package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bedmanagement.service.BedManagementService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/discharge")
public class BahmniDischargeController extends BaseRestController {

    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction discharge(@RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        Patient patientByUuid = Context.getPatientService().getPatientByUuid(bahmniEncounterTransaction.getPatientUuid());
        BedManagementService bedManagementService = (BedManagementService) (Context.getModuleOpenmrsServices(BedManagementService.class.getName()).get(0));
        bedManagementService.unAssignPatientFromBed(patientByUuid);
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }
}
