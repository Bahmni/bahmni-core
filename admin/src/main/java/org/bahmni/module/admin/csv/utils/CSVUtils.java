package org.bahmni.module.admin.csv.utils;

import org.bahmni.csv.KeyValue;
import org.bahmni.csv.exception.MigrationException;
import org.openmrs.api.context.Context;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVUtils {

    public static final String ENCOUNTER_DATE_PATTERN = "yyyy-M-d";
    private final static String GLOBAL_DATE_FORMAT = "bahmni.admin.csv.upload.dateFormat";

    public static String getCsvGlobalDateFormat(){
        return Context.getAdministrationService().getGlobalProperty(GLOBAL_DATE_FORMAT);
    };

    public static String[] getStringArray(List<KeyValue> keyValueList) {
        List<String> stringList = new ArrayList<>();
        for (KeyValue keyValue : keyValueList) {
            stringList.add(keyValue.getValue());
        }
        return stringList.toArray(new String[]{});
    }

    public static List<KeyValue> getKeyValueList(String key, List<String> stringList) {
        List<KeyValue> keyValueList = new ArrayList<>();
        for (String string : stringList) {
            keyValueList.add(new KeyValue(key, string));
        }
        return keyValueList;
    }

    public static String getDateStringInSupportedFormat(String dateString) throws ParseException {
        String dateGlobalProperty = getCsvGlobalDateFormat();
        SimpleDateFormat simpleDateFormat;
        if( dateGlobalProperty != null) {
            simpleDateFormat = new SimpleDateFormat(dateGlobalProperty);
            simpleDateFormat.setLenient(false);
            Date date = new Date(simpleDateFormat.parse(dateString).getTime());

            SimpleDateFormat defaultDateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
            return defaultDateFormat.format(date);
        }else{
            return dateString;
        }
    };

    public static Date getDateFromString(String dateString) throws ParseException {
        // All csv imports use the date format from global properties
        SimpleDateFormat simpleDateFormat;
        String dateGlobalProperty = getCsvGlobalDateFormat();
        String expectedDateFormat = dateGlobalProperty != null ? dateGlobalProperty : ENCOUNTER_DATE_PATTERN;
        try {
            if (dateGlobalProperty != null) {
                dateString = getDateStringInSupportedFormat(dateString);
            }
            simpleDateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
            simpleDateFormat.setLenient(false);
            return simpleDateFormat.parse(dateString);
        }
        catch (ParseException e){
            throw new MigrationException("Date format " + dateString + " doesn't match `bahmni.admin.csv.upload.dateFormat` global property, expected format " + expectedDateFormat );
        }
    };

    public static Date getTodayDate() throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(ENCOUNTER_DATE_PATTERN);
        Date date = new Date();
        String dateString = dateFormat.format(date);
        return getDateFromString(dateString);
    }

}
