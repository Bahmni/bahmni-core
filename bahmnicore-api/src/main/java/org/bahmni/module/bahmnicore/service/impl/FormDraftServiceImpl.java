package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.dao.FormDraftDAO;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.openmrs.api.context.Context;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

@Transactional
public class FormDraftServiceImpl implements FormDraftService {

    private static final Logger log = LoggerFactory.getLogger(FormDraftServiceImpl.class);
    private static final String FORM_DRAFTS_SUBDIRECTORY = "form_draft";

    private FormDraftDAO formDraftDAO;
    private PatientService patientService;
    private UserService userService;
    private EncounterService encounterService;
    private User authenticatedUser;  // For testing - overrides Context.getAuthenticatedUser()

    // For testing purposes - can be overridden
    private String formDraftsBasePath;

    public FormDraftServiceImpl() {
        // Initialize with OPENMRS_APPLICATION_DATA_DIRECTORY
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
    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    // Package-private setters for testing
    protected void setFormDraftsBasePath(String basePath) {
        this.formDraftsBasePath = basePath;
    }

    protected void setAuthenticatedUser(User user) {
        this.authenticatedUser = user;
    }

    private User getAuthenticatedUser() {
        return authenticatedUser != null ? authenticatedUser : Context.getAuthenticatedUser();
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

            UserService us = userService != null ? userService : Context.getUserService();
            User user = us.getUserByUuid(request.getProviderUuid());
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + request.getProviderUuid());
            }

            FormDraft draft = formDraftDAO.getLatestByPatientAndUser(patient.getPatientId(), user.getUserId());
            boolean isNewDraft = (draft == null);
            boolean contentChanged = true;

            // If the latest draft is marked as saved, create a new draft instead of updating
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
                return true;  // File doesn't exist, so content is new
            }

            String existingContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return !existingContent.equals(newFormData);
        } catch (IOException e) {
            log.warn("Error reading existing form data file, assuming content changed", e);
            return true;  // If we can't read, assume it changed to be safe
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
            // Validate required fields
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            // Fetch entities to get their IDs
            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                return null;
            }

            UserService us = userService != null ? userService : Context.getUserService();
            User user = us.getUserByUuid(providerUuid);
            if (user == null) {
                return null;
            }

            // Query by patient ID and user ID
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
            // Validate required fields
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            // Fetch entities to get their IDs
            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                throw new APIException("Patient not found with UUID: " + patientUuid);
            }

            UserService us = userService != null ? userService : Context.getUserService();
            User user = us.getUserByUuid(providerUuid);
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + providerUuid);
            }

            // Delete (void) latest draft for this patient-provider pair
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
    public void markDraftAsSaved(String patientUuid, String providerUuid) {
        try {
            // Validate required fields
            if (patientUuid == null || patientUuid.isEmpty()) {
                throw new IllegalArgumentException("Patient UUID is required");
            }
            if (providerUuid == null || providerUuid.isEmpty()) {
                throw new IllegalArgumentException("Provider UUID is required");
            }

            // Fetch entities to get their IDs
            PatientService ps = patientService != null ? patientService : Context.getPatientService();
            Patient patient = ps.getPatientByUuid(patientUuid);
            if (patient == null) {
                throw new APIException("Patient not found with UUID: " + patientUuid);
            }

            UserService us = userService != null ? userService : Context.getUserService();
            User user = us.getUserByUuid(providerUuid);
            if (user == null) {
                throw new APIException("User/Provider not found with UUID: " + providerUuid);
            }

            // Get latest draft and mark as saved
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
