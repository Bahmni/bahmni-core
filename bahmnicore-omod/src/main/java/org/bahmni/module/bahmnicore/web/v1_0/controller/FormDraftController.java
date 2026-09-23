package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftResponse;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.bahmni.module.bahmnicore.util.WebUtils;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/formdraft")
public class FormDraftController extends BaseRestController {

    private static final Logger log = LoggerFactory.getLogger(FormDraftController.class);

    private final FormDraftService formDraftService;
    private final ProviderService providerService;

    @Autowired
    public FormDraftController(FormDraftService formDraftService, ProviderService providerService) {
        this.formDraftService = formDraftService;
        this.providerService = providerService;
    }

    @ExceptionHandler(APIAuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(APIAuthenticationException e) {
        return new ResponseEntity<>(WebUtils.wrapErrorResponse(null, e.getMessage()), HttpStatus.FORBIDDEN);
    }

    protected User getAuthenticatedUser() {
        return Context.getAuthenticatedUser();
    }

    private String resolveAuthenticatedProviderUuid() {
        User user = getAuthenticatedUser();
        if (user == null || user.getPerson() == null) {
            throw new APIAuthenticationException("No provider associated with authenticated user");
        }
        Collection<Provider> providers = providerService.getProvidersByPerson(user.getPerson(), false);
        if (providers == null || providers.isEmpty()) {
            throw new APIAuthenticationException("No provider associated with authenticated user");
        }
        Provider provider = providers.iterator().next();
        if (provider == null || provider.getUuid() == null) {
            throw new APIAuthenticationException("No provider associated with authenticated user");
        }
        return provider.getUuid();
    }

    @GetMapping(value = "/list")
    public ResponseEntity<Object> getDraftsByProvider() {
        String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
        try {
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

    @PostMapping
    public ResponseEntity<Object> saveDraft(@RequestBody FormDraftRequest request) {
        String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
        try {
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

    @GetMapping
    public ResponseEntity<Object> getDraft(@RequestParam(value = "patientUuid") String patientUuid) {
        String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
        try {
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

    @PatchMapping
    public ResponseEntity<Object> markDraftAsSaved(@RequestParam(value = "patientUuid") String patientUuid) {
        String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
        try {
            formDraftService.markDraftAsSaved(patientUuid, resolvedProviderUuid);
            log.info("Draft marked as saved for patient: {} and provider: {}",
                    patientUuid.replaceAll("[\r\n]", ""), resolvedProviderUuid.replaceAll("[\r\n]", ""));
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

    @DeleteMapping
    public ResponseEntity<Object> discardDraft(@RequestParam(value = "patientUuid") String patientUuid) {
        String resolvedProviderUuid = resolveAuthenticatedProviderUuid();
        try {
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
