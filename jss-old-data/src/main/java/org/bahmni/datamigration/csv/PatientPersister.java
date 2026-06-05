package org.bahmni.datamigration.csv;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.RowResult;
import org.bahmni.datamigration.AddressService;
import org.bahmni.datamigration.AllLookupValues;
import org.bahmni.datamigration.FullyQualifiedTehsil;
import org.bahmni.datamigration.LookupValueProvider;
import org.bahmni.datamigration.request.patient.CenterId;
import org.bahmni.datamigration.request.patient.Name;
import org.bahmni.datamigration.request.patient.PatientAddress;
import org.bahmni.datamigration.request.patient.PatientAttribute;
import org.bahmni.datamigration.request.patient.PatientRequest;
import org.bahmni.jss.registration.RegistrationFields;
import org.bahmni.jss.registration.RegistrationNumber;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.bahmni.jss.registration.RegistrationFields.sentenceCase;

public class PatientPersister implements EntityPersister<Patient> {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Log log = LogFactory.getLog(PatientPersister.class);
    private static Logger logger = LogManager.getLogger(PatientPersister.class);

    private static int count;

    private final HashMap<String, AllLookupValues> lookupValuesMap;
    private final AddressService addressService;
    private final AllPatientAttributeTypes allPatientAttributeTypes;
    private OpenMRSRESTConnection openMRSRESTConnection;
    private String sessionId;

    private RestTemplate restTemplate = new RestTemplate();

    public PatientPersister(HashMap<String, AllLookupValues> lookupValuesMap, AddressService addressService,
                            AllPatientAttributeTypes allPatientAttributeTypes, OpenMRSRESTConnection openMRSRESTConnection, String sessionId) {
        this.lookupValuesMap = lookupValuesMap;
        this.addressService = addressService;
        this.allPatientAttributeTypes = allPatientAttributeTypes;
        this.openMRSRESTConnection = openMRSRESTConnection;
        this.sessionId = sessionId;
    }

    @Override
    public RowResult<Patient> persist(Patient patient) {
        int i = incrementCounter();
        PatientRequest patientRequest = createPatientRequest(patient);

        String jsonRequest = null;
        ResponseEntity<String> out;
        try {
            jsonRequest = objectMapper.writeValueAsString(patientRequest);
            if (logger.isDebugEnabled()) logger.debug(jsonRequest);

            HttpHeaders httpHeaders = getHttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity(patientRequest, httpHeaders);

            String url = openMRSRESTConnection.getRestApiUrl() + "bahmnicore/patient";
            out = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (logger.isDebugEnabled()) logger.debug(out.getBody());
            log.info(String.format("%d Successfully created %s", i, patientRequest.getIdentifier()));
        } catch (HttpServerErrorException serverErrorException) {
            log.info(String.format("%d Failed to create %s", i, patientRequest.getIdentifier()));
            log.info("Patient request: " + jsonRequest);
            log.error("Patient create response: " + serverErrorException.getResponseBodyAsString());
            return new RowResult(patient, serverErrorException);
        } catch (Exception e) {
            log.info(String.format("%d Failed to create", i));
            log.info("Patient request: " + jsonRequest);
            log.error("Failed to process a patient", e);
            log.info("Patient request: " + jsonRequest);
            return new RowResult(patient, e);
        }

        return new RowResult(patient);
    }

    @Override
    public RowResult<Patient> validate(Patient patient) {
        return new RowResult<>(patient);
    }

    private synchronized int incrementCounter() {
        return count++;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Cookie", "JSESSIONID=" + sessionId);
        return requestHeaders;
    }

    private PatientRequest createPatientRequest(Patient patient) {
        try {
            PatientRequest patientRequest = new PatientRequest();
            RegistrationNumber registrationNumber = RegistrationFields.parseRegistrationNumber(patient.registrationNumber);
            CenterId centerID = new CenterId(registrationNumber.getCenterCode());
            patientRequest.setIdentifier(centerID.getName() + registrationNumber.getId());
            patientRequest.setCenterID(centerID);

            Name name = RegistrationFields.name(patient.firstName, patient.lastName);
            patientRequest.setName(RegistrationFields.sentenceCase(name.getGivenName()), RegistrationFields.sentenceCase(name.getFamilyName()));

            addPatientAttribute(patient.fathersName, patientRequest, "primaryRelative", null, 0);
            patientRequest.setDateOfRegistration(RegistrationFields.getDate(patient.registrationDate));

            patientRequest.setGender(patient.sex);
            String birthdate = RegistrationFields.getDate(patient.dob);
            patientRequest.setBirthdate(birthdate == null ? RegistrationFields.UnknownDateOfBirthAsString : birthdate);

            LinkedHashMap<Object, Object> ageMap = new LinkedHashMap<>();
            ageMap.put("years", RegistrationFields.getAge(patient.age));
            patientRequest.setAge(ageMap);

            PatientAddress patientAddress = new PatientAddress();
            patientRequest.addPatientAddress(patientAddress);

            patientRequest.setBalance(patient.balanceAmount);

            addPatientAttribute(patient.casteId, patientRequest, "caste", lookupValuesMap.get("Castes"), 0);
            addPatientAttribute(patient.classId, patientRequest, "class", lookupValuesMap.get("Classes"), 0);

            //Address information
            patientAddress.setAddress2(sentenceCase(patient.gramPanch));

            FullyQualifiedTehsil fullyQualifiedTehsil = new FullyQualifiedTehsil();
            String stateId = lookupValuesMap.get("Districts").getLookUpValue(patient.districtId, 0);
            if (stateId != null) {
                String state = lookupValuesMap.get("States").getLookUpValue(stateId);
                fullyQualifiedTehsil.setState(sentenceCase(state));
            }

            String district = lookupValuesMap.get("Districts").getLookUpValue(patient.districtId, 2);
            fullyQualifiedTehsil.setDistrict(sentenceCase(district));

            String village = patient.village;
            patientAddress.setCityVillage(sentenceCase(village));

            String tehsil = patient.tahsil;
            fullyQualifiedTehsil.setTehsil(sentenceCase(tehsil));

            FullyQualifiedTehsil correctedFullyQualifiedTehsil = addressService.getTehsilFor(fullyQualifiedTehsil);
            setPatientAddressFrom(correctedFullyQualifiedTehsil, patientAddress);
            return patientRequest;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create request from this row: " + ArrayUtils.toString(patient.getOriginalRow()), e);
        }
    }

    private void setPatientAddressFrom(FullyQualifiedTehsil fullyQualifiedTehsil, PatientAddress patientAddress) {
        patientAddress.setStateProvince(fullyQualifiedTehsil.getState());
        patientAddress.setCountyDistrict(fullyQualifiedTehsil.getDistrict());
        patientAddress.setAddress3(fullyQualifiedTehsil.getTehsil());
    }

    private void addPatientAttribute(String value, PatientRequest patientRequest, String name,
                                     LookupValueProvider lookupValueProvider, int valueIndex) {
        if (lookupValueProvider != null) {
            String lookUpValue = lookupValueProvider.getLookUpValue(value, valueIndex);
            if (lookUpValue == null) return;
        }
        if (StringUtils.isEmpty(value)) return;

        PatientAttribute patientAttribute = new PatientAttribute();
        patientAttribute.setAttributeType(allPatientAttributeTypes.getAttributeUUID(name));
        patientAttribute.setName(name);
        String valueToSet = lookupValueProvider == null ? value : lookupValueProvider.getLookUpValue(value, valueIndex);
        valueToSet = "class".equals(name) ? valueToSet : sentenceCase(valueToSet);
        patientAttribute.setValue(valueToSet);
        patientRequest.addPatientAttribute(patientAttribute);
    }

}
