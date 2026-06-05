package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.visit.VisitSummary;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BahmniVisitControllerIT extends BaseIntegrationTest {

    @Autowired
    private BahmniVisitController bahmniVisitController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitInfo.xml");
    }

    @Test
    public void shouldGetVisitSummary() {
        VisitSummary visitSummary = bahmniVisitController.getVisitInfo("1e5d5d48-6b78-11e0-933c-18a905e044cd");

        assertNotNull(visitSummary);
        assertEquals("1e5d5d48-6b78-11e0-933c-18a905e044cd", visitSummary.getUuid());
        assertEquals("2005-01-01 00:00:00.0", visitSummary.getStartDateTime().toString());
        assertEquals("2005-01-05 00:00:00.0", visitSummary.getStopDateTime().toString());
    }

    @Test
    public void shouldNotGetVisitSummaryOfVoidedVisit() {
        VisitSummary visitSummary = bahmniVisitController.getVisitInfo("e1428fea-6b78-11e0-93c3-18a905e044dc");

        assertNull(visitSummary);
    }

    @Test
    public void shouldGetAdmissionDetailsAndDischargeDetailsIfVisitStatusIsIPD() throws Exception {
        VisitSummary visitSummary = bahmniVisitController.getVisitInfo("1e5d5d48-6b78-11e0-933c-18a905e044cd");

        assertNotNull(visitSummary);
        assertNotNull(visitSummary.getAdmissionDetails());
        assertEquals("bb0af6767-707a-4629-9850-f15206e63ab0", visitSummary.getAdmissionDetails().getUuid());
        assertEquals("2005-01-01 00:00:00.0", visitSummary.getAdmissionDetails().getDate().toString());

        assertNotNull(visitSummary.getDischargeDetails());
        assertEquals("bb0af6767-707a-4629-9850-f15206e63a0b", visitSummary.getDischargeDetails().getUuid());
        assertEquals("2005-01-04 00:00:00.0", visitSummary.getDischargeDetails().getDate().toString());
    }

    @Test
    public void shouldNotGetAdmissionDetailsOrDischargeDetailsIfTheVisitStatusIsOPD() throws Exception {
        VisitSummary visitSummary = bahmniVisitController.getVisitInfo("1e5d5d48-6b78-11e0-988c-18a905e044cd");

        assertNotNull(visitSummary);
        assertNull(visitSummary.getAdmissionDetails());
        assertNull(visitSummary.getDischargeDetails());
    }

    @Test
    public void shouldNotGetAdmissionDetailsOrDischargeDetailsIfTheVisitStatusIsEmergency() throws Exception {
        VisitSummary visitSummary = bahmniVisitController.getVisitInfo("1e5d5d48-6b78-11e0-988c-18a905e033cd");

        assertNotNull(visitSummary);
        assertNull(visitSummary.getAdmissionDetails());
        assertNull(visitSummary.getDischargeDetails());
    }
}