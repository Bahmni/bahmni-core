package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftResponse;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.bahmni.module.bahmnicore.util.WebUtils;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
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

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/formdraft")
public class FormDraftController extends BaseRestController {

    private static final Logger log = LoggerFactory.getLogger(FormDraftController.class);

    @Autowired
    private FormDraftService formDraftService;

    @Autowired(required = false)
    private ProviderService providerService;

    protected User getAuthenticatedUser() {
        return Context.getAuthenticatedUser();
    }

    private String resolveAuthenticatedProviderUuid() {
        User user = getAuthenticatedUser();
        if (user == null || user.getPerson() == null) {
            return null;
        }
        ProviderService ps = providerService != null ? providerService : Context.getProviderService();
        Collection<Provider> providers = ps.getProvidersByPerson(user.getPerson(), false);
        if (providers == null || providers.isEmpty()) {
            return null;
        }
        return providers.iterator().next().getUuid();
    }

    private ResponseEntity<Object> forbiddenResponse() {
        return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, "No provider associated with authenticated user"), HttpStatus.FORBIDDEN);
    }

    /**
     * List all unsaved drafts for the authenticated provider.
     * GET /rest/v1/bahmnicore/formdraft/list
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getDraftsByProvider() {
        try {
            String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
            if (resolvedProviderUuid == null) {
                return forbiddenResponse();
            }
            List<FormDraftSummaryResponse> drafts = formDraftService.getDraftsByProvider(resolvedProviderUuid);
            return new ResponseEntity<>(drafts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for draft list", e);
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error retrieving draft list for authenticated provider", e);
            return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Auto-save a form draft. Upserts by patient and provider UUID.
     * POST /rest/v1/bahmnicore/formdraft
     *
     * @param request FormDraftRequest with patientUuid and formData
     * @return FormDraftResponse with uuid, formData, markedAsSaved flag, and timestamp
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Object> saveDraft(@RequestBody FormDraftRequest request) {
        try {
            String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
            if (resolvedProviderUuid == null) {
                return forbiddenResponse();
            }
            FormDraft draft = formDraftService.saveDraft(request, resolvedProviderUuid);
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
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieve a form draft by patient UUID for the authenticated provider.
     * GET /rest/v1/bahmnicore/formdraft?patientUuid=xxx
     *
     * @param patientUuid the UUID of the patient
     * @return FormDraftResponse with uuid, formData, and timestamp
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getDraft(
            @RequestParam(value = "patientUuid", required = true) String patientUuid) {
        try {
            String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
            if (resolvedProviderUuid == null) {
                return forbiddenResponse();
            }
            FormDraft draft = formDraftService.getDraft(patientUuid, resolvedProviderUuid);
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
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Mark a form draft as saved (finalized).
     * PATCH /rest/v1/bahmnicore/formdraft?patientUuid=xxx
     *
     * @param patientUuid the UUID of the patient
     * @return 200 OK on success
     */
    @RequestMapping(method = RequestMethod.PATCH)
    @ResponseBody
    public ResponseEntity<Object> markDraftAsSaved(
            @RequestParam(value = "patientUuid", required = true) String patientUuid) {
        try {
            String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
            if (resolvedProviderUuid == null) {
                return forbiddenResponse();
            }
            formDraftService.markDraftAsSaved(patientUuid, resolvedProviderUuid);
            log.info("Draft marked as saved for patient: {} and provider: {}", patientUuid.replaceAll("[\\r\\n]", ""), resolvedProviderUuid);
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
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Discard (void) a form draft by patient UUID for the authenticated provider.
     * DELETE /rest/v1/bahmnicore/formdraft?patientUuid=xxx
     *
     * @param patientUuid the UUID of the patient
     * @return 204 No Content on success
     */
    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Object> discardDraft(
            @RequestParam(value = "patientUuid", required = true) String patientUuid) {
        try {
            String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
            if (resolvedProviderUuid == null) {
                return forbiddenResponse();
            }
            formDraftService.discardDraft(patientUuid, resolvedProviderUuid);
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
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
