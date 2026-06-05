package org.bahmni.jss.registration;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.bahmni.datamigration.request.patient.Name;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.StringTokenizer;

import static org.bahmni.datamigration.DataScrub.scrubData;

public class RegistrationFields {
    private static final String patternWhenYearSpecifiedAs4Digits = "dd/MM/yyyy";
    private static final String patternWhenYearSpecifiedAs2Digits = "dd/MM/yy";
    public static final LocalDate UnknownDateOfBirth = new LocalDate(1900, 1, 1);
    public static final String UnknownDateOfBirthAsString = UnknownDateOfBirth.toString("dd-MM-yyyy");

    public static String getDate(String s) {
        StringTokenizer stringTokenizer = new StringTokenizer(s.trim(), " ");
        if (!stringTokenizer.hasMoreTokens()) return null;
        String datePart = stringTokenizer.nextToken();
        String pattern = datePart.length() == 8 ? patternWhenYearSpecifiedAs2Digits : patternWhenYearSpecifiedAs4Digits;
        LocalDate localDate = LocalDateTime.parse(datePart, DateTimeFormat.forPattern(pattern)).toLocalDate();
        if(localDate.getYear() <= 1900 || localDate.isAfter(LocalDate.now()))
            localDate = UnknownDateOfBirth;
        return localDate.toString("dd-MM-yyyy");
    }

    public static String sentenceCase(String s) {
        return WordUtils.capitalizeFully(s);
    }

    public static RegistrationNumber parseRegistrationNumber(String registrationNumber) {
        StringTokenizer stringTokenizer = new StringTokenizer(registrationNumber, "/");
        String id = stringTokenizer.nextToken();
        String centerCode = stringTokenizer.nextToken();
        return new RegistrationNumber(scrubData(centerCode), scrubData(id));
    }

    public static Name name(String firstName, String lastName) {
        String[] splitFirstNames = StringUtils.split(firstName, " ");
        String givenName;
        String familyName = null;

        Name name = new Name();
        if (StringUtils.isEmpty(lastName) && splitFirstNames.length > 1) {
            Object[] splitFirstNamesExceptLastWord = ArrayUtils.remove(splitFirstNames, splitFirstNames.length - 1);
            givenName = StringUtils.join(splitFirstNamesExceptLastWord, " ");
            familyName = splitFirstNames[splitFirstNames.length - 1];
        } else {
            givenName = firstName;
            familyName = lastName;
        }
        name.setGivenName((givenName == null || StringUtils.isEmpty(givenName)) ? "." : givenName);
        name.setFamilyName((familyName == null || StringUtils.isEmpty(familyName)) ? "." : familyName);
        return name;
    }

    public static int getAge(String fieldValue) {
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(fieldValue);
        } catch (NumberFormatException e) {
            return 0;
        }
        return (int) Math.round(doubleValue);
    }


}