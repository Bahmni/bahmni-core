package org.bahmni.module.bahmnicore.forms2.contract.form.data;

import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FormDetailsTest {

    private String formName = "FormName";
    private int formVersion = 2;
    private String encounterUuid = "encounter-uuid";

    private FormDetails formDetails;

    @Before
    public void setUp() {
        formDetails = new FormDetails();
        formDetails.setFormName(formName);
        formDetails.setFormVersion(formVersion);
        formDetails.setEncounterUuid(encounterUuid);
    }

    @Test
    public void shouldReturnTrueWhenTwoFormDetailsAreSameByReference() {

        assertEquals(formDetails, formDetails);
    }

    @Test
    public void shouldReturnFalseWhenOneOfTheFormDetailsIsNull() {

        assertNotEquals(formDetails, null);
    }

    @Test
    public void shouldReturnFalseWhenTypeOfTheObjectDoesNotEqualToFormDetails() {

        assertNotEquals(formDetails, "");
    }

    @Test
    public void shouldReturnFalseWhenFormNameDoesNotMatch() {
        FormDetails otherFormDetails = new FormDetails();
        otherFormDetails.setFormName("some form name");
        otherFormDetails.setFormVersion(formVersion);
        otherFormDetails.setEncounterUuid(encounterUuid);

        assertNotEquals(formDetails, otherFormDetails);
    }

    @Test
    public void shouldReturnTrueWhenFormNameFormVersionAndEncounterUuidMatches() {

        FormDetails otherFormDetails = new FormDetails();
        otherFormDetails.setFormName(formName);
        otherFormDetails.setFormVersion(formVersion);
        otherFormDetails.setEncounterUuid(encounterUuid);

        assertEquals(otherFormDetails, otherFormDetails);
    }
}