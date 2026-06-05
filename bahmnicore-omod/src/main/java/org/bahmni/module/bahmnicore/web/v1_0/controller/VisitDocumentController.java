package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.model.Document;
import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.bahmni.module.bahmnicore.util.WebUtils;
import org.bahmni.module.bahmnicore.web.v1_0.InvalidInputException;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentResponse;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.HashMap;

@Controller
public class VisitDocumentController extends BaseRestController {
    private static final String INSUFFICIENT_PRIVILEGE = "Insufficient privilege";
    private static final String INVALID_USER_PRIVILEGE = "User [%d] does not have required privilege to delete patient file [%s]";
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
    public ResponseEntity<HashMap<String, Object>> saveDocument(@RequestBody Document document) {
        try {
            HashMap<String, Object> savedDocument = new HashMap<>();
            Patient patient = Context.getPatientService().getPatientByUuid(document.getPatientUuid());
            String encounterTypeName = document.getEncounterTypeName();
            String maxDocumentSize = System.getenv("DOCUMENT_MAX_SIZE_MB");

            if (StringUtils.isEmpty(encounterTypeName)) {
                encounterTypeName = administrationService.getGlobalProperty("bahmni.encounterType.default");
            }
            String fileName = sanitizeFileName(document.getFileName());
            Paths.get(fileName);

            if (!StringUtils.isEmpty(maxDocumentSize)) {
                Long maxDocumentSizeMb = Long.parseLong(maxDocumentSize);
                Long maxDocumentSizeBytes = maxDocumentSizeMb * 1024 * 1024;
                if (document.getContent().length() > maxDocumentSizeBytes) {
                    logger.warn("Uploaded document size is greater than the maximum size " + maxDocumentSizeMb + "MB");
                    savedDocument.put("maxDocumentSizeMB", maxDocumentSizeMb);
                    return new ResponseEntity<>(savedDocument, HttpStatus.PAYLOAD_TOO_LARGE);
                }
            }
            // Old files will follow: patientid-encounterName-uuid.ext (eg. 6-Patient-Document-706a448b-3f10-11e4-adec-0800271c1b75.png)
            // New ones will follow: patientid_encounterName_uuid__filename.ext (eg. 6-Patient-Document-706a448b-3f10-11e4-adec-0800271c1b75__doc1.png)
            String url = patientDocumentService.saveDocument(patient.getId(), encounterTypeName, document.getContent(),
                document.getFormat(), document.getFileType(), fileName);
            savedDocument.put("url", url);
            return new ResponseEntity<>(savedDocument, HttpStatus.OK);
        } catch (Exception e) {
            throw new InvalidInputException("Could not save patient document", e);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = baseVisitDocumentUrl)
    @ResponseBody
    public ResponseEntity<Object> deleteDocument(@RequestParam(value = "filename") String fileName) {
        if (!Context.getUserContext().hasPrivilege(PrivilegeConstants.DELETE_PATIENT_DOCUMENT_PRIVILEGE)) {
            logger.error(String.format(INVALID_USER_PRIVILEGE, getAuthenticatedUserId(), fileName));
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, INSUFFICIENT_PRIVILEGE), HttpStatus.FORBIDDEN);
        }
        try {
            patientDocumentService.delete(fileName);
            return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    private Integer getAuthenticatedUserId() {
        User authenticatedUser = Context.getUserContext().getAuthenticatedUser();
        if (authenticatedUser == null) {
            return null;
        }
        return Integer.valueOf(authenticatedUser.getUserId());
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "";
        return fileName.trim().replaceAll(" ", "-").replaceAll("__", "_");
    }

}
