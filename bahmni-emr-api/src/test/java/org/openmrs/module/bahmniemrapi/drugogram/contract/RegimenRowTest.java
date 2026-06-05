package org.openmrs.module.bahmniemrapi.drugogram.contract;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RegimenRowTest {

    @Test
    public void shouldReturnEmptyStringWhenDrugValueIsAbsent() {
        RegimenRow regimenRow = new RegimenRow();
        assertEquals("", regimenRow.getDrugValue("Paracetamol"));
    }

    @Test
    public void shouldGetDrugValueForDrugConceptName() {
        RegimenRow regimenRow = new RegimenRow();
        regimenRow.addDrugs("Paracetamol", "300.0");
        assertEquals("300.0", regimenRow.getDrugValue("Paracetamol"));
    }
}