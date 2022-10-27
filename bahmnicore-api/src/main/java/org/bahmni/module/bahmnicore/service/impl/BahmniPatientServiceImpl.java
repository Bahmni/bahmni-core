package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicore.dao.PatientDao;
import org.bahmni.module.bahmnicore.service.BahmniPatientService;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.bahmni.module.bahmnicommons.visitlocation.BahmniVisitLocationServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Lazy //to toString rid of cyclic dependencies
@Transactional
public class BahmniPatientServiceImpl implements BahmniPatientService {
    private PersonService personService;
    private ConceptService conceptService;
    private PatientDao patientDao;
    private static final Logger log = LogManager.getLogger(BahmniPatientServiceImpl.class);

    //@Autowired
    public BahmniPatientServiceImpl(PersonService personService, ConceptService conceptService,
                                    PatientDao patientDao) {
        this.personService = personService;
        this.conceptService = conceptService;
        this.patientDao = patientDao;
    }

    @Override
    public PatientConfigResponse getConfig() {
        List<PersonAttributeType> personAttributeTypes = personService.getAllPersonAttributeTypes();

        PatientConfigResponse patientConfigResponse = new PatientConfigResponse();
        for (PersonAttributeType personAttributeType : personAttributeTypes) {
            Concept attributeConcept = null;
            if (personAttributeType.getFormat().equals("org.openmrs.Concept")) {
                attributeConcept = conceptService.getConcept(personAttributeType.getForeignKey());
            }
            patientConfigResponse.addPersonAttribute(personAttributeType, attributeConcept);
        }
        return patientConfigResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Patient> get(String partialIdentifier, boolean shouldMatchExactPatientId) {
        return patientDao.getPatients(partialIdentifier, shouldMatchExactPatientId);
    }

    @Override
    public List<RelationshipType> getByAIsToB(String aIsToB) {
        return patientDao.getByAIsToB(aIsToB);
    }

    private Location getVisitLocation(String loginLocationUuid) {
        if (StringUtils.isBlank(loginLocationUuid)) {
            return null;
        }
        BahmniVisitLocationServiceImpl bahmniVisitLocationService = new BahmniVisitLocationServiceImpl(Context.getLocationService());
        return bahmniVisitLocationService.getVisitLocation(loginLocationUuid);
    }

}
