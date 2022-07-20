package org.bahmni.module.elisatomfeedclient.api.domain;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openmrs.Encounter;
import org.openmrs.Provider;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

public class OpenElisAccessionNote {
    private String note;
    private String providerUuid;
    private String dateTime;

    public OpenElisAccessionNote() {
    }

    public OpenElisAccessionNote(String note, String providerUuid, String dateTime) {
        this.note = note;
        this.providerUuid = providerUuid;
        this.dateTime = dateTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }


    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isProviderInEncounter(Encounter encounter){
        return encounter.getEncounterProviders().iterator().next().getProvider().getUuid().equals(providerUuid);
    }

    public boolean matchesProvider(Provider provider){
        return  providerUuid.equals(provider.getUuid());
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenElisAccessionNote that = (OpenElisAccessionNote) o;
        return (note != null && note.equals(that.note) &&
                providerUuid != null && providerUuid.equals(that.providerUuid));

    }

    @Override
    public int hashCode() {
        int result = note != null ? note.hashCode() : 0;
        result = 31 * result + (providerUuid != null ? providerUuid.hashCode() : 0);
        return result;
    }

    public Date getDateTimeAsDate() throws ParseException {
        return DateUtils.parseDateStrictly(dateTime.toString(), Locale.US, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.getPattern());
    }
}
