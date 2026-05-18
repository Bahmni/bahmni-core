package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftResponse;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.bahmni.module.bahmnicore.util.WebUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/formdraft")
public class FormDraftController extends BaseRestController {

    private static final Logger log = LoggerFactory.getLogger(FormDraftController.class);

    @Autowired
    private FormDraftService formDraftService;

    /**
     * List all unsaved drafts for a given provider.
     * GET /rest/v1/bahmnicore/formdraft/list?providerUuid=xxx
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getDraftsByProvider(
            @RequestParam(value = "providerUuid", required = true) String providerUuid) {
        try {
            List<FormDraftSummaryResponse> drafts = formDraftService.getDraftsByProvider(providerUuid);
            return new ResponseEntity<>(drafts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for draft list", e);
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error retrieving draft list for provider: " + providerUuid, e);
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Auto-save a form draft. Upserts by patient and provider UUID.
     * POST /rest/v1/bahmnicore/formdraft
     *
     * @param request FormDraftRequest with patientUuid, providerUuid, and formData
     * @return FormDraftResponse with uuid, formData, markedAsSaved flag, and timestamp
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveDraft(@RequestBody FormDraftRequest request) {
        try {
            FormDraft draft = formDraftService.saveDraft(request);
            String formData = formDraftService.getFormData(draft.getFormDataPath());
            Long timestamp = draft.getDateChanged() != null ? draft.getDateChanged().getTime() : draft.getDateCreated().getTime();
            FormDraftResponse response = new FormDraftResponse(draft.getUuid(), formData, draft.getMarkedAsSaved(), timestamp);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid form draft request", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error saving form draft", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieve a form draft by patient and provider UUIDs.
     * GET /rest/v1/bahmnicore/formdraft?patientUuid=xxx&providerUuid=yyy
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     * @return FormDraftResponse with uuid, formData, and timestamp
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getDraft(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "providerUuid", required = true) String providerUuid) {
        try {
            FormDraft draft = formDraftService.getDraft(patientUuid, providerUuid);
            if (draft == null) {
                return new ResponseEntity<>(new FormDraftResponse(), HttpStatus.OK);
            }

            String formData = formDraftService.getFormData(draft.getFormDataPath());
            Long timestamp = draft.getDateChanged() != null ? draft.getDateChanged().getTime() : draft.getDateCreated().getTime();
            FormDraftResponse response = new FormDraftResponse(draft.getUuid(), formData, draft.getMarkedAsSaved(), timestamp);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid form draft request", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error retrieving form draft", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Mark a form draft as saved (finalized).
     * PATCH /rest/v1/bahmnicore/formdraft?patientUuid=xxx&providerUuid=yyy
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     * @return 200 OK on success
     */
    @RequestMapping(method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Object> markDraftAsSaved(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "providerUuid", required = true) String providerUuid) {
        try {
            formDraftService.markDraftAsSaved(patientUuid, providerUuid);
            log.info("Draft marked as saved for patient: " + patientUuid + " and provider: " + providerUuid);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid form draft request", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error marking draft as saved", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Discard (void) a form draft by patient and provider UUIDs.
     * DELETE /rest/v1/bahmnicore/formdraft?patientUuid=xxx&providerUuid=yyy
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     * @return 204 No Content on success, 403 Forbidden if insufficient privileges
     */
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> discardDraft(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "providerUuid", required = true) String providerUuid) {
        if (!Context.getUserContext().hasPrivilege(PrivilegeConstants.DELETE_FORM_DRAFT_PRIVILEGE)) {
            log.error("User " + Context.getAuthenticatedUser().getUsername() +
                    " does not have privilege to discard form drafts");
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, "Insufficient privileges to discard form draft"),
                    HttpStatus.FORBIDDEN);
        }
        try {
            formDraftService.discardDraft(patientUuid, providerUuid);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid form draft request", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error discarding form draft", e);
            return new ResponseEntity<>(
                    WebUtils.wrapErrorResponse(null, e.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

}
