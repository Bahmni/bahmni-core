package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientIdentifierMapper {
    
    public static final String BAHMNI_PRIMARY_IDENTIFIER_TYPE = "bahmni.primaryIdentifierType";
    private PatientService patientService;
    private AdministrationService administrationService;

    @Autowired
    public PatientIdentifierMapper(PatientService patientService,
                                    @Qualifier("adminService") AdministrationService administrationService) {
        this.patientService = patientService;
        this.administrationService = administrationService;
    }

    public Patient map(BahmniPatient bahmniPatient, Patient patient) {
		PatientIdentifier patientIdentifier;
		String existingIdentifierValue = bahmniPatient.getIdentifier();
		
		if (existingIdentifierValue == null || existingIdentifierValue.trim().isEmpty()) {
			patientIdentifier = generateIdentifier(bahmniPatient.getCenterName());
		} else {
			PatientIdentifierType identifierType = getPatientIdentifierType();
			patientIdentifier = new PatientIdentifier(existingIdentifierValue, identifierType, null);
		}
		
		patientIdentifier.setPreferred(true);
		patient.addIdentifier(patientIdentifier);
        return patient;
	}

    public BahmniPatient mapFromPatient(BahmniPatient bahmniPatient, Patient patient) {
        if(bahmniPatient == null){
            bahmniPatient = new BahmniPatient();
        }
        bahmniPatient.setIdentifier(patient.getPatientIdentifier().getIdentifier());
        return bahmniPatient;
    }
	
	private PatientIdentifier generateIdentifier(String centerName) {
		IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
		List<IdentifierSource> allIdentifierSources = identifierSourceService.getAllIdentifierSources(false);
        for (IdentifierSource identifierSource : allIdentifierSources) {
			if (identifierSource.getName().equals(centerName)) {
				String identifier = identifierSourceService.generateIdentifier(identifierSource, "Bahmni Registration App");
                PatientIdentifierType identifierType = getPatientIdentifierType();
                return new PatientIdentifier(identifier, identifierType, null);
			}
		}
		return null;
	}

    private PatientIdentifierType getPatientIdentifierType() {
        String globalProperty = administrationService.getGlobalProperty(BAHMNI_PRIMARY_IDENTIFIER_TYPE);
        PatientIdentifierType patientIdentifierByUuid = patientService.getPatientIdentifierTypeByUuid(globalProperty);
        return patientIdentifierByUuid;
    }
}
