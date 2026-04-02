package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.FormDraftRequest;
import org.bahmni.module.bahmnicore.model.FormDraft;

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
}
