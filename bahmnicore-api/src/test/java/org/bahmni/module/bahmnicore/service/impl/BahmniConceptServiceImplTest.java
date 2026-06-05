package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniConceptServiceImplTest {

    public static final String QUESTION = "question";
    public static final String SEARCH_QUERY = "q";
    @Mock
    private BahmniConceptDao bahmniConceptDao;
    @Mock
    private ConceptService conceptService;
    private BahmniConceptServiceImpl bahmniConceptService;

    @Before
    public void setUp() {
        initMocks(this);
        bahmniConceptService = new BahmniConceptServiceImpl(conceptService, bahmniConceptDao);
    }

    @Test
    public void searchByQuestionShouldUseBahmniConceptDaoToSearchConcepts() {
        Concept questionConcept = new Concept();
        when(bahmniConceptDao.getConceptByFullySpecifiedName(QUESTION)).thenReturn(questionConcept);
        ConceptAnswer resultConceptAnswer = new ConceptAnswer();
        when(bahmniConceptDao.searchByQuestion(questionConcept, SEARCH_QUERY)).thenReturn(Arrays.asList(resultConceptAnswer));

        Collection<ConceptAnswer> conceptAnswers = bahmniConceptService.searchByQuestion(QUESTION, SEARCH_QUERY);
        assertThat(conceptAnswers.size(), is(equalTo(1)));
        assertThat(conceptAnswers.iterator().next().getUuid(), is(equalTo(resultConceptAnswer.getUuid())));
    }

    @Test(expected = ConceptNotFoundException.class)
    public void searchByQuestionShouldThrowExceptionWhenQuestionConceptNotFound() throws Exception {
        bahmniConceptService.searchByQuestion("this concept doesn't exist", "headache");
    }

    @Test
    public void getDrugsByConceptSetNameShouldRetrieveAllDrugsForMembersOfAConceptSet() {
        Concept allTBDrugsConceptSet = new Concept();
        List<Concept> allTBDrugConcepts = Arrays.asList(new Concept(), new Concept());
        String conceptSetName = "All TB Drugs";
        List<Drug> allTBDrugs = Arrays.asList(new Drug(), new Drug());

        when(bahmniConceptDao.getConceptByFullySpecifiedName(conceptSetName)).thenReturn(allTBDrugsConceptSet);
        when(conceptService.getConceptsByConceptSet(allTBDrugsConceptSet)).thenReturn(allTBDrugConcepts);
        when(bahmniConceptDao.searchDrugsByDrugName(allTBDrugsConceptSet.getId(), null)).thenReturn(allTBDrugs);

        Collection<Drug> drugs = bahmniConceptService.getDrugsByConceptSetName(conceptSetName, null);

        assertThat(drugs, containsInAnyOrder(allTBDrugs.toArray()));
    }

    @Test(expected = ConceptNotFoundException.class)
    public void getDrugsByConceptSetNameShouldFailWhenConceptSetNameDoesNotExist() {
        bahmniConceptService.getDrugsByConceptSetName("this concept doesn't exist", null);
    }

    @Test
    public void shouldMakeACallToGetConceptByFullySpecifiedName() throws Exception {
        Concept expectedConcept = new Concept();
        String conceptName = "Concept Name";
        when(bahmniConceptDao.getConceptByFullySpecifiedName(conceptName)).thenReturn(expectedConcept);

        Concept actualConcept = bahmniConceptService.getConceptByFullySpecifiedName(conceptName);

        verify(bahmniConceptDao, times(1)).getConceptByFullySpecifiedName(conceptName);
        assertEquals(expectedConcept, actualConcept);
    }

    @Test
    public void shouldReturnEmptyConceptsListIfConceptNamesListIsEmpty() throws Exception {

        List<Concept> concepts = bahmniConceptService.getConceptsByFullySpecifiedName(new ArrayList<String>());
        assertEquals(0, concepts.size());
    }

    @Test
    public void shouldGetListOfConceptsByTakingListOfNamesAsParameters() throws Exception {

        List<String> conceptNames = new ArrayList<String>();
        conceptNames.add("concept1");
        conceptNames.add("concept2");
        List<Concept> conceptList = new ArrayList<>();
        conceptList.add(new Concept(1));
        conceptList.add(new Concept(2));
        when(bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames)).thenReturn(conceptList);

        List<Concept> concepts = bahmniConceptService.getConceptsByFullySpecifiedName(conceptNames);

        verify(bahmniConceptDao, times(1)).getConceptsByFullySpecifiedName(conceptNames);
        assertEquals(2, concepts.size());
        assertEquals(1, concepts.get(0).getConceptId().intValue());
        assertEquals(2, concepts.get(1).getConceptId().intValue());
    }

    @Test
    public void shouldGetEmptyListIfListOfNamesIsNull() throws Exception {
        List<Concept> concepts = bahmniConceptService.getConceptsByFullySpecifiedName(null);

        assertEquals(0, concepts.size());
    }
}
