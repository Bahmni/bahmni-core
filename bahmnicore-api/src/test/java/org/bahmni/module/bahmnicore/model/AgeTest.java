package org.bahmni.module.bahmnicore.model;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class AgeTest {

    @Test
    public void shouldConvertHashToAgeWhenOneOfThePropertiesDoNotExist() {
        assertEquals(new Age(2010, 0, 0), Age.fromHash(new SimpleObject().add("years", 2010)));
        assertEquals(new Age(0, 12, 0), Age.fromHash(new SimpleObject().add("months", 12)));
        assertEquals(new Age(0, 0, 31), Age.fromHash(new SimpleObject().add("days", 31)));
        assertEquals(new Age(0, 0, 0), Age.fromHash(new SimpleObject()));
    }

    @Test
    public void shouldCalculateAgeFromDateOfBirth() {
        Date birthDate = new LocalDate(1990, 6, 15).toDate();
        Date today = new LocalDate(2013, 12, 5).toDate();

        Age age = Age.fromDateOfBirth(birthDate, today);

        assertEquals(new Age(23, 5, 20), age);
    }

    @Test
    public void shouldCalculateDateOfBirthFromAge() {
        Age age = new Age(20, 5, 21);
        Date today = new LocalDate(2013, 6, 20).toDate();

        Date dateOfBirth = age.getDateOfBirth(today);

        assertEquals(new LocalDate(1992, 12, 30).toDate(), dateOfBirth);
    }
}
