package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.junit.Test;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OrderAttributesMapperTest {

    @Test
    public void shouldMapRelatedObservationsWithOrders(){
        List<BahmniObservation> bahmniObservationList = new ArrayList<>();
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept("Concept_uuid", "dispensed", true, "Concept_dataType", "Concept_units", "Concept_conceptClass", null, null);

        BahmniObservation  dispensedObservation =new BahmniObservationBuilder().withUuid("obs-uuid").withConcept(concept).withOrderUuid("Order_uuid").withValue("true").build();

        bahmniObservationList.add(dispensedObservation);

        BahmniDrugOrder bahmniDrugOrder = new BahmniDrugOrder();
        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setUuid("Order_uuid");
        bahmniDrugOrder.setDrugOrder(drugOrder);
        List<BahmniDrugOrder> bahmniDrugOrderList = new ArrayList<>();
        bahmniDrugOrderList.add(bahmniDrugOrder);

        bahmniDrugOrderList = new OrderAttributesMapper().map(bahmniDrugOrderList, bahmniObservationList);

        assertEquals(1,bahmniDrugOrderList.get(0).getOrderAttributes().size());
        assertEquals("dispensed", bahmniDrugOrderList.get(0).getOrderAttributes().get(0).getName());
        assertEquals("obs-uuid", bahmniDrugOrderList.get(0).getOrderAttributes().get(0).getObsUuid());
    }
}