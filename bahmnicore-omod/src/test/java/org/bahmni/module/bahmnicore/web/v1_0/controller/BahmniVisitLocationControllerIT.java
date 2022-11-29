package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.bahmni.module.bahmnicommons.api.visitlocation.VisitLocationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class BahmniVisitLocationControllerIT extends BaseIntegrationTest {

    @Autowired
    private BahmniVisitLocationController bahmniLocationController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("locationData.xml");
    }

        @Test
        public void shouldGetImmediateParentLocationIdIfItIsTaggedToVisitLocation() throws Exception {
            String locationUuid = bahmniLocationController.getVisitLocationInfo("c36006e5-9fbb-4f20-866b-0ece245615a1").get("uuid");
            assertEquals("e36006e5-9fbb-4f20-866b-0ece24561525", locationUuid);
        }

        @Test
        public void shouldTraverseTillItGetsParentLocationIdWhichIsTaggedToVisitLocation() throws Exception {
           String locationUuid= bahmniLocationController.getVisitLocationInfo("e36023e5-9fwb-4f20-866b-0ece24561525").get("uuid");
            assertEquals("e36006e5-9fbb-4f20-866b-0ece24561525", locationUuid);
        }

        @Test(expected = VisitLocationNotFoundException.class)
        public void shouldThrowExceptionIfNoLocationTaggedUntilRoot() throws Exception {
            bahmniLocationController.getVisitLocationInfo("l36023e5-9fhb-4f20-866b-0ece24561525");
        }
}