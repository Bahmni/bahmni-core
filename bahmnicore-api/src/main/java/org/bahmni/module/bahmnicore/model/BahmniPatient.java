package org.bahmni.module.bahmnicore.model;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class BahmniPatient {
	private Date birthdate;
	private Age age;
	private String centerName;
	private String identifier;
	private List<BahmniPersonAttribute> attributes = new ArrayList<BahmniPersonAttribute>();
	private List<BahmniAddress> addresses = new ArrayList<BahmniAddress>();
	private List<BahmniName> names = new ArrayList<BahmniName>();
	private String gender;
    private String image;
    private String uuid;
    private String balance;
    private Date personDateCreated;
    private static Logger logger = LogManager.getLogger(BahmniPatient.class);

    public BahmniPatient() {
    }

    public BahmniPatient(SimpleObject post) throws ParseException {
		SimpleObjectExtractor extractor = new SimpleObjectExtractor(post);
        balance = extractor.extract("balance");
		age = Age.fromHash(extractor.<LinkedHashMap>extract("age"));
		identifier = extractor.extract("identifier");
		image = extractor.extract("image");
		gender = extractor.extract("gender");
		centerName = extractor.extract("centerID");

        extractRegistrationDate(extractor);
        extractBirthdate(extractor);

        List<LinkedHashMap> nameList = extractor.extract("names");
		for (LinkedHashMap name : nameList) {
			names.add(new BahmniName(name));
		}
		
		List<LinkedHashMap> addressList = extractor.extract("addresses");
		for (LinkedHashMap address : addressList) {
			addresses.add(new BahmniAddress(address));
		}
		
		List<LinkedHashMap> attributeList = extractor.extract("attributes");
		for (LinkedHashMap attribute : attributeList) {
			attributes.add(new BahmniPersonAttribute(attribute));
		}
	}

    private void extractBirthdate(SimpleObjectExtractor extractor) {
        try {
            birthdate = new SimpleDateFormat("dd-MM-yyyy").parse(extractor.<String> extract("birthdate"));
        }
        catch (Exception e) {
            logger.warn(e);
        }
    }

    private void extractRegistrationDate(SimpleObjectExtractor extractor) {
        try {
            personDateCreated = new SimpleDateFormat("dd-MM-yyyy").parse(extractor.<String>extract("dateOfRegistration"));
        }
        catch (Exception e) {
            logger.warn(e);
        }
    }

    public Date getBirthdate() {
		return birthdate;
	}
	
	public Age getAge() {
		return age;
	}
	
	public List<BahmniAddress> getAddresses() {
		return addresses;
	}
	
	public List<BahmniName> getNames() {
		return names;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getCenterName() {
		return centerName;
	}
	
	public List<BahmniPersonAttribute> getAttributes() {
		return attributes;
	}
	
	public String getGender() {
		return gender;
	}

    public String getImage() {
        return image;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getBalance() {
        return (balance == null || StringUtils.isEmpty(balance)) ? Double.NaN : Double.parseDouble(balance);
    }

    public String getFullName() {
        return names.get(0).getFullName();
    }

    public String getPatientName() {
        BahmniName patientName = getNames().get(0);
        return patientName.getGivenName() + " " + patientName.getFamilyName();
    }

    public boolean hasBalance() {
        return !Double.isNaN(getBalance()) && getBalance() > 0;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Date getPersonDateCreated() {
        return personDateCreated;
    }

    public void setPersonDateCreated(Date personDateCreated) {
        this.personDateCreated = personDateCreated;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void addName(BahmniName bahmniName) {
        names.add(bahmniName);
    }

    public void addAttribute(BahmniPersonAttribute bahmniPersonAttribute) {
        attributes.add(bahmniPersonAttribute);
    }

    public void addAddress(BahmniAddress bahmniAddress) {
        addresses.add(bahmniAddress);
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setCenter(String center) {
        this.centerName = center;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public void setBirthDate(Date birthDate) {
        this.birthdate = birthDate;
    }


}
