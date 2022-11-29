package org.bahmni.module.admin.csv.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;
import static org.bahmni.module.admin.csv.utils.CSVUtils.getTodayDate;

public class CSVRelationshipService {

    private static final String BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY = "bahmni.relationshipTypeMap";
    private static final String PATIENT_RELATIONSHIP = "patient";
    private static final String PROVIDER_RELATIONSHIP = "provider";

    private BahmniPatientService patientService;
    private PersonService personService;
    private ProviderService providerService;
    private AdministrationService administrationService;


    public CSVRelationshipService(BahmniPatientService patientService, PersonService personService, ProviderService providerService, AdministrationService administrationService) {
        this.patientService = patientService;
        this.personService = personService;
        this.providerService = providerService;
        this.administrationService = administrationService;
    }

    public Relationship save(RelationshipRow relationshipRow) throws ParseException {
        List<Patient> patientList = patientService.get(relationshipRow.getPatientIdentifier(), true);
        if (null == patientList || patientList.size() == 0) {
            throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPatientIdentifier() + "'");
        } else {
            Patient patient = patientList.get(0);
            Relationship relationship = createRelationship(relationshipRow, patient);
            return personService.saveRelationship(relationship);
        }
    }

    private Relationship createRelationship(RelationshipRow relationshipRow, Patient patient) throws ParseException {

        RelationshipType relationshipType = getMatchingRelationship(relationshipRow.getRelationshipType());
        Person personB = getRelatedPerson(relationshipRow);
        Relationship relationship = checkForExistingRelationship(patient, relationshipType, personB);

        if (relationship == null){
            relationship = new Relationship();
            relationship.setPersonA(patient);
            relationship.setPersonB(personB);
            relationship.setRelationshipType(relationshipType);
        }

        relationship.setStartDate(getStartDate(relationshipRow));
        relationship.setEndDate(getEndDate(relationshipRow));
        return relationship;
    }

    private Person getRelatedPerson(RelationshipRow relationshipRow) {
        String relationshipMapProperty = administrationService.getGlobalProperty(BAHMNI_RELATIONSHIP_TYPE_MAP_PROPERTY);
        Map<String, Object> relationshipMap = new Gson().fromJson(relationshipMapProperty, new TypeToken<HashMap<String, Object>>() {
        }.getType());

        if (isProviderRelationship(relationshipMap, relationshipRow.getRelationshipType())) {
            return getProvider(relationshipRow);

        } else if (isPatientRelationship(relationshipMap, relationshipRow.getRelationshipType())) {
            return getPatient(relationshipRow);

        } else {
            throw new RuntimeException("Relationship not found " + relationshipRow.getProviderName());
        }
    }

    private Person getProvider(RelationshipRow relationshipRow) {
        if (StringUtils.isEmpty(relationshipRow.getProviderName())) {
            throw new RuntimeException("Provider name not found");
        }
        List<Provider> matchedProvider = providerService.getProviders(relationshipRow.getProviderName(), null, null, null);
        if (CollectionUtils.isEmpty(matchedProvider)) {
            throw new RuntimeException("No matching provider found with ID:'" + relationshipRow.getProviderName() + "'");
        }
        return matchedProvider.get(0).getPerson();
    }

    private Person getPatient(RelationshipRow relationshipRow) {
        List<Patient> matchedPatient = patientService.get(relationshipRow.getPatientRelationIdentifier(), true);

        if (CollectionUtils.isEmpty(matchedPatient)) {
            throw new RuntimeException("No matching patients found with ID:'" + relationshipRow.getPatientRelationIdentifier() + "'");
        }
        return matchedPatient.get(0);
    }

    private Relationship checkForExistingRelationship(Patient patient, RelationshipType relationshipType, Person matchedPerson) {
        List<Relationship> existingRelationship = personService.getRelationships(patient, matchedPerson, relationshipType);
        if (CollectionUtils.isNotEmpty(existingRelationship)) {
            return existingRelationship.get(0);
        }
        return null;
    }

    private RelationshipType getMatchingRelationship(String relationshipType) {
        List<RelationshipType> relationshipTypes = patientService.getByAIsToB(relationshipType);

        if (CollectionUtils.isEmpty(relationshipTypes)) {
            throw new RuntimeException("No matching relationship type found with relationship type name:'" + relationshipType + "'");
        }
        return relationshipTypes.get(0);
    }

    private List<String> getRelationshipTypes(Map<String, Object> relationshipMap, String relationship) {
        return relationshipMap != null ? (List<String>) relationshipMap.get(relationship) : null;
    }

    private boolean isProviderRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PROVIDER_RELATIONSHIP);
        return relationshipTypes != null && containsIgnoreCase(relationshipTypes, relationshipType);
    }

    private boolean isPatientRelationship(Map<String, Object> relationshipMap, String relationshipType) {
        List<String> relationshipTypes = getRelationshipTypes(relationshipMap, PATIENT_RELATIONSHIP);
        return relationshipTypes != null && containsIgnoreCase(relationshipTypes, relationshipType);
    }

    private Date getStartDate(RelationshipRow relationshipRow) throws ParseException {
        if (!StringUtils.isEmpty(relationshipRow.getStartDate())) {
            return getDateFromString(relationshipRow.getStartDate());
        }
        return getTodayDate();
    }

    private Date getEndDate(RelationshipRow relationshipRow) throws ParseException {
        if (!StringUtils.isEmpty(relationshipRow.getEndDate())) {
            return getDateFromString(relationshipRow.getEndDate());
        }
        return null;
    }

    private boolean containsIgnoreCase(List<String> relationshipTypes, String relationshipType) {
        for (String relType : relationshipTypes) {
            if(relationshipType.equalsIgnoreCase(relType)){
                return true;
            }
        }
        return false;
    }

}