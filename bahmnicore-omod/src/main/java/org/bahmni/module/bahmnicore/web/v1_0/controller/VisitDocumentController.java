package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.model.Document;
import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentResponse;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Controller
public class VisitDocumentController extends BaseRestController {
    private static final String INVALID_USER_PRIVILEGE = "User [%d] does not have require to delete patient file [%s]";
    private final String baseVisitDocumentUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/visitDocument";
    @Autowired
    private VisitDocumentService visitDocumentService;

    @Autowired
    private PatientDocumentService patientDocumentService;

    @Autowired
    private BahmniVisitLocationService bahmniVisitLocationService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private Log logger = LogFactory.getLog(this.getClass());

    @RequestMapping(method = RequestMethod.POST, value = baseVisitDocumentUrl)
    @WSDoc("Save Patient Document")
    @ResponseBody
    public VisitDocumentResponse save(@RequestBody VisitDocumentRequest visitDocumentUpload) {
        String visitLocation = bahmniVisitLocationService.getVisitLocationUuid(visitDocumentUpload.getLocationUuid());
        visitDocumentUpload.setVisitLocationUuid(visitLocation);
        final Encounter encounter = visitDocumentService.upload(visitDocumentUpload);
        return new VisitDocumentResponse(encounter.getVisit().getUuid(), encounter.getUuid());
    }

    @RequestMapping(method = RequestMethod.POST, value = baseVisitDocumentUrl + "/uploadDocument")
    @ResponseBody
    public HashMap<String, String> saveDocument(@RequestBody Document document) {
        Patient patient = Context.getPatientService().getPatientByUuid(document.getPatientUuid());
        String encounterTypeName = document.getEncounterTypeName();
        if (StringUtils.isEmpty(encounterTypeName)) {
            encounterTypeName = administrationService.getGlobalProperty("bahmni.encounterType.default");
        }
        HashMap<String, String> savedDocument = new HashMap<>();
        String url = patientDocumentService.saveDocument(patient.getId(), encounterTypeName, document.getContent(),
                document.getFormat(), document.getFileType());
        savedDocument.put("url", url);
        return savedDocument;
    }

    @RequestMapping(method = RequestMethod.DELETE, value = baseVisitDocumentUrl)
    @ResponseBody
    public ResponseEntity<Object> deleteDocument(@RequestParam(value = "filename") String fileName) {
        if (!Context.getUserContext().hasPrivilege(PrivilegeConstants.DELETE_PATIENT_DOCUMENT_PRIVILEGE)) {
            logger.error(String.format(INVALID_USER_PRIVILEGE, getAuthenticatedUserId(), fileName));
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.UNAUTHORIZED);
        }
        try {
            patientDocumentService.delete(fileName);
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        } catch (Exception e) {
            HashMap<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private Integer getAuthenticatedUserId() {
        User authenticatedUser = Context.getUserContext().getAuthenticatedUser();
        if (authenticatedUser == null) {
            return null;
        }
        return Integer.valueOf(authenticatedUser.getUserId());
    }
}
