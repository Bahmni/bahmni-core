package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.bahmniemrapi.disposition.service.BahmniDispositionService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/disposition")
public class BahmniDispositionController extends BaseRestController {

    @Autowired
    private BahmniDispositionService bahmniDispositionService;

    @Autowired
    private VisitDao visitDao;

    @Autowired
    private PatientService patientService;

    @RequestMapping(method = RequestMethod.GET, value = "visit")
    @ResponseBody
    public List<BahmniDisposition> getDispositionByVisitUuid(@RequestParam(value = "visitUuid") String visitUuid) {
        return bahmniDispositionService.getDispositionByVisitUuid(visitUuid);
    }

    @RequestMapping(method = RequestMethod.GET, value = "visitWithLocale")
    @ResponseBody
    public List<BahmniDisposition> getDispositionByVisitUuid(@RequestParam(value = "visitUuid") String visitUuid, @RequestParam(value = "locale") String locale) {
        return bahmniDispositionService.getDispositionByVisitUuid(visitUuid , new Locale(locale));
    }

    @RequestMapping(method = RequestMethod.GET, value = "patient")
    @ResponseBody
    public List<BahmniDisposition> getDispositionByPatientUuid(@RequestParam(value = "patientUuid") String patientUuid, @RequestParam(value = "numberOfVisits") int numberOfVisits){
        Patient patient = patientService.getPatientByUuid(patientUuid);

        if(patient == null){
            return new ArrayList<>();
        }

        List<Visit> visits = visitDao.getVisitsByPatient(patient,numberOfVisits);
        return bahmniDispositionService.getDispositionByVisits(visits);
    }
    @RequestMapping(method = RequestMethod.GET, value = "patientWithLocale")
    @ResponseBody
    public List<BahmniDisposition> getDispositionByPatientUuid(@RequestParam(value = "patientUuid") String patientUuid, @RequestParam(value = "numberOfVisits") int numberOfVisits, @RequestParam(value = "locale") String locale){
        Patient patient = patientService.getPatientByUuid(patientUuid);

        if(patient == null){
            return new ArrayList<>();
        }

        List<Visit> visits = visitDao.getVisitsByPatient(patient,numberOfVisits);
        return bahmniDispositionService.getDispositionByVisits(visits, new Locale(locale));
    }

}
