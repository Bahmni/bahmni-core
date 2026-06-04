package org.bahmni.module.admin.retrospectiveEncounter.domain;

import org.bahmni.module.admin.builder.BahmniObservationBuilder;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.EncounterBuilder;
import org.bahmni.test.builder.ObsBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DuplicateObservationsMatcher.class, LocaleUtility.class})
public class DuplicateObservationsMatcherTest {

    public static final Date VISIT_START_DATETIME = new Date();
    public static final Date ENCOUNTER_DATE = new Date();
    private DuplicateObservationsMatcher duplicateObservationsMatcher;

    @Mock
    private BahmniVisit bahmniVisit;

    @Mock
    private Visit visit;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void shouldNotGetAnObservationIfItExistsInTheSameEncounter() throws Exception {
        Concept heightConcept = new ConceptBuilder().withName("HEIGHT").build();
        Obs height = new ObsBuilder().withConcept(heightConcept).withValue(150.9).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(height);

        Encounter encounter = new EncounterBuilder().withDatetime(ENCOUNTER_DATE).build();
        encounter.setObs(allObs);

        Visit existingVisit = new VisitBuilder().withEncounter(encounter).withStartDatetime(VISIT_START_DATETIME).build();

        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservationBuilder().withConcept("HEIGHT").withValue("150.9").withEncounterDate(ENCOUNTER_DATE).build());

        List<Obs> obsList = new ArrayList<>();
        obsList.addAll(allObs);

        when(bahmniVisit.obsFor("OPD")).thenReturn(obsList);
        whenNew(BahmniVisit.class).withArguments(existingVisit).thenReturn(bahmniVisit);
        duplicateObservationsMatcher = new DuplicateObservationsMatcher(existingVisit, "OPD");
        Collection<BahmniObservation> newlyAddedBahmniObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniObservations, ENCOUNTER_DATE);
        assertEquals(0, newlyAddedBahmniObservations.size());
    }

    @Test
    public void shouldGetObservationIfItDoesNotExistInSameEncounter() throws Exception {
        Concept weightConcept = new ConceptBuilder().withName("WEIGHT").withDataTypeNumeric().build();
        Obs weight = new ObsBuilder().withConcept(weightConcept).withValue(200.9).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(weight);

        Encounter encounter = new EncounterBuilder().withDatetime(ENCOUNTER_DATE).build();
        encounter.setObs(allObs);

        Visit existingVisit = new VisitBuilder().withEncounter(encounter).withStartDatetime(VISIT_START_DATETIME).build();

        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservationBuilder().withConcept("HEIGHT").withValue("150.9").withEncounterDate(ENCOUNTER_DATE).build());

        List<Obs> obsList = new ArrayList<>();
        obsList.addAll(allObs);

        when(bahmniVisit.obsFor("OPD")).thenReturn(obsList);
        whenNew(BahmniVisit.class).withArguments(existingVisit).thenReturn(bahmniVisit);

        duplicateObservationsMatcher = new DuplicateObservationsMatcher(existingVisit, "OPD");
        Collection<BahmniObservation> newlyAddedBahmniObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniObservations, ENCOUNTER_DATE);

        assertEquals(1, newlyAddedBahmniObservations.size());
    }

    @Test
    public void shouldGetObservationIfSameObservationExistsInDifferentEncounter() throws Exception {
        Concept weightConcept = new ConceptBuilder().withName("WEIGHT").build();
        Obs weight = new ObsBuilder().withConcept(weightConcept).withValue(200.9).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(weight);

        Encounter encounter = new EncounterBuilder().withDatetime(ENCOUNTER_DATE).build();
        encounter.setObs(allObs);

        Visit existingVisit = new VisitBuilder().withEncounter(encounter).withStartDatetime(VISIT_START_DATETIME).build();

        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        Date newEncounterDate = new Date();
        bahmniObservations.add(new BahmniObservationBuilder().withConcept("WEIGHT").withValue("200.9").withEncounterDate(newEncounterDate).build());

        List<Obs> obsList = new ArrayList<>();
        obsList.addAll(allObs);

        when(bahmniVisit.obsFor("OPD")).thenReturn(obsList);
        whenNew(BahmniVisit.class).withArguments(existingVisit).thenReturn(bahmniVisit);

        duplicateObservationsMatcher = new DuplicateObservationsMatcher(existingVisit, "OPD");
        Collection<BahmniObservation> newlyAddedBahmniObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniObservations, newEncounterDate);

        assertEquals(1, newlyAddedBahmniObservations.size());
    }

    @Test
    public void shouldGetObservationIfDifferentObservationExistsInDifferentEncounter() throws Exception {
        Concept weightConcept = new ConceptBuilder().withName("WEIGHT").build();
        Obs weight = new ObsBuilder().withConcept(weightConcept).withValue(200.9).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(weight);

        Encounter encounter = new EncounterBuilder().withDatetime(ENCOUNTER_DATE).build();
        encounter.setObs(allObs);

        Visit existingVisit = new VisitBuilder().withEncounter(encounter).withStartDatetime(VISIT_START_DATETIME).build();

        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        Date newEncounterDate = new Date();
        bahmniObservations.add(new BahmniObservationBuilder().withConcept("HEIGHT").withValue("200.9").withEncounterDate(newEncounterDate).build());

        List<Obs> obsList = new ArrayList<>();
        obsList.addAll(allObs);

        when(bahmniVisit.obsFor("OPD")).thenReturn(obsList);
        whenNew(BahmniVisit.class).withArguments(existingVisit).thenReturn(bahmniVisit);

        duplicateObservationsMatcher = new DuplicateObservationsMatcher(existingVisit, "OPD");
        Collection<BahmniObservation> newlyAddedBahmniObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniObservations, newEncounterDate);

        assertEquals(1, newlyAddedBahmniObservations.size());
    }

    @Test
    public void shouldNotGetObservationIfDifferentObservationWithSameRootExistsInSameEncounter() throws Exception {
        Concept heightConcept = new ConceptBuilder().withName("HEIGHT").withDataTypeNumeric().build();
        Obs heightObs = new ObsBuilder().withValue(150.0).withConcept(heightConcept).build();

        Concept vitalConcept = new ConceptBuilder().withName("Vitals").build();
        Obs vitalObs = new ObsBuilder().withConcept(vitalConcept).withGroupMembers(heightObs).build();

        Set<Obs> allObs = new HashSet<>();
        allObs.add(vitalObs);

        Encounter encounter = new EncounterBuilder().withDatetime(ENCOUNTER_DATE).build();
        encounter.setObs(allObs);

        Visit existingVisit = new VisitBuilder().withEncounter(encounter).withStartDatetime(VISIT_START_DATETIME).build();

        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        BahmniObservation weightObs = new BahmniObservationBuilder().withConcept("WEIGHT").withValue("190").withEncounterDate(ENCOUNTER_DATE).build();
        bahmniObservations.add(new BahmniObservationBuilder().withConcept("Vitals").withSetMember(weightObs).withEncounterDate(ENCOUNTER_DATE).build());

        List<Obs> obsList = new ArrayList<>();
        obsList.addAll(allObs);

        when(bahmniVisit.obsFor("OPD")).thenReturn(obsList);
        whenNew(BahmniVisit.class).withArguments(existingVisit).thenReturn(bahmniVisit);

        duplicateObservationsMatcher = new DuplicateObservationsMatcher(existingVisit, "OPD");
        Collection<BahmniObservation> newlyAddedBahmniObservations = duplicateObservationsMatcher.getNewlyAddedBahmniObservations(bahmniObservations, ENCOUNTER_DATE);

        assertEquals(0, newlyAddedBahmniObservations.size());
    }

    @Test
    public void shouldIgnoreDuplicateObservationMatchingForFreeTextDiagnosisRequest() throws Exception {

        whenNew(BahmniVisit.class).withArguments(visit).thenReturn(bahmniVisit);
        when(bahmniVisit.obsFor("OPD")).thenReturn(new ArrayList<Obs>());
        List<BahmniDiagnosisRequest> requests = new ArrayList<>();
        BahmniDiagnosisRequest request = new BahmniDiagnosisRequest();
        request.setCodedAnswer(null);
        request.setFreeTextAnswer("Some Non-coded concept");
        requests.add(request);

        duplicateObservationsMatcher = new DuplicateObservationsMatcher(visit, "OPD");
        requests = duplicateObservationsMatcher.getUniqueDiagnoses(requests);

        assertEquals(1,requests.size());

    }
}
