package org.bahmni.module.bahmnicore.model;

import java.util.LinkedHashMap;

public class BahmniAddress {
	
	private String address1;
	private String address2;
	private String address3;
    private String address4;
    private String address5;
    private String address6;
	private String cityVillage;
	private String countyDistrict;
	private String stateProvince;
    private String postalCode;
    private String country;
    private String latitude;
    private String longitude;

    public BahmniAddress() {
    }

    public BahmniAddress(LinkedHashMap post) {
		SimpleObjectExtractor extractor = new SimpleObjectExtractor(post);
		address1 = extractor.extract("address1");
		address2 = extractor.extract("address2");
		address3 = extractor.extract("address3");
		address4 = extractor.extract("address4");
		address5 = extractor.extract("address5");
		address6 = extractor.extract("address6");
		cityVillage = extractor.extract("cityVillage");
		countyDistrict = extractor.extract("countyDistrict");
		stateProvince = extractor.extract("stateProvince");
		postalCode = extractor.extract("postalCode");
		country = extractor.extract("country");
		latitude = extractor.extract("latitude");
		longitude = extractor.extract("longitude");
	}
	
	public String getAddress1() {
		return address1;
	}
	
	public String getAddress2() {
		return address2;
	}
	
	public String getAddress3() {
		return address3;
	}
	
	public String getCityVillage() {
		return cityVillage;
	}
	
	public String getCountyDistrict() {
		return countyDistrict;
	}
	
	public String getStateProvince() {
		return stateProvince;
	}

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public void setCityVillage(String cityVillage) {
        this.cityVillage = cityVillage;
    }

    public void setCountyDistrict(String countyDistrict) {
        this.countyDistrict = countyDistrict;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getAddress4() {
        return address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    public String getAddress5() {
        return address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }


    public String getAddress6() {
        return address6;
    }

    public void setAddress6(String address6) {
        this.address6 = address6;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
