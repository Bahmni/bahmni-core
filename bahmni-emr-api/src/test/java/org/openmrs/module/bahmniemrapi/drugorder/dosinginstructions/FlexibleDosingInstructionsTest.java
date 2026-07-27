package org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FlexibleDosingInstructionsTest {

    private FlexibleDosingInstructions flexibleDosingInstructions;

    @Before
    public void setUp() {
        flexibleDosingInstructions = new FlexibleDosingInstructions();
    }

    @Test
    public void getDosingInstructions_shouldReturnFlexibleDosingInstructionsInstanceWhenTypeMatches() {
        DrugOrder order = new DrugOrder();
        order.setDosingType(FlexibleDosingInstructions.class);
        assertTrue(flexibleDosingInstructions.getDosingInstructions(order) instanceof FlexibleDosingInstructions);
    }

    @Test
    public void setDosingInstructions_shouldSetDosingTypeToFlexibleDosingInstructions() {
        DrugOrder order = new DrugOrder();
        flexibleDosingInstructions.setDosingInstructions(order);
        assertEquals(FlexibleDosingInstructions.class, order.getDosingType());
    }
}
