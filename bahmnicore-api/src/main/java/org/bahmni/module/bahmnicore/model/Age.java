package org.bahmni.module.bahmnicore.model;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Date;
import java.util.LinkedHashMap;

public class Age {
    private final int years;
    private final int months;
    private final int days;

    public Age(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    public Date getDateOfBirth(Date forDate) {
        Period ageAsPeriod = new Period(years, months, 0, days, 0, 0, 0, 0, PeriodType.yearMonthDay());
        LocalDate dateOfBirth = new LocalDate(forDate).minus(ageAsPeriod);
        return dateOfBirth.toDate();
    }

    public Date getDateOfBirth() {
        return getDateOfBirth(new Date());
    }

    public static Age fromDateOfBirth(Date birthDate, Date today) {
        Period ageAsPeriod = new Period(new LocalDate(birthDate), new LocalDate(today), PeriodType.yearMonthDay());
        return new Age(ageAsPeriod.getYears(), ageAsPeriod.getMonths(), ageAsPeriod.getDays());
    }

    public static Age fromBirthDate(Date birthDate) {
        return fromDateOfBirth(birthDate, new Date());
    }

    public static Age fromHash(LinkedHashMap simpleObject) {
        SimpleObjectExtractor simpleObjectExtractor = new SimpleObjectExtractor(simpleObject);
        int years = simpleObjectExtractor.getValueOrDefault("years", int.class);
        int months = simpleObjectExtractor.getValueOrDefault("months", int.class);
        int days = simpleObjectExtractor.getValueOrDefault("days", int.class);
        return new Age(years, months, days);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Age age = (Age) o;

        return days == age.days && months == age.months && years == age.years;

    }

    @Override
    public int hashCode() {
        int result = years;
        result = 31 * result + months;
        result = 31 * result + days;
        return result;
    }

    @Override
    public String toString() {
        return "Age{" +
                "years=" + years +
                ", months=" + months +
                ", days=" + days +
                '}';
    }
}
