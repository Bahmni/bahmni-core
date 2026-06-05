package org.openmrs.module.bahmniemrapi.drugogram.contract;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;


public class TreatmentRegimenTest {

    @Test
    public void shouldAddRowToRegimenWhenRowForThatDateIsAbsent() throws ParseException {
        TreatmentRegimen treatmentRegimen = new TreatmentRegimen();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        treatmentRegimen.addRow(simpleDateFormat.parse("2016-01-01"));

        assertEquals(1,treatmentRegimen.getRows().size());
    }


    @Test
    public void shouldNotAddRowToRegimenWhenRowForThatDateIsPresent() throws ParseException {
        TreatmentRegimen treatmentRegimen = new TreatmentRegimen();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        treatmentRegimen.addRow(simpleDateFormat.parse("2016-01-01"));
        treatmentRegimen.addRow(simpleDateFormat.parse("2016-01-01"));
        treatmentRegimen.addRow(simpleDateFormat.parse("2016-01-02"));

        assertEquals(2,treatmentRegimen.getRows().size());
    }
}