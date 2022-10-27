package org.bahmni.module.bahmnicore.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicommons.contract.patient.PatientSearchParameters;
import org.bahmni.module.bahmnicommons.contract.patient.response.PatientResponse;
import org.bahmni.module.bahmnicore.contract.patient.search.PatientSearchQueryBuilder;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.openmrs.ProgramAttributeType;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PatientDaoImpl implements PatientDao {

    private SessionFactory sessionFactory;
    private final Logger log = LogManager.getLogger(PatientDaoImpl.class);

    public PatientDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private List<String> patientAddressFields = Arrays.asList("country", "state_province", "county_district", "city_village",
            "postal_code", "address1", "address2", "address3",
            "address4", "address5", "address6", "address7", "address8",
            "address9", "address10", "address11", "address12",
            "address13", "address14", "address15");

    @Override
    public List<PatientResponse> getPatients(PatientSearchParameters searchParameters, Supplier<Location> visitLocation, Supplier<List<String>> configuredAddressFields) {
        validateSearchParams(searchParameters.getPatientAttributes(), searchParameters.getProgramAttributeFieldName(), searchParameters.getAddressFieldName());
        ProgramAttributeType programAttributeType = getProgramAttributeType(searchParameters.getProgramAttributeFieldName());
        List<String> addressLevelFields = configuredAddressFields.get();
        Location location = visitLocation.get();
        validateLocation(location, searchParameters);

        List<PersonAttributeType> patientAttributes = getPersonAttributes(searchParameters.getPatientAttributes());
        List<PersonAttributeType> patientSearchResultAttributes = getPersonAttributes(searchParameters.getPatientSearchResultFields());

        SQLQuery sqlQuery = new PatientSearchQueryBuilder(sessionFactory)
                .withPatientName(searchParameters.getName())
                .withPatientAddress(searchParameters.getAddressFieldName(), searchParameters.getAddressFieldValue(), searchParameters.getAddressSearchResultFields(), addressLevelFields)
                .withPatientIdentifier(searchParameters.getIdentifier(), searchParameters.getFilterOnAllIdentifiers())
                .withPatientAttributes(searchParameters.getCustomAttribute(),
                        patientAttributes,
                        patientSearchResultAttributes)
                .withProgramAttributes(searchParameters.getProgramAttributeFieldValue(), programAttributeType)
                .withLocation(location, searchParameters.getFilterPatientsByLocation())
                .buildSqlQuery(searchParameters.getLength(), searchParameters.getStart());
        try {
            return sqlQuery.list();
        } catch (Exception e) {
            log.error("Error occurred while trying to execute patient search query.", e);
            throw new RuntimeException("Error occurred while to perform patient search");
        }
    }

    @Override
    public List<String> getConfiguredPatientAddressFields() {
        return this.patientAddressFields;
        /**
         * AbstractEntityPersister aep=((AbstractEntityPersister) sessionFactory.getClassMetadata(PersonAddress.class));
         *         String[] properties=aep.getPropertyNames();
         *         for(int nameIndex=0;nameIndex!=properties.length;nameIndex++){
         *             System.out.println("Property name: "+properties[nameIndex]);
         *             String[] columns=aep.getPropertyColumnNames(nameIndex);
         *             for(int columnIndex=0;columnIndex!=columns.length;columnIndex++){
         *                 System.out.println("Column name: "+columns[columnIndex]);
         *             }
         *         }
         */
    }

    private void validateLocation(Location location, PatientSearchParameters searchParameters) {
        if (searchParameters.getFilterPatientsByLocation() && location == null) {
            log.error(String.format("Invalid parameter Location: %s", searchParameters.getLoginLocationUuid()));
            throw new IllegalArgumentException("Invalid Location specified");
        }
    }

    private void validateSearchParams(String[] customAttributeFields, String programAttributeFieldName, String addressFieldName) {
        List<Integer> personAttributeIds = getPersonAttributeIds(customAttributeFields);
        if (customAttributeFields != null && personAttributeIds.size() != customAttributeFields.length) {
            log.error(String.format("Invalid Patient Attribute(s) specified: [%s]", StringUtils.join(customAttributeFields, ", ")));
            //TODO, do not reveal information
            throw new IllegalArgumentException(String.format("Invalid Attribute In Patient Attributes [%s]", StringUtils.join(customAttributeFields, ", ")));
        }

        ProgramAttributeType programAttributeTypeId = getProgramAttributeType(programAttributeFieldName);
        if (!StringUtils.isBlank(programAttributeFieldName) && programAttributeTypeId == null) {
            log.error("Invalid Program Attribute specified, name: " + programAttributeFieldName);
            throw new IllegalArgumentException("Invalid Program Attribute");
        }


        if (!isValidAddressField(addressFieldName)) {
            log.error("Invalid address field:" + addressFieldName);
            throw new IllegalArgumentException(String.format("Invalid address parameter"));
        }
    }

    /**
     * This should not be querying the information schema at all.
     * Most of the time, the table columns that are fixed in nature should suffice.
     * If not, we can introduce external property.
     * Or worst case use Hibernate mappings to find column names. see {@link #getConfiguredPatientAddressFields()}.
     * @param addressFieldName
     * @return
     */
    private boolean isValidAddressField(String addressFieldName) {
        if (StringUtils.isBlank(addressFieldName)) return true;
        return patientAddressFields.contains(addressFieldName.toLowerCase());
    }

    private ProgramAttributeType getProgramAttributeType(String programAttributeField) {
        if (StringUtils.isEmpty(programAttributeField)) {
            return null;
        }

        return (ProgramAttributeType) sessionFactory.getCurrentSession().createCriteria(ProgramAttributeType.class).
                add(Restrictions.eq("name", programAttributeField)).uniqueResult();
    }

    private List<PersonAttributeType> getPersonAttributes(String[] patientAttributes) {
        if (patientAttributes == null || patientAttributes.length == 0) {
            return new ArrayList<>();
        }
        return sessionFactory.getCurrentSession().createCriteria(PersonAttributeType.class).
                add(Restrictions.in("name", patientAttributes)).list();
    }

    private List<Integer> getPersonAttributeIds(String[] patientAttributes) {
        if (patientAttributes == null || patientAttributes.length == 0) {
            return new ArrayList<>();
        }

        String query = "select person_attribute_type_id from person_attribute_type where name in " +
                "( :personAttributeTypeNames)";
        Query queryToGetAttributeIds = sessionFactory.getCurrentSession().createSQLQuery(query);
        queryToGetAttributeIds.setParameterList("personAttributeTypeNames", Arrays.asList(patientAttributes));
        List list = queryToGetAttributeIds.list();
        return (List<Integer>) list;
    }

    @Override
    public Patient getPatient(String identifier) {
        Session currentSession = sessionFactory.getCurrentSession();
        List<PatientIdentifier> ident = currentSession.createQuery("from PatientIdentifier where identifier = :ident").setString("ident", identifier).list();
        if (!ident.isEmpty()) {
            return ident.get(0).getPatient();
        }
        return null;
    }

    @Override
    public List<Patient> getPatients(String patientIdentifier, boolean shouldMatchExactPatientId) {
        if (!shouldMatchExactPatientId) {
            String partialIdentifier = "%" + patientIdentifier;
            Query querytoGetPatients = sessionFactory.getCurrentSession().createQuery(
                    "select pi.patient " +
                            " from PatientIdentifier pi " +
                            " where pi.identifier like :partialIdentifier ");
            querytoGetPatients.setString("partialIdentifier", partialIdentifier);
            return querytoGetPatients.list();
        }

        Patient patient = getPatient(patientIdentifier);
        List<Patient> result = (patient == null ? new ArrayList<Patient>() : Arrays.asList(patient));
        return result;
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        Query querytoGetPatients = sessionFactory.getCurrentSession().createQuery(
                "select rt " +
                        " from RelationshipType rt " +
                        " where rt.aIsToB = :aIsToB ");
        querytoGetPatients.setString("aIsToB", aIsToB);
        return querytoGetPatients.list();
    }
}
