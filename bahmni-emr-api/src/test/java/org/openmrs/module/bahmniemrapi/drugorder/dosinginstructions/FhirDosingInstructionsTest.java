package org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.Assert.*;

public class FhirDosingInstructionsTest {

    private FhirDosingInstructions fhirDosingInstructions;

    @Before
    public void setUp() {
        fhirDosingInstructions = new FhirDosingInstructions();
    }

    @Test
    public void setDosingInstructions_shouldSetDosingTypeToFhirDosingInstructions() {
        DrugOrder order = new DrugOrder();
        fhirDosingInstructions.setDosingInstructions(order);
        assertEquals(FhirDosingInstructions.class, order.getDosingType());
    }

    @Test
    public void getDosingInstructions_shouldReturnNewInstanceWhenTypeMatches() {
        DrugOrder order = new DrugOrder();
        order.setDosingType(FhirDosingInstructions.class);
        assertNotNull(fhirDosingInstructions.getDosingInstructions(order));
    }

    @Test(expected = org.openmrs.api.APIException.class)
    public void getDosingInstructions_shouldThrowWhenTypeDoesNotMatch() {
        DrugOrder order = new DrugOrder();
        order.setDosingType(FlexibleDosingInstructions.class);
        fhirDosingInstructions.getDosingInstructions(order);
    }

    @Test
    public void getDosingInstructionsAsString_shouldReturnNull() {
        assertNull(fhirDosingInstructions.getDosingInstructionsAsString(null));
    }

    @Test
    public void validate_shouldNotAddAnyErrors() {
        DrugOrder order = new DrugOrder();
        Errors errors = new BeanPropertyBindingResult(order, "drugOrder");
        fhirDosingInstructions.validate(order, errors);
        assertFalse(errors.hasErrors());
    }
}
