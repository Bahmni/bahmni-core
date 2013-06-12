package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.ApplicationError;
import org.bahmni.module.bahmnicore.BahmniCoreException;
import org.bahmni.module.bahmnicore.BillingSystemException;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.model.error.ErrorCode;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Patient;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for REST web service access to
 * the Drug resource.
 */
@Controller
@RequestMapping(value = "/rest/v1/bahmnicore/patient")
public class BahmniPatientController extends BaseRestController {
    private static Logger logger = Logger.getLogger(BahmniPatientController.class);
    private BahmniPatientService bahmniPatientService;
    private static final String[] REQUIRED_FIELDS = {"names", "gender"};

    @Autowired
    public BahmniPatientController(BahmniPatientService bahmniPatientService) {
        this.bahmniPatientService = bahmniPatientService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @WSDoc("Save New Patient")
    @ResponseBody
    public Object createNewPatient(@RequestBody SimpleObject post, HttpServletResponse response) {
        BahmniPatient bahmniPatient = null;
        try {
            validatePost(post);
            bahmniPatient = new BahmniPatient(post);
            Patient patient = bahmniPatientService.createPatient(bahmniPatient);
            return respondCreated(response, bahmniPatient, patient);
        } catch (APIAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            return respondNotCreated(response, e);
        }
    }

    private Object respondNotCreated(HttpServletResponse response, Exception e) {
        logger.error("Patient create failed", e);
        SimpleObject obj = new SimpleObject();
        obj.add("exception", ExceptionUtils.getFullStackTrace(e));
        if (e instanceof ApplicationError) {
            ApplicationError applicationError = (ApplicationError) e;
            int errorCode = applicationError.getErrorCode();
            int statusCode = ErrorCode.duplicationError(errorCode) ? HttpServletResponse.SC_CONFLICT : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            response.setStatus(statusCode);
            Throwable cause = applicationError.getCause() == null ? applicationError : applicationError.getCause();
            obj.add("error", new SimpleObject().add("code", errorCode).add("message", cause.getMessage()));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        if(e instanceof BillingSystemException){
            BillingSystemException billingSystemException = (BillingSystemException) e;
            obj.add("patient", getPatientAsSimpleObject(billingSystemException.getPatient()));
        }
        return obj;
    }

    private SimpleObject respondCreated(HttpServletResponse response, BahmniPatient bahmniPatient, Patient patient) {
        response.setStatus(HttpServletResponse.SC_CREATED);
        SimpleObject obj = new SimpleObject();
        obj.add("uuid", patient == null ? null : patient.getUuid());
        obj.add("name", bahmniPatient.getPatientName());
        obj.add("identifier", patient == null ? bahmniPatient.getIdentifier() : patient.getPatientIdentifier().toString());
        return obj;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{patientUuid}")
    @WSDoc("Update existing patient")
    @ResponseBody
    public Object updatePatient(@PathVariable("patientUuid") String patientUuid, @RequestBody SimpleObject post,
                                HttpServletResponse response)
            throws Exception {
        try {
            validatePost(post);
            BahmniPatient bahmniPatient = new BahmniPatient(post);
            bahmniPatient.setUuid(patientUuid);
            Patient patient = bahmniPatientService.updatePatient(bahmniPatient);
            return RestUtil.created(response, getPatientAsSimpleObject(patient));
        } catch (Exception e) {
            logger.error("Update patient failed", e);
            throw e;
        }
    }

    private boolean validatePost(SimpleObject post) {
        List<String> missingFields = new ArrayList<String>();
        for (int i = 0; i < REQUIRED_FIELDS.length; i++) {
            if (post.get(REQUIRED_FIELDS[i]) == null) {
                missingFields.add(REQUIRED_FIELDS[i]);
            }
        }
        if (missingFields.size() > 0)
            throw new BahmniCoreException("Required field " + ArrayUtils.toString(missingFields) + " not found");
        return true;
    }

    private SimpleObject getPatientAsSimpleObject(Patient p) {
        SimpleObject obj = new SimpleObject();
        obj.add("uuid", p.getUuid());
        obj.add("name", p.getGivenName() + " " + p.getFamilyName());
        obj.add("identifier", p.getPatientIdentifier().getIdentifier());
        return obj;
    }
}
