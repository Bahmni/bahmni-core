package org.bahmni.module.bahmnicore.forms2.service.impl;

import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.service.BahmniFormDetailsService;
import org.bahmni.module.bahmnicore.forms2.util.FormUtil;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.bahmni.module.bahmnicore.forms2.mapper.FormDetailsMapper.createFormDetails;

@Service
public class BahmniFormDetailsServiceImpl implements BahmniFormDetailsService {

    private final VisitService visitService;
    private final PatientService patientService;
    private final EncounterService encounterService;
    private final ObsService obsService;
    private BahmniVisitService bahmniVisitService;
    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Autowired
    public BahmniFormDetailsServiceImpl(PatientService patientService, VisitService visitService,
                                        EncounterService encounterService, ObsService obsService,
                                        BahmniVisitService bahmniVisitService,
                                        BahmniProgramWorkflowService bahmniProgramWorkflowService) {
        this.visitService = visitService;
        this.patientService = patientService;
        this.encounterService = encounterService;
        this.obsService = obsService;
        this.bahmniVisitService = bahmniVisitService;
        this.bahmniProgramWorkflowService = bahmniProgramWorkflowService;
    }

    @Override
    public Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, int numberOfVisits) {
        Patient patient = getPatient(patientUuid);
        List<Visit> visits = visitService.getVisitsByPatient(patient);

        //Warning: This check is needed to avoid getEncounters returning ALL non-voided encounters of all patients leading to OutOfMemoryError:Java Heap
        //Refer: https://bahmni.atlassian.net/browse/BAH-3513
        if(visits.isEmpty())
            return Collections.emptyList();

        List<Visit> limitedVisits = limitVisits(visits, numberOfVisits);

        List<Encounter> encounters = getEncounters(limitedVisits);

        if (isNotEmpty(encounters) && isNotEmpty(limitedVisits)) {
            return getFormDetails(patient, encounters, formType);
        }
        return Collections.emptyList();
    }

    private Patient getPatient(String patientUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        if (patient == null) {
            throw new InvalidParameterException("Patient does not exist");
        }
        return patient;
    }

    private List<Encounter> getEncounters(List<Visit> visits) {
        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
                .setVisits(visits).createEncounterSearchCriteria();
        return encounterService.getEncounters(encounterSearchCriteria);
    }

    private Collection<FormDetails> getFormDetails(Patient patient, List<Encounter> encounters, FormType formType) {
        Collection<FormDetails> formDetails = new ArrayList<>();
        List<Obs> observations = obsService.getObservations(singletonList(patient.getPerson()), encounters,
                null, null, null, null, null, null, null, null, null, false);
        if (FormType.FORMS2.equals(formType) || (formType == null)) {
            formDetails = createFormDetails(FormUtil.filterFormBuilderObs(observations), FormType.FORMS2);
        }
        return formDetails;
    }

    @Override
    public Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, String visitUuid,
                                                  String patientProgramUuid) {
        Patient patient = getPatient(patientUuid);
        Visit visit = bahmniVisitService.getVisitSummary(visitUuid);
        List<Encounter> encountersByVisitUuid = getEncounters(singletonList(visit));

        Collection<Encounter> encountersByPatientProgramUuid = bahmniProgramWorkflowService
                .getEncountersByPatientProgramUuid(patientProgramUuid);

        if (isNotEmpty(encountersByVisitUuid) && isNotEmpty(encountersByPatientProgramUuid)) {
            List<Encounter> encountersByPatientProgramUuidAndVisitUuid = encountersByPatientProgramUuid.stream()
                    .filter(encounter -> encounter.getVisit().equals(visit)).collect(Collectors.toList());
            return getFormDetails(patient, encountersByPatientProgramUuidAndVisitUuid, formType);
        } else if (isNotEmpty(encountersByVisitUuid)) {
            return getFormDetails(patient, encountersByVisitUuid, formType);
        } else if (isNotEmpty(encountersByPatientProgramUuid)) {
            return getFormDetails(patient, new ArrayList<>(encountersByPatientProgramUuid), formType);
        }
        return Collections.emptyList();
    }

    private List<Visit> limitVisits(List<Visit> visits, int numberOfVisits) {
        if (numberOfVisits <= -1) {
            return visits;
        }
        return visits.size() > numberOfVisits ? visits.subList(0, numberOfVisits) : visits;
    }


}
