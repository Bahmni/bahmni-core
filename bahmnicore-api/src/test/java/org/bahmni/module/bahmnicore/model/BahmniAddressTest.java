package org.bahmni.module.bahmnicore.model;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static junit.framework.Assert.assertEquals;

public class BahmniAddressTest {
	
	@Test
	public void shouldCreateAddressFromSimpleObject() {
        String address1 = "someAddress1";
        String address2 = "someAddress2";
		String address3 = "someAddress3";
		String address4 = "someAddress4";
		String address5 = "someAddress5";
		String address6 = "someAddress6";
        String stateProvince = "somestateProvince";
        String countyDistrict = "somecountyDistrict";
        String cityVillage = "somecityVillage";
        String postalCode = "somepostalCode";
        String country = "somecountry";
        String latitude = "somelatitude";
        String longitude = "longitude";
		SimpleObject addressObject = new SimpleObject().add("address1", address1).add("address2", address2).add("address3",
		    address3).add("address4", address4).add("address5", address5).add("address6",
		    address6).add("cityVillage", cityVillage).add("countyDistrict", countyDistrict).add("stateProvince",
		    stateProvince).add("postalCode", postalCode).add("country", country).add("latitude",
            latitude).add("longitude", longitude);
		
		BahmniAddress address = new BahmniAddress(addressObject);
		
		assertEquals(address1, address.getAddress1());
		assertEquals(address2, address.getAddress2());
		assertEquals(address3, address.getAddress3());
		assertEquals(address4, address.getAddress4());
		assertEquals(address5, address.getAddress5());
		assertEquals(address6, address.getAddress6());
		assertEquals(cityVillage, address.getCityVillage());
		assertEquals(countyDistrict, address.getCountyDistrict());
		assertEquals(stateProvince, address.getStateProvince());
		assertEquals(postalCode, address.getPostalCode());
		assertEquals(country, address.getCountry());
		assertEquals(latitude, address.getLatitude());
		assertEquals(longitude, address.getLongitude());
	}
}
