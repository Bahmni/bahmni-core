package org.bahmni.module.bahmnicore.model;

import junit.framework.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BahmniPatientTest {

    @Test
    public void shouldCreateAPersonFromASimpleObject() throws ParseException {
        String birthdate = "01-01-2012";
        String registrationDateStr = "25-04-1988";
        String centerName = "Ganiyari";
        double expectedBalance = 123;
        SimpleObject age = new SimpleObject().add("years", 21).add("months", 10).add("days", 30);
        SimpleObject personObject = new SimpleObject().add("birthdate", birthdate).add("age", age).add("gender", "M").add(
                "attributes", Arrays.asList(new SimpleObject().add("attributeType", "caste").add("value", "someCaste"))).add(
                "addresses", Arrays.asList(new SimpleObject().add("address1", "7143 Koramangala"))).add("centerID",  centerName)
                .add("names", Arrays.asList(new SimpleObject().add("givenName", "first").add("familyName", "Last")))
                .add("identifier", "someIdentifier")
                .add("balance", "123")
                .add("dateOfRegistration", registrationDateStr);


        BahmniPatient person = new BahmniPatient(personObject);

        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(birthdate);
        Date registrationDate = new SimpleDateFormat("dd-MM-yyyy").parse(registrationDateStr);
        Assert.assertEquals(date, person.getBirthdate());
        Assert.assertEquals("M", person.getGender());
        Assert.assertEquals("someIdentifier", person.getIdentifier());
        Assert.assertEquals(1, person.getAttributes().size());
        Assert.assertEquals(1, person.getAddresses().size());
        Assert.assertEquals(1, person.getNames().size());
        Assert.assertEquals(centerName, person.getCenterName());
        Assert.assertEquals(expectedBalance, person.getBalance());
        Assert.assertEquals(registrationDate, person.getPersonDateCreated());
    }
}
