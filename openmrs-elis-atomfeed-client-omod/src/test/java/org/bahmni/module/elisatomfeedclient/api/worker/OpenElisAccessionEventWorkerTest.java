package org.bahmni.module.elisatomfeedclient.api.worker;

import org.bahmni.module.elisatomfeedclient.api.Constants;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisAccessionBuilder;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisTestDetailBuilder;
import org.bahmni.module.elisatomfeedclient.api.domain.AccessionDiff;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.bahmni.module.elisatomfeedclient.api.mapper.AccessionHelper;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.auditlog.service.AuditLogService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.impl.BahmniVisitAttributeService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, OpenmrsUtil.class})
public class OpenElisAccessionEventWorkerTest {
    @Mock
    private HttpClient httpClient;
    @Mock
    private EncounterService encounterService;
    @Mock
    private AccessionHelper accessionMapper;
    @Mock
    private ElisAtomFeedProperties feedProperties;
    @Mock
    private ConceptService conceptService;
    @Mock
    private ProviderService providerService;
    @Mock
    private BahmniVisitAttributeService bahmniVisitAttributeSaveCommand;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AdministrationService administrationService;
    @Mock
    private UserContext userContext;

    private OpenElisAccessionEventWorker accessionEventWorker;
    private String openElisUrl;
    private Event event;

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        accessionEventWorker = new OpenElisAccessionEventWorker(feedProperties, httpClient, encounterService,
                conceptService, accessionMapper, providerService, bahmniVisitAttributeSaveCommand, auditLogService);
        openElisUrl = "http://localhost:8080";
        event = new Event("id", "/openelis/accession/12-34-56-78", "title", "feedUri", new Date());
        when(feedProperties.getOpenElisUri()).thenReturn(openElisUrl);
        when(administrationService.getGlobalProperty("bahmni.enableAuditLog")).thenReturn("true");
        when(Context.getAdministrationService()).thenReturn(administrationService);
        PowerMockito.mockStatic(OpenmrsUtil.class);
        PowerMockito.when(OpenmrsUtil.getApplicationDataDirectory()).thenReturn(getClass().getClassLoader().getResource("").getPath());
    }

    @Test
    public void shouldSaveEncounterWhenEncounterForGivenAccessionDoesNotExists() throws Exception {
        final Encounter encounter = getEncounterWithTests("test1");
        EncounterType encounterType = new EncounterType(Constants.DEFAULT_INVESTIGATION_ENCOUNTER_TYPE, "OpenElis Order Encounter");
        encounter.setEncounterType(encounterType);
        final Visit visit = new Visit();
        visit.setId(1);
        encounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Arrays.asList(encounter)));
        final OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().build();
        stubAccession(openElisAccession);

        // first time when it calls it should return null as there is no encounter at that point
        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(null).thenReturn(encounter);
        when(accessionMapper.mapToNewEncounter(any(OpenElisAccession.class), any(String.class))).thenReturn(encounter);
        when(accessionMapper.findOrInitializeVisit(any(Patient.class), any(Date.class), any(String.class))).thenReturn(visit);
        when(encounterService.saveEncounter(encounter)).thenReturn(encounter);
        accessionEventWorker.process(event);

        verify(encounterService).saveEncounter(encounter);
        verify(bahmniVisitAttributeSaveCommand).save(encounter);
        Map<String, String> params = new HashMap<>();
        params.put("encounterUuid", encounter.getUuid());
        params.put("encounterType", encounter.getEncounterType().getName());
        verify(auditLogService).createAuditLog(encounter.getPatient().getUuid(),"EDIT_ENCOUNTER", "EDIT_ENCOUNTER_MESSAGE", params, "OpenElis");
    }

    @Test
    public void shouldUpdateEncounterWhenAccessionHasNewOrder() throws Exception {
        Encounter previousEncounter = getEncounterWithTests("test1");
        Encounter encounterFromAccession = getEncounterWithTests("test1", "test2");
        final Visit visit = new Visit();
        visit.setId(1);
        previousEncounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Arrays.asList(previousEncounter)));
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).build();
        stubAccession(openElisAccession);
        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(previousEncounter);
        when(accessionMapper.addOrDiscontinueOrderDifferences(any(OpenElisAccession.class), any(AccessionDiff.class), any(Encounter.class))).thenReturn(encounterFromAccession);
        when(accessionMapper.findOrInitializeVisit(any(Patient.class), any(Date.class), any(String.class))).thenReturn(visit);
        when(encounterService.saveEncounter(previousEncounter)).thenReturn(previousEncounter);
        accessionEventWorker.process(event);

        verify(encounterService, times(2)).getEncounterByUuid(openElisAccession.getAccessionUuid());
        verify(encounterService).saveEncounter(previousEncounter);
        verify(bahmniVisitAttributeSaveCommand).save(previousEncounter);
    }

    @Test
    public void shouldUpdateEncounterWithAccessionNumberWhenItsGenerated() throws Exception {
        Encounter orderEncounter = getEncounterWithTests("test1", "test2", "test3");
        final Visit visit = new Visit();
        visit.setId(1);
        orderEncounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Arrays.asList(orderEncounter)));
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").build();
        test1.setStatus("Not started");
        test2.setStatus("Canceled");
        test3.setStatus("Not started");
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();

        stubAccession(openElisAccession);
        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(orderEncounter);
        when(accessionMapper.findOrInitializeVisit(any(Patient.class), any(Date.class), any(String.class))).thenReturn(visit);
        when(encounterService.saveEncounter(orderEncounter)).thenReturn(orderEncounter);
        accessionEventWorker.associateTestResultsToOrder(openElisAccession);

        List<String> orderAccessionNumbers = orderEncounter.getOrders().stream()
                .map(Order::getAccessionNumber)
                .collect(Collectors.toList());

        verify(encounterService, times(1)).getEncounterByUuid(openElisAccession.getAccessionUuid());

        assertEquals("1234", orderAccessionNumbers.get(0));
        assertNotEquals("1234", orderAccessionNumbers.get(1));
        assertEquals("1234", orderAccessionNumbers.get(2));
    }

    @Test
    public void shouldUpdateEncounterWhenAccessionHasRemovedOrderFromPreviousEncounter() throws Exception {
        Encounter previousEncounter = getEncounterWithTests("test1", "test2", "test3");
        Encounter encounterFromAccession = getEncounterWithTests("test1", "test2", "test3");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").withStatus("Canceled").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();
        stubAccession(openElisAccession);

        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(previousEncounter);
        AccessionDiff accessionDiff = new AccessionDiff();
        accessionDiff.addRemovedTestDetails(test3);
        when(accessionMapper.addOrDiscontinueOrderDifferences(any(OpenElisAccession.class), any(AccessionDiff.class), any(Encounter.class))).thenReturn(encounterFromAccession);
        final Visit visit = new Visit();
        visit.setId(1);
        previousEncounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Arrays.asList(previousEncounter)));
        when(accessionMapper.findOrInitializeVisit(any(Patient.class), any(Date.class), any(String.class))).thenReturn(visit);
        when(encounterService.saveEncounter(previousEncounter)).thenReturn(previousEncounter);

        accessionEventWorker.process(event);

        verify(encounterService, times(2)).getEncounterByUuid(openElisAccession.getAccessionUuid());
        verify(accessionMapper, never()).mapToNewEncounter(any(OpenElisAccession.class), any(String.class));
        verify(accessionMapper).addOrDiscontinueOrderDifferences(any(OpenElisAccession.class), any(AccessionDiff.class), any(Encounter.class));
        verify(encounterService).saveEncounter(previousEncounter);
        verify(bahmniVisitAttributeSaveCommand).save(previousEncounter);
    }

    @Test
    public void shouldNotUpdateEncounterWhenAccessionHasSameOrdersAsPreviousEncounter() throws Exception {
        Encounter previousEncounter = getEncounterWithTests("test1", "test2");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());
        stubAccession(openElisAccession);
        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(previousEncounter);
        final Visit visit = new Visit();
        visit.setId(1);
        previousEncounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Arrays.asList(previousEncounter)));
        when(accessionMapper.findOrInitializeVisit(any(Patient.class), any(Date.class), any(String.class))).thenReturn(visit);

        accessionEventWorker.process(event);

        verify(encounterService, times(2)).getEncounterByUuid(openElisAccession.getAccessionUuid());
        verify(encounterService, never()).saveEncounter(previousEncounter);
    }

    @Test
    public void shouldIgnoreAccessionEventIfPatientIsNotPresentInOpenMRS() throws IOException {
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().build();
        when(accessionMapper.shouldIgnoreAccession(openElisAccession)).thenReturn(true);
        stubAccession(openElisAccession);

        accessionEventWorker.process(event);

        verify(encounterService, times(0)).saveEncounter(any(Encounter.class));
    }

    @Test
    public void shouldNotIgnoreAccessionEventIfPatientIsPresentInOpenMRS() throws IOException {
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().build();
        Encounter encounter = getEncounterWithTests("test1");
        Visit visit = new Visit();
        visit.setId(1);
        encounter.setVisit(visit);
        visit.setEncounters(new HashSet<>(Collections.singletonList(encounter)));
        stubAccession(openElisAccession);
        when(accessionMapper.shouldIgnoreAccession(openElisAccession)).thenReturn(false);
        when(encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid())).thenReturn(encounter);
        when(encounterService.saveEncounter(any(Encounter.class))).thenReturn(encounter);

        accessionEventWorker.process(event);

        verify(encounterService, times(1)).saveEncounter(any(Encounter.class));
    }

    private Encounter getEncounterWithTests(String... testUuids) {
        Encounter encounter = new Encounter();
        for (String testUuid : testUuids) {
            Order order = new Order();
            Concept concept = new Concept();
            concept.setId(123);
            concept.setUuid(testUuid);
            order.setConcept(concept);
            order.setAccessionNumber(null);
            encounter.addOrder(order);
            encounter.setEncounterType(new EncounterType());
            Patient patient = new Patient();
            patient.setUuid("patientUuid");
            encounter.setPatient(patient);
        }
        return encounter;
    }

    private void stubAccession(OpenElisAccession accession) throws IOException {
        when(httpClient.get(openElisUrl + event.getContent(), OpenElisAccession.class)).thenReturn(accession);
    }
}
