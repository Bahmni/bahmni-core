package org.bahmni.datamigration.request.patient;

import static org.bahmni.datamigration.DataScrub.scrubData;


public class Name {
    private String familyName;
    private String givenName;

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = scrubData(familyName);
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = scrubData(givenName);
    }
}