package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.model.FormDraft;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.db.DAOException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FormDraftDaoImplTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private FormDraftDaoImpl formDraftDao;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        formDraftDao = new FormDraftDaoImpl();
        formDraftDao.setSessionFactory(sessionFactory);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
    }

    // --- saveOrUpdate ---

    @Test
    public void saveOrUpdate_shouldSaveDraftAndReturnIt() {
        FormDraft draft = new FormDraft();
        draft.setUuid("test-uuid");

        FormDraft result = formDraftDao.saveOrUpdate(draft);

        verify(session).saveOrUpdate(draft);
        assertEquals(draft, result);
    }

    @Test(expected = DAOException.class)
    public void saveOrUpdate_shouldThrowDAOExceptionOnError() {
        FormDraft draft = new FormDraft();
        doThrow(new RuntimeException("DB error")).when(session).saveOrUpdate(any(FormDraft.class));

        formDraftDao.saveOrUpdate(draft);
    }

    // --- getLatestByPatientAndUser ---

    @Test
    public void getLatestByPatientAndUser_shouldReturnDraft() {
        FormDraft expectedDraft = new FormDraft();
        expectedDraft.setUuid("draft-uuid");

        Query<FormDraft> query = mock(Query.class);
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.uniqueResult()).thenReturn(expectedDraft);

        FormDraft result = formDraftDao.getLatestByPatientAndUser(1, 2);

        assertNotNull(result);
        assertEquals("draft-uuid", result.getUuid());
    }

    @Test
    public void getLatestByPatientAndUser_shouldReturnNullWhenNoDraft() {
        Query<FormDraft> query = mock(Query.class);
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.uniqueResult()).thenReturn(null);

        FormDraft result = formDraftDao.getLatestByPatientAndUser(1, 2);

        assertEquals(null, result);
    }

    @Test(expected = DAOException.class)
    public void getLatestByPatientAndUser_shouldThrowDAOExceptionOnError() {
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenThrow(new RuntimeException("DB error"));

        formDraftDao.getLatestByPatientAndUser(1, 2);
    }

    // --- getAllByUserOrderedByDateDesc ---

    @Test
    public void getAllByUserOrderedByDateDesc_shouldReturnDrafts() {
        FormDraft draft1 = new FormDraft();
        draft1.setUuid("uuid-1");
        FormDraft draft2 = new FormDraft();
        draft2.setUuid("uuid-2");

        Query<FormDraft> query = mock(Query.class);
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(draft1, draft2));

        List<FormDraft> result = formDraftDao.getAllByUserOrderedByDateDesc(1);

        assertEquals(2, result.size());
    }

    @Test
    public void getAllByUserOrderedByDateDesc_shouldReturnEmptyList() {
        Query<FormDraft> query = mock(Query.class);
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<FormDraft> result = formDraftDao.getAllByUserOrderedByDateDesc(1);

        assertEquals(0, result.size());
    }

    @Test(expected = DAOException.class)
    public void getAllByUserOrderedByDateDesc_shouldThrowDAOExceptionOnError() {
        when(session.createQuery(anyString(), eq(FormDraft.class))).thenThrow(new RuntimeException("DB error"));

        formDraftDao.getAllByUserOrderedByDateDesc(1);
    }

    // --- getAllNonVoidedFilePaths ---

    @Test
    public void getAllNonVoidedFilePaths_shouldReturnPaths() {
        Query<String> query = mock(Query.class);
        when(session.createQuery(anyString(), eq(String.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList("/path/1.json", "/path/2.json"));

        List<String> result = formDraftDao.getAllNonVoidedFilePaths();

        assertEquals(2, result.size());
    }

    @Test(expected = DAOException.class)
    public void getAllNonVoidedFilePaths_shouldThrowDAOExceptionOnError() {
        when(session.createQuery(anyString(), eq(String.class))).thenThrow(new RuntimeException("DB error"));

        formDraftDao.getAllNonVoidedFilePaths();
    }

    // --- deleteDraftsOlderThanDays ---

    @Test
    public void deleteDraftsOlderThanDays_shouldReturnDeletedCount() {
        Query query = mock(Query.class);
        when(session.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(5);

        Integer result = formDraftDao.deleteDraftsOlderThanDays(30);

        assertEquals(Integer.valueOf(5), result);
    }

    @Test(expected = DAOException.class)
    public void deleteDraftsOlderThanDays_shouldThrowDAOExceptionOnError() {
        when(session.createQuery(anyString())).thenThrow(new RuntimeException("DB error"));

        formDraftDao.deleteDraftsOlderThanDays(30);
    }

    // --- deleteAllDrafts --- (uses Context, tested via PowerMock in task test)

    // --- deleteLatestDraft --- (uses Context, tested via PowerMock in task test)
}
