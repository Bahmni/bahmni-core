package org.bahmni.module.bahmnicore.util;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class BahmniDateUtil {

    public enum DateFormatType {
        UTC("yyyy-MM-dd'T'HH:mm:ss.SSS");

        private final String dateFormat;

        DateFormatType(String dateFormat) {
            this.dateFormat = dateFormat;
        }

        public String getDateFormat() {
            return dateFormat;
        }
    }

    public static Date convertToDate(String dateString, DateFormatType dateFormat) throws ParseException {
        if (StringUtils.isEmpty(dateString) || dateFormat == null) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat.getDateFormat());
        return simpleDateFormat.parse(dateString);
    }

    public static Date convertToDate(String dateString, String dateFormat) throws ParseException {
        if (StringUtils.isEmpty(dateString) || StringUtils.isEmpty(dateFormat)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.parse(dateString);
    }

    public static Date convertToLocalDateFromUTC(String dateString) throws ParseException {
        if (StringUtils.isEmpty(dateString)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(BahmniDateUtil.DateFormatType.UTC.dateFormat);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.parse(dateString);
    }
}
