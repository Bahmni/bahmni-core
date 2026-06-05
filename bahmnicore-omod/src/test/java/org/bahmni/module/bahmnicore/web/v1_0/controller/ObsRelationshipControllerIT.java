package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class ObsRelationshipControllerIT extends BaseIntegrationTest {

    @Autowired
    private ObsRelationshipController obsRelationshipController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("obsRelationshipDataset.xml");
    }

    @Test @Ignore
    public void shouldReturnAllSourceObsByGivenTargetObsUuid(){
        List<BahmniObservation> bahmniObservations = obsRelationshipController.find("39fb7f47-e80a-4056-9285-bd798be13c63");

        assertEquals(2, bahmniObservations.size());
        assertEquals("be48cdcb-6a76-47e3-9f2e-2635032f3a9a", bahmniObservations.get(0).getUuid());
        assertEquals("39fb7f47-e80a-4056-9285-bd798be13c63", bahmniObservations.get(0).getTargetObsRelation().getTargetObs().getUuid());
        assertEquals("qualified-by", bahmniObservations.get(0).getTargetObsRelation().getRelationshipType());

        assertEquals("f6ec1267-8eac-415f-a3f0-e47be2c8bb67", bahmniObservations.get(1).getUuid());
        assertEquals("39fb7f47-e80a-4056-9285-bd798be13c63", bahmniObservations.get(1).getTargetObsRelation().getTargetObs().getUuid());
        assertEquals("qualified-by", bahmniObservations.get(1).getTargetObsRelation().getRelationshipType());
    }
}