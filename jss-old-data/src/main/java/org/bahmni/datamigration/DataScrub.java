package org.bahmni.datamigration;

public class DataScrub {

    public static String scrubData(String value) {
        if(value == null)
            return "";
        return value.replace("\\", "").trim();
    }

}
