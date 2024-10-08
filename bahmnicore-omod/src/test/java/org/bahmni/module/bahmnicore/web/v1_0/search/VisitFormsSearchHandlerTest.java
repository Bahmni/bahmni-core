package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver.identifyLocale;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class, LocaleUtility.class, LocaleResolver.class})
@RunWith(PowerMockRunner.class)
public class VisitFormsSearchHandlerTest {

    @InjectMocks
    private VisitFormsSearchHandler visitFormsSearchHandler = new VisitFormsSearchHandler();
    @Mock
    RequestContext context;
    @Mock
    PatientService patientService;
    @Mock
    ConceptService conceptService;
    @Mock
    EncounterService encounterService;
    @Mock
    VisitService visitService;
    @Mock
    ObsService obsService;
    @Mock
    private BahmniProgramWorkflowService programWorkflowService;
    @Mock
    private EpisodeService episodeService;
    private Patient patient;
    private Concept concept;
    private Obs obs;
    private final List<Concept> concepts = new ArrayList<>();
    private final String conceptNames = null;


    @Before
    public void before() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        mockStatic(LocaleResolver.class);
        setUp();
    }

    public Concept createConcept(String conceptName, String locale) {
        concept = new Concept();
        concept.setFullySpecifiedName(new ConceptName(conceptName, new Locale(locale)));
        return concept;
    }

    public Obs createObs(Concept concept) {
        obs = new Obs();
        obs.setConcept(concept);
        return obs;
    }

    public void setUp() throws Exception {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);

        when(context.getLimit()).thenReturn(3);
        when(context.getRequest()).thenReturn(req);
        when(context.getRequest().getSession()).thenReturn(session);
        when(context.getRequest().getParameter("patient")).thenReturn("patientUuid");
        when(context.getRequest().getParameter("numberOfVisits")).thenReturn("10");
        when(context.getRequest().getSession().getAttribute("locale")).thenReturn(Locale.ENGLISH);
        when(identifyLocale(any())).thenReturn(Locale.ENGLISH);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);

        String[] conceptNames = {"Vitals"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);
        patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        PowerMockito.mockStatic(Context.class);
        PowerMockito.when(Context.getPatientService()).thenReturn(patientService);
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);
        concept = createConcept("Vitals", "en");

        PowerMockito.when(identifyLocale(any())).thenReturn(Locale.ENGLISH);

        Visit visit = new Visit();
        PowerMockito.when(Context.getVisitService()).thenReturn(visitService);
        PowerMockito.when(Context.getVisitService().getVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));

        PowerMockito.when(Context.getEncounterService()).thenReturn(encounterService);
        Encounter encounter = mock(Encounter.class);
        PowerMockito.when(encounterService.getEncounters(any(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Collection.class), eq(false))).thenReturn(Arrays.asList(encounter));
        PowerMockito.when(Context.getObsService()).thenReturn(obsService);
        obs = createObs(concept);
    }

    @Test
    public void testGetSearchConfig() throws Exception {
        SearchConfig searchConfig = visitFormsSearchHandler.getSearchConfig();
        assertThat(searchConfig.getId(), is(equalTo("byPatientUuid")));

    }

    @Test
    public void shouldSupportVersions1_10To2() {
        SearchConfig searchConfig = visitFormsSearchHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.10.* - 2.*"));
    }

    @Test
    public void shouldReturnConceptSpecificObsIfConceptNameIsSpecified() {
        String [] conceptNames = new String[]{"Vitals"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);
        concept = createConcept("Vitals", "en");

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(obs));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnConceptSpecificObsIfConceptNameIsFoundInUserLocale() {
        PowerMockito.when(identifyLocale(any())).thenReturn(Locale.FRENCH);

        String [] conceptNames = new String[]{"Vitals_fr"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);

        Concept obsConcept = createConcept("Vitals_fr", "fr");
        Obs obs = createObs(obsConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(obs));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnConceptSpecificObsIfConceptNameIsNullInUserLocaleButFoundInDefaultSearch() {
        PowerMockito.when(identifyLocale(any())).thenReturn(Locale.FRENCH);

        String [] conceptNames = new String[]{"Vitals"};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);

        Concept obsConcept = createConcept("Vitals", "en");
        Obs obs = createObs(obsConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(obs));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnConceptSpecificObsIfConceptNameIsFoundInDefaultLocale() {
        PowerMockito.when(identifyLocale(any())).thenReturn(Locale.FRENCH);

        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);

        Concept parentConcept = new Concept();
        parentConcept.addSetMember(concept);
        Concept historyConcept = createConcept("History and Examination", "en");
        parentConcept.addSetMember(historyConcept);

        when(conceptService.getConceptsByName("All Observation Templates", Locale.ENGLISH,  false)).thenReturn(Arrays.asList(parentConcept));

        Concept obsConcept = createConcept("History and Examination", "en");
        Obs obs = createObs(obsConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(obs));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
    }

    @Test
    public void shouldReturnAllObsIfConceptNameIsNotSpecified() {

        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        Concept parentConcept = new Concept();
        parentConcept.addSetMember(concept);
        Concept historyConcept = createConcept("History and Examination", "en");
        parentConcept.addSetMember(historyConcept);

        when(conceptService.getConceptsByName("All Observation Templates", Locale.ENGLISH,  false)).thenReturn(Arrays.asList(parentConcept));

        Obs obs2 = createObs(historyConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false))).thenReturn(Arrays.asList(obs, obs2));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(2)));
    }

    @Test
    public void shouldReturnEmptyObservationsIfAllConceptNamesAreInvalid() {

        String[] conceptNames = {null, null};
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(conceptNames);

        Concept parentConcept = new Concept();
        parentConcept.addSetMember(concept);
        Concept historyConcept = createConcept("History and Examination", "en");
        parentConcept.addSetMember(historyConcept);

        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);

        Obs obs2 = createObs(historyConcept);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs, obs2));
        NeedsPaging<Obs> searchResults = (NeedsPaging<Obs>) visitFormsSearchHandler.search(context);
        assertThat(searchResults.getPageOfResults().size(), is(equalTo(0)));
    }

    @Test(expected = InvalidSearchException.class)
    public void shouldThrowExceptionIfThePatienUuidIsNull(){
        when(context.getRequest().getParameter("patient")).thenReturn(null);

        visitFormsSearchHandler.search(context);
    }

    @Test
    public void shouldGetObservationsWithinThePatientProgramIfThePatientProgramUuidIsPassed() {
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        when(conceptService.getConceptsByName("conceptNames",Locale.ENGLISH,null)).thenReturn(concepts);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new PatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        Episode episode = new Episode();
        episode.addEncounter(new Encounter());
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConceptsByName("All Observation Templates",Locale.ENGLISH, false);
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Collection.class), eq(false));
        verify(obsService, times(1)).getObservations(any(List.class), any(List.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(false));
    }

    @Test
    public void shouldNotFetchAnyObservationsIfThereIsNoEpisodeForTheProgram() {
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        when(conceptService.getConceptsByName("conceptNames",Locale.ENGLISH,  null)).thenReturn(concepts);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new PatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(null);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConceptsByName("All Observation Templates", Locale.ENGLISH, false);
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false));
        verify(obsService, never()).getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false));
    }

    @Test
    public void shouldNotFetchAnyObservationsIfThereAreNoEncountersInEpisode() {
        when(conceptService.getConceptsByName(conceptNames, Locale.ENGLISH,  null)).thenReturn(concepts);
        when(context.getRequest().getParameterValues("conceptNames")).thenReturn(null);
        String patientProgramUuid = "patient-program-uuid";
        when(context.getRequest().getParameter("patientProgramUuid")).thenReturn(patientProgramUuid);
        when(Context.getService(BahmniProgramWorkflowService.class)).thenReturn(programWorkflowService);
        PatientProgram patientProgram = new PatientProgram();
        when(programWorkflowService.getPatientProgramByUuid(patientProgramUuid)).thenReturn(patientProgram);
        when(Context.getService(EpisodeService.class)).thenReturn(episodeService);
        Episode episode = new Episode();
        when(episodeService.getEpisodeForPatientProgram(patientProgram)).thenReturn(episode);

        PowerMockito.when(obsService.getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false))).thenReturn(Arrays.asList(obs));

        visitFormsSearchHandler.search(context);

        verify(conceptService, times(1)).getConceptsByName("All Observation Templates", Locale.ENGLISH, false);
        verify(programWorkflowService, times(1)).getPatientProgramByUuid(patientProgramUuid);
        verify(episodeService, times(1)).getEpisodeForPatientProgram(patientProgram);
        verify(visitService, never()).getVisitsByPatient(patient);
        verify(encounterService, never()).getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), any(Collection.class), eq(false));
        verify(obsService, never()).getObservations(any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(List.class), any(Integer.class), any(Integer.class), any(Date.class), any(Date.class), eq(false));
    }
}
