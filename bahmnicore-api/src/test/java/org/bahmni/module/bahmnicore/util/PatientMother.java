package org.bahmni.module.bahmnicore.util;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class PatientMother {

    private String patientIdentifier;
    private NameMother nameMother = new NameMother();
    private AddressMother addressMother = new AddressMother();
    private String balance;
    private Date dateCreated;

    public PatientMother() {
        patientIdentifier = "GAN11223344";
    }

    public PatientMother withName(String firstName, String middleName, String lastName) {
        nameMother = nameMother.withName(firstName, middleName, lastName);
        return this;
    }

    public PatientMother withPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
        return this;
    }

    public PatientMother withBalance(String balance) {
        this.balance = balance;
        return this;
    }

    public PatientMother withDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    public Patient build() {
        Patient patient = new Patient();
        patient.addIdentifier(new PatientIdentifier(patientIdentifier, null, null));
        patient.addName(nameMother.build());
        patient.setPersonDateCreated(this.dateCreated);
        patient.setAddresses(new HashSet<>(Arrays.asList(addressMother.build())));
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("healthCenter");
        patient.setAttributes(new HashSet<>(Arrays.asList(new PersonAttribute(personAttributeType, "Ganiyari"))));
        return patient;
    }

    public SimpleObject buildSimpleObject() {
        String dateCreatedString = dateCreated != null ? new SimpleDateFormat("dd-MM-yyyy").format(dateCreated) : "";
        SimpleObject simpleObject = new SimpleObject().add("birthdate", "01-01-2012")
                .add("age", new SimpleObject().add("years", 21).add("months", 1).add("days", 3))
                .add("gender", "M")
                .add("attributes", Arrays.asList(new SimpleObject()
                        .add("attributeType", "b3b6d540-a32e-44c7-91b3-292d97667518")
                        .add("value", "someCaste")))
                .add("addresses", Arrays.asList(addressMother.getSimpleObjectForAddress()))
                .add("centerID", "Ganiyari")
                .add("names", Arrays.asList(nameMother.getSimpleObjectForName()))
                .add("dateOfRegistration", dateCreatedString)
                .add("identifier", patientIdentifier);
        if (balance != null) {
            simpleObject.add("balance", balance);
        }

        return simpleObject;
    }

    public BahmniPatient buildBahmniPatient() throws ParseException {
        SimpleObject simpleObject = buildSimpleObject();
        return new BahmniPatient(simpleObject);
    }
}


