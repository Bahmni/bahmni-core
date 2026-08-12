package org.bahmni.module.bahmnicore.dao;

import java.util.List;

import org.bahmni.module.bahmnicore.model.FormDraft;

public interface FormDraftDAO {

    /**
     * Save a new form draft.
     * Each draft gets a unique UUID and is never updated once voided (new drafts are created instead).
     *
     * @param draft the FormDraft object to save or update
     * @return the saved FormDraft object
     */
    FormDraft saveOrUpdate(FormDraft draft);

    /**
     * Retrieve the latest non-voided form draft for a patient and user.
     *
     * @param patientId the OpenMRS patient ID
     * @param userId the OpenMRS user ID (provider)
     * @return the latest non-voided FormDraft object, or null if not found
     */
    FormDraft getLatestByPatientAndUser(Integer patientId, Integer userId);

    /**
     * Soft delete (void) the latest non-voided form draft for a patient and user.
     * Sets voided = true and dateVoided = now, voidedBy = currentUser, voidReason = "Draft deleted"
     *
     * @param patientId the OpenMRS patient ID
     * @param userId the OpenMRS user ID (provider)
     */
    void deleteLatestDraft(Integer patientId, Integer userId);

    /**
     * Soft delete (void) all non-voided form drafts.
     * Sets voided = true and dateVoided = now, voidedBy = currentUser, voidReason = "Draft deleted by scheduler"
     */
    void deleteAllDrafts();

    /**
     * Retrieve all non-voided, unsaved drafts for a user, ordered newest first.
     * Drafts where markedAsSaved is true are excluded.
     *
     * @param userId the OpenMRS user ID (provider)
     * @return list of FormDraft objects, ordered by COALESCE(dateChanged, dateCreated) DESC
     */
    List<FormDraft> getAllByUserOrderedByDateDesc(Integer userId);

    /**
     * Permanently delete (hard delete) all form drafts older than the specified number of days.
     *
     * @param retentionDays the number of days to retain drafts
     * @return the number of draft records deleted
     */
    Integer deleteDraftsOlderThanDays(Integer retentionDays);
}
