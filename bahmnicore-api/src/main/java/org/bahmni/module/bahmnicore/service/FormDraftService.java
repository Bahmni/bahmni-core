package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.contract.FormDraftSummaryResponse;
import org.bahmni.module.bahmnicore.model.FormDraft;

import java.util.List;

public interface FormDraftService {

    /**
     * Create a new form draft. Each draft gets a unique UUID.
     *
     * @param request FormDraftRequest containing patient, provider, and form data
     * @return the created FormDraft object with generated UUID
     */
    FormDraft saveDraft(FormDraftRequest request);

    /**
     * Retrieve the latest non-voided form draft for a patient and provider.
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     * @return the latest FormDraft object if found, null otherwise
     */
    FormDraft getDraft(String patientUuid, String providerUuid);

    /**
     * Soft delete (void) the latest non-voided form draft for a patient and provider.
     * Sets voided=true and updates audit fields (dateVoided, voidedBy, voidReason).
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     */
    void discardDraft(String patientUuid, String providerUuid);

    /**
     * Retrieve form data from file using UTF-8 charset.
     *
     * @param formDataPath the file path to read from
     * @return the form data as a string, or null if file doesn't exist
     */
    String getFormData(String formDataPath);

    /**
     * Mark the latest form draft as saved for a patient and provider.
     * Sets markedAsSaved=true so subsequent saves will create a new draft.
     *
     * @param patientUuid the UUID of the patient
     * @param providerUuid the UUID of the provider
     */
    void markDraftAsSaved(String patientUuid, String providerUuid);

    /**
     * Soft delete (void) all non-voided form drafts regardless of markedAsSaved value.
     * Intended to be called by a scheduled task at midnight.
     */
    void discardAllDrafts();

    /**
     * Retrieve a summary list of all unsaved drafts for a given provider.
     * Reads formData to extract formUuid/formName where available.
     * Drafts with missing patient name or identifier are skipped with a warning log.
     *
     * @param providerUuid the UUID of the provider
     * @return list of FormDraftSummaryResponse, ordered newest first; empty list if provider not found
     */
    List<FormDraftSummaryResponse> getDraftsByProvider(String providerUuid);

    /**
     * Delete all form drafts older than the configured retention period, regardless of voided status.
     * The retention period is read from global property 'bahmni.formDraft.voidedRetentionDays'.
     * The property is initialized to 15 days by the Liquibase changeset during module deployment.
     * Intended to be called by a scheduled task at midnight.
     */
    void deleteDraftsOlderThanRetentionPeriod();
}
