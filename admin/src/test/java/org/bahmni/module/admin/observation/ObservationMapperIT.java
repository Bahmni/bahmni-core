package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ObservationMapperIT extends BaseIntegrationTest {

    @Autowired
    private ObservationMapper observationMapper;

    @Before
    public void setUp() throws Exception {
        executeDataSet("conceptSetup.xml");
        executeDataSet("form2DataSetup.xml");
    }

    @Test
    public void shouldCreateForm1AndForm2Observations() throws ParseException {
        List<String> observation = Arrays.asList("Temperature", "100", "Vitals.1/2-0");
        List<String> anotherObservation = Arrays.asList("Pulse", "150", null);
        List<List<String>> expectedObservations = Arrays.asList(observation, anotherObservation);

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();

        anEncounter.obsRows.add(new KeyValue("Pulse", "150"));
        anEncounter.obsRows.add(new KeyValue("form2.Vitals.Section.Temperature", "100"));
        anEncounter.encounterDateTime = "2019-09-19";

        List<List<String>> actualObservations = convertToList(observationMapper.getObservations(anEncounter));

        assertEquals(2, actualObservations.size());
        assertTrue(actualObservations.containsAll(expectedObservations));
    }

    private List<List<String>> convertToList(List<EncounterTransaction.Observation> observations) {
        List<List<String>> actualObservations = new ArrayList<>();
        observations
                .forEach(obs ->
                        actualObservations.add(Arrays.asList(obs.getConcept().getName(), (String) obs.getValue(), obs.getFormFieldPath()))
                );
        return actualObservations;
    }

    @Test
    public void shouldCreateForm2Observations() throws ParseException {
        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();

        anEncounter.obsRows.add(new KeyValue("form2.Form2EncountersTest.HIV Infection History.WHO Stage Conditions", "Asymptomatic|Herpes Zoster"));
        anEncounter.encounterDateTime = "2019-09-19";

        final List<EncounterTransaction.Observation> observations = observationMapper.getObservations(anEncounter, true);

        assertEquals(2, observations.size());

        final EncounterTransaction.Observation multiSelectObs1 = observations.get(0);
        assertEquals("WHO Stage Conditions", multiSelectObs1.getConcept().getName());
        assertEquals("Asymptomatic", multiSelectObs1.getValue());
        assertEquals("Form2EncountersTest.2/2-0", multiSelectObs1.getFormFieldPath());

        final EncounterTransaction.Observation multiSelectObs2 = observations.get(1);
        assertEquals("WHO Stage Conditions", multiSelectObs2.getConcept().getName());
        assertEquals("Herpes Zoster", multiSelectObs2.getValue());
        assertEquals("Form2EncountersTest.2/2-0", multiSelectObs2.getFormFieldPath());

    }
}