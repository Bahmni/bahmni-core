package org.bahmni.module.elisatomfeedclient.api.worker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class EncounterHelperTest {
    private EncounterType encounterType;
    @Mock
    EncounterService encounterService;
    @Mock
    VisitService visitService;
    private EncounterHelper encounterHelper;
    private Provider provider;
    private Patient patient;

    @Before
    public void setUp() throws ParseException {
        initMocks(this);
        encounterType = new EncounterType("TestEncounter", "Encounter for test");
        when(encounterService.getEncounters(any(Patient.class), any(Location.class), any(Date.class), any(Date.class),
                anyListOf(Form.class), anyListOf(EncounterType.class), anyListOf(Provider.class),
                anyListOf(VisitType.class), anyListOf(Visit.class), anyBoolean())).thenReturn(getEncounters());
        encounterHelper = new EncounterHelper(encounterService);
        provider = new Provider(333);
        patient = new Patient(444);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(new User());
    }

    @Test
    public void shouldCreateEncounterWithGivenParameters() throws Exception {
        Visit visit = new Visit();
        visit.setEncounters(new HashSet<>(getEncounters()));
        Encounter newEncounter = encounterHelper.createNewEncounter(visit, encounterType, new Date(), patient, provider, null);
        assertEquals(encounterType, newEncounter.getEncounterType());
        assertEquals(provider.getIdentifier(), newEncounter.getEncounterProviders().iterator().next().getProvider().getIdentifier());
        assertEquals(patient.getId(), newEncounter.getPatient().getId());
    }

    public List<Encounter> getEncounterWithObs() {
        List<Encounter> encounters = new ArrayList<>();
        Encounter encounter = new Encounter(1);
        encounter.setObs(createObs("10", "20", "30"));
        encounters.add(encounter);
        encounters.add(new Encounter(2));
        encounters.add(new Encounter(3));
        return encounters;
    }

    private Set<Obs> createObs(String... obsUuids) {
        HashSet<Obs> observations = new HashSet<>();
        Order order = createOrders("30").iterator().next();
        Concept concept = new Concept();
        concept.setUuid("c1");
        for (String obsUuid : obsUuids) {
            Obs obs = new Obs();
            obs.setUuid(obsUuid);
            obs.setOrder(order);
            obs.setConcept(concept);
            observations.add(obs);
        }
        return observations;
    }

    public List<Encounter> getEncounters() {
        ArrayList<Encounter> encounters = new ArrayList<Encounter>();
        encounters.add(new Encounter(1));
        encounters.add(new Encounter(2));
        encounters.add(new Encounter(3));
        encounters.get(2).setOrders(createOrders("10", "20", "30"));
        encounters.get(0).setOrders(createOrders("40", "50", "60"));
        return encounters;
    }

    private Set<Order> createOrders(String... orderUuids) {
        HashSet<Order> orders = new HashSet<Order>();
        for (String orderUuid : orderUuids) {
            Order order = new Order();
            order.setUuid(orderUuid);
            orders.add(order);
        }
        return orders;
    }
}
