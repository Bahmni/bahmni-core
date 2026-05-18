package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.FormDraftDAO;
import org.bahmni.module.bahmnicore.model.FormDraft;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class FormDraftDaoImpl implements FormDraftDAO {

    private static final Logger log = LoggerFactory.getLogger(FormDraftDaoImpl.class);

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public FormDraft saveOrUpdate(FormDraft draft) throws DAOException {
        try {
            sessionFactory.getCurrentSession().saveOrUpdate(draft);
            return draft;
        } catch (Exception e) {
            log.error("Error saving or updating form draft", e);
            throw new DAOException("Failed to save or update form draft", e);
        }
    }

    @Override
    public FormDraft getLatestByPatientAndUser(Integer patientId, Integer userId) throws DAOException {
        try {
            Query<FormDraft> query = sessionFactory.getCurrentSession()
                    .createQuery("FROM FormDraft WHERE patient.patientId = :patientId AND user.userId = :userId " +
                            "AND voided = false ORDER BY dateCreated DESC", FormDraft.class);
            query.setParameter("patientId", patientId);
            query.setParameter("userId", userId);
            query.setMaxResults(1);
            return query.uniqueResult();
        } catch (Exception e) {
            log.error("Error retrieving latest form draft for patient: " + patientId + ", user: " + userId, e);
            throw new DAOException("Failed to retrieve form draft", e);
        }
    }

    @Override
    public void deleteLatestDraft(Integer patientId, Integer userId) throws DAOException {
        try {
            FormDraft draft = getLatestByPatientAndUser(patientId, userId);
            if (draft != null) {
                draft.setVoided(true);
                draft.setDateVoided(new Date());
                draft.setVoidedBy(Context.getAuthenticatedUser());
                draft.setVoidReason("Draft deleted");
                sessionFactory.getCurrentSession().saveOrUpdate(draft);
            }
        } catch (DAOException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting latest form draft for patient: " + patientId + ", user: " + userId, e);
            throw new DAOException("Failed to delete form draft", e);
        }
    }

    @Override
    public List<FormDraft> getAllByUserOrderedByDateDesc(Integer userId) throws DAOException {
        try {
            Query<FormDraft> query = sessionFactory.getCurrentSession()
                    .createQuery("FROM FormDraft WHERE user.userId = :userId " +
                            "AND voided = false " +
                            "AND (markedAsSaved IS NULL OR markedAsSaved = false) " +
                            "ORDER BY COALESCE(dateChanged, dateCreated) DESC", FormDraft.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            log.error("Error retrieving all form drafts for user: " + userId, e);
            throw new DAOException("Failed to retrieve form drafts for user", e);
        }
    }
}
