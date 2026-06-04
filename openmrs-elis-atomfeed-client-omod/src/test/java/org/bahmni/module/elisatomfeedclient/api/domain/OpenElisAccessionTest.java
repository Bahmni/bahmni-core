package org.bahmni.module.elisatomfeedclient.api.domain;

import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisAccessionBuilder;
import org.bahmni.module.elisatomfeedclient.api.builder.OpenElisTestDetailBuilder;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class OpenElisAccessionTest {
    private HashSet<Encounter> accessionNotesEncounters;
    private Concept accessionNotesConcept;
    private OpenElisAccession openElisAccessionWithNotes;
    private Provider defaultLabManagerProvider;

    @Test
    public void shouldGetDiffWhenAccessionHasNewOrder() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).build();

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(1, diff.getAddedTestDetails().size());
        assertEquals(test2, diff.getAddedTestDetails().toArray()[0]);
    }

    @Test
    public void shouldGetDiffWhenAccessionHasRemovedOrderFromPreviousEncounter() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1", "test2", "test3");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").withStatus("Canceled").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        Set<OpenElisTestDetail> removedTestDetails = diff.getRemovedTestDetails();

        assertEquals(1, removedTestDetails.size());
        assertEquals(test3, removedTestDetails.toArray()[0]);

    }

    @Test
    public void shouldGetDiffWhenAccessionHasAddedTestToPreviousEncounterAndRemovedTestWithinElis() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").withStatus("Canceled").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(1, diff.getAddedTestDetails().size());
        assertEquals(test2, diff.getAddedTestDetails().toArray()[0]);
        assertEquals(0, diff.getRemovedTestDetails().size());
    }

    @Test
    public void shouldNotDiffIfThereAreNoAddedOrRemovedTests() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1", "test2");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(0, diff.getAddedTestDetails().size());
        assertEquals(0, diff.getRemovedTestDetails().size());

    }

    @Test
    public void shouldNotDiffIfThereAreTestsRemovedOnBothSides() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1", "test2");
        getOrderByName(previousEncounter, "test1").setVoided(true);
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").withStatus("Canceled").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(0, diff.getAddedTestDetails().size());
        assertEquals(0, diff.getRemovedTestDetails().size());
    }

    @Test
    @Ignore
    //This testcase has other consequences. For example
    // Doctor orders a test, it gets synced to elis. The sample is collected and now the doctor cancels it from MRS (voided=1).
    // The cancel is dropped as the sample is already collected.  Now, when it syncs back, we do not want the original order to be un-voided. Refer #2341
    public void shouldGetDiffIfCancelledTestIsReordered() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1", "test2");
        getOrderByName(previousEncounter, "test1").setVoided(true);
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").withStatus("Canceled").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail test1ReOrdered = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test1ReOrdered))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(1, diff.getAddedTestDetails().size());
        assertEquals(test1ReOrdered, diff.getAddedTestDetails().toArray()[0]);
    }

    @Test
    public void shouldGetDiffIfNewPanelAreAdded() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").withPanelUuid("panel1").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").withPanelUuid("panel1").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(2, diff.getAddedTestDetails().size());
        Assert.assertThat(diff.getAddedTestDetails(), hasItem(test2));
        Assert.assertThat(diff.getAddedTestDetails(), hasItem(test3));
    }

    @Test
    public void shouldGetDiffIfPanelAreRemoved() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("test1", "panel1");
        OpenElisTestDetail test1 = new OpenElisTestDetailBuilder().withTestUuid("test1").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").withStatus("Canceled").withPanelUuid("panel1").build();
        OpenElisTestDetail test3 = new OpenElisTestDetailBuilder().withTestUuid("test3").withStatus("Canceled").withPanelUuid("panel1").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(test1, test2, test3))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(2, diff.getRemovedTestDetails().size());
        Assert.assertThat(diff.getRemovedTestDetails(), hasItem(test2));
        Assert.assertThat(diff.getRemovedTestDetails(), hasItem(test3));
    }

    @Test
    public void shouldNotGetDiffIfThereArePanelsRemovedOnBothSides() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("panel1", "test2");
        getOrderByName(previousEncounter, "panel1").setVoided(true);
        OpenElisTestDetail panel1 = new OpenElisTestDetailBuilder().withTestUuid("test1").withPanelUuid("panel1").withStatus("Canceled").build();
        OpenElisTestDetail test2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(panel1, test2))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(0, diff.getAddedTestDetails().size());
        assertEquals(0, diff.getRemovedTestDetails().size());
    }

    @Test
    @Ignore
    public void shouldGetDiffIfCancelledPanelIsReordered() throws Exception {
        Encounter previousEncounter = getEncounterWithOrders("panel1", "test2");
        getOrderByName(previousEncounter, "panel1").setVoided(true);
        OpenElisTestDetail panel1 = new OpenElisTestDetailBuilder().withTestUuid("test1").withPanelUuid("panel1").withStatus("Canceled").build();
        OpenElisTestDetail panel2 = new OpenElisTestDetailBuilder().withTestUuid("test2").build();
        OpenElisTestDetail panel1ReOrdered = new OpenElisTestDetailBuilder().withTestUuid("test1").withPanelUuid("panel1").build();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().withTestDetails(new HashSet<>(Arrays.asList(panel1, panel2, panel1ReOrdered))).build();
        previousEncounter.setUuid(openElisAccession.getAccessionUuid());

        AccessionDiff diff = openElisAccession.getDiff(previousEncounter);

        assertEquals(1, diff.getAddedTestDetails().size());
        assertEquals(panel1ReOrdered, diff.getAddedTestDetails().toArray()[0]);
    }

    public void accessionNotesTestSetup() {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(new User());

        accessionNotesEncounters = new HashSet<>();
        Encounter encounter1 = createEncounterWithProviderAndObservations("e1","p1","c1","note1");
        Encounter encounter2 =createEncounterWithProviderAndObservations("e2","p2","c1","note2","note3");
        accessionNotesEncounters.add(encounter1);
        accessionNotesEncounters.add(encounter2);
        defaultLabManagerProvider = new Provider();
        defaultLabManagerProvider.setUuid("default");
        accessionNotesConcept = new Concept();
        accessionNotesConcept.setUuid("c1");
    }

    private Encounter createEncounterWithProviderAndObservations(String encUuid,String providerUuid,String conceptUuid,String... observations) {
        Encounter encounter1 = new Encounter();
        encounter1.setUuid(encUuid);
        Provider provider1 = new Provider();
        provider1.setUuid(providerUuid);
        encounter1.addProvider(new EncounterRole(1), provider1);
        Concept concept = new Concept();
        concept.setUuid(conceptUuid);

        for(String observation : observations){
            Obs obs = new Obs();
            obs.setConcept(concept);
            obs.setValueText(observation);
            encounter1.addObs(obs);
        }
        return encounter1;
    }


    @Test
    public void shouldUpdateTheAccessionNotesToBeAdded() {
        accessionNotesTestSetup();
        openElisAccessionWithNotes = new OpenElisAccessionBuilder().withAccessionNotes(
                new OpenElisAccessionNote("note1","p1", "2014-01-30T11:50:18+0530"),
                new OpenElisAccessionNote("note2","p2", "2014-01-30T11:50:18+0530"),
                new OpenElisAccessionNote("note3","p2", "2014-01-30T11:50:18+0530"),
                new OpenElisAccessionNote("note4","p1", "2014-01-30T11:50:18+0530")).build();

        AccessionDiff diff = openElisAccessionWithNotes.getAccessionNoteDiff(accessionNotesEncounters, accessionNotesConcept,defaultLabManagerProvider);
        assertNotNull(diff);
        assertEquals(1, diff.getAccessionNotesToBeAdded().size());
        assertEquals("note4", diff.getAccessionNotesToBeAdded().get(0).getNote());
        assertEquals("p1", diff.getAccessionNotesToBeAdded().get(0).getProviderUuid());
    }

   @Test
    public void shouldntReturnDiffWhenNotesAlreadyExist() {
        accessionNotesTestSetup();
       openElisAccessionWithNotes = new OpenElisAccessionBuilder().withAccessionNotes(
               new OpenElisAccessionNote("note1","p1", "2014-01-30T11:50:18+0530"),
               new OpenElisAccessionNote("note2","p2", "2014-01-30T11:50:18+0530"),
               new OpenElisAccessionNote("note3","p2", "2014-01-30T11:50:18+0530")).build();
        AccessionDiff diff = openElisAccessionWithNotes.getAccessionNoteDiff(accessionNotesEncounters, accessionNotesConcept,defaultLabManagerProvider);
        assertNotNull(diff);
        assertEquals(0, diff.getAccessionNotesToBeAdded().size());
    }

    @Test
    public void shouldntReturnDiffWhenNotesAreAddedAndNoNotesExist() {
        accessionNotesTestSetup();
        OpenElisAccession openElisAccession = new OpenElisAccessionBuilder().build();
        AccessionDiff diff = openElisAccession.getAccessionNoteDiff(accessionNotesEncounters, accessionNotesConcept, defaultLabManagerProvider);
        assertNotNull(diff);
        assertEquals(0, diff.getAccessionNotesToBeAdded().size());
    }

    private Order getOrderByName(Encounter encounter, String testUuid) {
        for (Order order : encounter.getOrders()) {
            if (order.getConcept().getUuid().equals(testUuid))
                return order;
        }
        return null;
    }

    private Encounter getEncounterWithOrders(String... testUuids) {
        Encounter encounter = new Encounter();
        for (String testUuid : testUuids) {
            Order order = new Order();
            Concept concept = new Concept();
            concept.setUuid(testUuid);
            order.setConcept(concept);
            encounter.addOrder(order);
        }
        return encounter;
    }
}
