package org.bahmni.datamigration.request.patient;

import static org.bahmni.datamigration.DataScrub.scrubData;

public class PatientAddress {
    private  String address1;
    private  String cityVillage;
    private  String address3;
    private  String countyDistrict;
    private  String stateProvince;
    private String address2;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = scrubData(address1);
    }

    public String getCityVillage() {
        return cityVillage;
    }

    public void setCityVillage(String cityVillage) {
        this.cityVillage = scrubData(cityVillage);
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = scrubData(address3);
    }

    public String getCountyDistrict() {
        return countyDistrict;
    }

    public void setCountyDistrict(String countyDistrict) {
        this.countyDistrict = scrubData(countyDistrict);
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = scrubData(stateProvince);
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = scrubData(address2);
    }
}