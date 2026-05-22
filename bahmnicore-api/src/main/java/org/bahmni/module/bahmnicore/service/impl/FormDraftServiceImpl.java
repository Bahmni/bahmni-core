package org.bahmni.module.bahmnicore.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.dao.FormDraftDAO;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class FormDraftServiceImpl implements FormDraftService {

    private static final Logger log = LoggerFactory.getLogger(FormDraftServiceImpl.class);
    private static final String FORM_DRAFTS_SUBDIRECTORY = "form_draft";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private FormDraftDAO formDraftDAO;
    private PatientService patientService;
    private UserService userService;
    private ProviderService providerService;
    private EncounterService encounterService;
    private User authenticatedUser;

    private String formDraftsBasePath;

    public FormDraftServiceImpl() {
        String appDataDir = System.getProperty("OPENMRS_APPLICATION_DATA_DIRECTORY");
        if (appDataDir == null || appDataDir.isEmpty()) {
            throw new IllegalStateException("OPENMRS_APPLICATION_DATA_DIRECTORY system property not set");
        }
        this.formDraftsBasePath = appDataDir + FORM_DRAFTS_SUBDIRECTORY;
    }

    @Autowired
    public void setFormDraftDAO(FormDraftDAO formDraftDAO) {
        this.formDraftDAO = formDraftDAO;
    }

    @Autowired(required = false)
    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    @Autowired(required = false)
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired(required = false)
    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Autowired(required = false)
    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    protected void setFormDraftsBasePath(String basePath) {
        this.formDraftsBasePath = basePath;
    }

    protected void setAuthenticatedUser(User user) {
        this.authenticatedUser = user;
    }

    private User getAuthenticatedUser() {
        return authenticatedUser != null ? authenticatedUser : Context.getAuthenticatedUser();
    }

    /**
     * Resolves a User from a Provider UUID via Provider → Person → User lookup.
     */
    private User resolveUser(String providerUuid) {
        ProviderService ps = this.providerService != null ? this.providerService : Context.getProviderService();
        Provider provider = ps.getProviderByUuid(providerUuid);
        if (provider != null && provider.getPerson() != null) {
            UserService us = userService != null ? userService : Context.getUserService();
            Collection<User> users = us.getUsersByPerson(provider.getPerson(), false);
            if (users != null && !users.isEmpty()) {
                return users.iterator().next();
            }
        }
        return null;
    }

    @Override
    public FormDraft saveDraft(FormDraftRequest request) {
        try {
            validateRequest(request);

            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(request.getPatientUuid());
            if (patient == null) {
                throw new APIException("Patient not found with UUID: " + request.getPatientUuid());
            }

            User user = resolveUser(request.getProviderUuid());
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + request.getProviderUuid());
            }

            FormDraft draft = formDraftDAO.getLatestByPatientAndUser(patient.getPatientId(), user.getUserId());
            boolean isNewDraft = (draft == null);
            boolean contentChanged = true;

            if (draft != null && draft.getMarkedAsSaved() != null && draft.getMarkedAsSaved()) {
                isNewDraft = true;
                draft = null;
            }

            if (draft == null) {
                draft = new FormDraft();
                draft.setUuid(UUID.randomUUID().toString());
                draft.setDateCreated(new Date());
                draft.setCreator(getAuthenticatedUser());
                draft.setMarkedAsSaved(false);
            } else {
                contentChanged = hasFormDataChanged(draft.getFormDataPath(), request.getFormData());
                if (contentChanged) {
                    draft.setDateChanged(new Date());
                    draft.setChangedBy(getAuthenticatedUser());
                }
            }

            draft.setPatient(patient);
            draft.setUser(user);

            if (request.getEncounterUuid() != null && !request.getEncounterUuid().isEmpty()) {
                EncounterService es = encounterService != null ? encounterService : Context.getEncounterService();
                Encounter encounter = es.getEncounterByUuid(request.getEncounterUuid());
                if (encounter != null) {
                    draft.setEncounter(encounter);
                } else {
                    log.warn("Encounter UUID provided but not found: " + request.getEncounterUuid());
                }
            }

            String filePath = generateFilePath(draft.getUuid());
            if (isNewDraft || contentChanged) {
                writeFormDataToFile(filePath, request.getFormData());
            }
            draft.setFormDataPath(filePath);

            return formDraftDAO.saveOrUpdate(draft);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            log.error("Error writing form draft file", e);
            throw new RuntimeException("Failed to save form draft file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error saving form draft", e);
            throw new RuntimeException("Failed to save form draft: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the form data content has changed by comparing with existing file.
     * Returns true if content differs or file doesn't exist.
     */
    private boolean hasFormDataChanged(String filePath, String newFormData) {
        if (filePath == null || newFormData == null) {
            return true;
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return true;
            }

            String existingContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return !existingContent.equals(newFormData);
        } catch (IOException e) {
            log.warn("Error reading existing form data file, assuming content changed", e);
            return true;
        }
    }

    /**
     * Validate that required fields are present and not empty.
     */
    private void validateRequest(FormDraftRequest request) {
        if (request.getPatientUuid() == null || request.getPatientUuid().isEmpty()) {
            throw new IllegalArgumentException("Patient UUID is required");
        }
        if (request.getProviderUuid() == null || request.getProviderUuid().isEmpty()) {
            throw new IllegalArgumentException("Provider UUID is required");
        }
        if (request.getFormData() == null || request.getFormData().isEmpty()) {
            throw new IllegalArgumentException("Form data is required");
        }
    }

    /**
     * Generate file path for form draft data using UUID.
     * Format: {OPENMRS_APPLICATION_DATA_DIRECTORY}/form_draft/{draftUuid}.json
     */
    private String generateFilePath(String draftUuid) {
        return String.format("%s%s%s.json",
                formDraftsBasePath,
                File.separator,
                draftUuid);
    }

    /**
     * Write form data JSON to file atomically.
     * Uses temp file + rename to ensure consistency.
     */
    private void writeFormDataToFile(String filePath, String formData) throws IOException {
        File targetFile = new File(filePath);
        File parentDir = targetFile.getParentFile();

        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        String tempPath = filePath + ".tmp";
        File tempFile = new File(tempPath);

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            writer.write(formData);
            writer.flush();
        } catch (IOException e) {
            tempFile.delete();
            throw e;
        }

        if (!tempFile.renameTo(targetFile)) {
            tempFile.delete();
            throw new IOException("Failed to finalize form data file: " + filePath);
        }
    }

    /**
     * Retrieve the latest non-voided form draft for a patient and provider.
     */
    @Override
    public FormDraft getDraft(String patientUuid, String providerUuid) {
        try {
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                return null;
            }

            User user = resolveUser(providerUuid);
            if (user == null) {
                return null;
            }

            return formDraftDAO.getLatestByPatientAndUser(patient.getPatientId(), user.getUserId());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving form draft", e);
            throw new RuntimeException("Failed to retrieve form draft: " + e.getMessage(), e);
        }
    }

    @Override
    public void discardDraft(String patientUuid, String providerUuid) {
        try {
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                throw new APIException("Patient not found with UUID: " + patientUuid);
            }

            User user = resolveUser(providerUuid);
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + providerUuid);
            }

            formDraftDAO.deleteLatestDraft(patient.getPatientId(), user.getUserId());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error discarding form draft", e);
            throw new RuntimeException("Failed to discard form draft: " + e.getMessage(), e);
        }
    }

    @Override
    public void discardAllDrafts() {
        formDraftDAO.deleteAllDrafts();
    }

    @Override
    public String getFormData(String formDataPath) {
        if (formDataPath == null) {
            return null;
        }

        try {
            File file = new File(formDataPath);
            if (!file.exists()) {
                return null;
            }
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Error reading form data file: " + formDataPath, e);
            return null;
        }
    }

    @Override
    public List<FormDraftSummaryResponse> getDraftsByProvider(String providerUuid) {
        if (providerUuid == null || providerUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider UUID is required");
        }

        User user = resolveUser(providerUuid);
        if (user == null) {
            log.warn("getDraftsByProvider: no user found for providerUuid={}", providerUuid);
            return new ArrayList<>();
        }

        List<FormDraft> drafts = formDraftDAO.getAllByUserOrderedByDateDesc(user.getUserId());
        List<FormDraftSummaryResponse> results = new ArrayList<>();
        for (FormDraft draft : drafts) {
            FormDraftSummaryResponse summary = buildSummary(draft);
            if (summary != null) {
                results.add(summary);
            }
        }
        return results;
    }

    private FormDraftSummaryResponse buildSummary(FormDraft draft) {
        Patient patient = draft.getPatient();
        if (patient == null) {
            log.warn("buildSummary: draft {} has null patient — skipping", draft.getUuid());
            return null;
        }

        String patientName = patient.getPersonName() != null
                ? patient.getPersonName().getFullName()
                : "";
        String patientIdentifier = patient.getPatientIdentifier() != null
                ? patient.getPatientIdentifier().getIdentifier()
                : null;
        String encounterUuid = draft.getEncounter() != null ? draft.getEncounter().getUuid() : null;
        long timestamp = draft.getDateChanged() != null
                ? draft.getDateChanged().getTime()
                : draft.getDateCreated().getTime();

        String formName = extractFormName(draft.getFormDataPath());

        FormDraftSummaryResponse response = new FormDraftSummaryResponse();
        response.setDraftUuid(draft.getUuid());
        response.setPatientUuid(patient.getUuid());
        response.setPatientName(patientName);
        response.setPatientIdentifier(patientIdentifier);
        response.setEncounterUuid(encounterUuid);
        response.setFormName(formName);
        response.setTimestamp(timestamp);
        return response;
    }

    private String extractFormName(String formDataPath) {
        String formData = getFormData(formDataPath);
        if (formData == null || formData.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(formData);
            if (!root.isArray()) {
                log.warn("extractFormName: expected observations array but got object at path={}", formDataPath);
                return null;
            }
            for (JsonNode obs : root) {
                String formFieldPath = obs.path("formFieldPath").asText(null);
                if (formFieldPath != null && !formFieldPath.isEmpty()) {
                    return formFieldPath.split("\\.")[0];
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("extractFormName: failed to parse form data at path={}", formDataPath, e);
            return null;
        }
    }

    @Override
    public void markDraftAsSaved(String patientUuid, String providerUuid) {
        try {
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                throw new APIException("Patient not found with UUID: " + patientUuid);
            }

            User user = resolveUser(providerUuid);
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + providerUuid);
            }

            FormDraft draft = formDraftDAO.getLatestByPatientAndUser(patient.getPatientId(), user.getUserId());
            if (draft != null) {
                draft.setMarkedAsSaved(true);
                draft.setDateChanged(new Date());
                draft.setChangedBy(getAuthenticatedUser());
                formDraftDAO.saveOrUpdate(draft);
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error marking form draft as saved", e);
            throw new RuntimeException("Failed to mark form draft as saved: " + e.getMessage(), e);
        }
    }
}
