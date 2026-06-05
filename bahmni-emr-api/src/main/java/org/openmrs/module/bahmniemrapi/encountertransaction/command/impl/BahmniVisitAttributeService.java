package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BahmniVisitAttributeService {
    public static final String VISIT_STATUS_ATTRIBUTE_TYPE = "Visit Status";
    public static final String ADMISSION_STATUS_ATTRIBUTE_TYPE = "Admission Status";
    public static final String OPD_VISIT_TYPE = "OPD";
    public static final String ADMISSION_ENCOUNTER_TYPE = "ADMISSION";
    private static final String DISCHARGE_ENCOUNTER_TYPE = "DISCHARGE";
    public static final String IPD_VISIT_TYPE = "IPD";
    private static final String CONSULTATION_ENCOUNTER_TYPE = "CONSULTATION";
    private VisitService visitService;

    @Autowired
    public BahmniVisitAttributeService(VisitService visitService) {
        this.visitService = visitService;
    }


    public void save(Encounter currentEncounter) {
        Visit updatedVisit = createOrUpdateVisitAttribute(currentEncounter);
        currentEncounter.setVisit(updatedVisit);
    }

    private Visit createOrUpdateVisitAttribute(Encounter currentEncounter) {
        Visit visit = currentEncounter.getVisit();
        if (findVisitAttribute(visit, ADMISSION_STATUS_ATTRIBUTE_TYPE) != null
                && findVisitAttribute(visit, VISIT_STATUS_ATTRIBUTE_TYPE) != null
                && currentEncounter.getEncounterType().getName().equalsIgnoreCase(CONSULTATION_ENCOUNTER_TYPE))
        {
            return visit;
        }
        setVisitStatus(currentEncounter, visit);
        setAdmissionStatus(currentEncounter, visit);
        return visitService.saveVisit(visit);
    }

    private void setAdmissionStatus(Encounter currentEncounter, Visit visit) {
        VisitAttribute admissionStatus = findVisitAttribute(visit, ADMISSION_STATUS_ATTRIBUTE_TYPE);
        if (admissionStatus == null) {
            admissionStatus = createVisitAttribute(visit, null, ADMISSION_STATUS_ATTRIBUTE_TYPE);
        }
        if (currentEncounter.getEncounterType().getName().equalsIgnoreCase(ADMISSION_ENCOUNTER_TYPE)) {
            admissionStatus.setValueReferenceInternal("Admitted");
            visit.setAttribute(admissionStatus);
        }
        if (currentEncounter.getEncounterType().getName().equalsIgnoreCase(DISCHARGE_ENCOUNTER_TYPE)) {
            if(currentEncounter.isVoided()){
                admissionStatus.setValueReferenceInternal("Admitted");
            }else{
                admissionStatus.setValueReferenceInternal("Discharged");
            }
            visit.setAttribute(admissionStatus);
        }
    }

    private void setVisitStatus(Encounter currentEncounter, Visit visit) {
        VisitAttribute visitStatus = findVisitAttribute(visit, VISIT_STATUS_ATTRIBUTE_TYPE);
        if (visitStatus == null) {
            visitStatus = createVisitAttribute(visit, OPD_VISIT_TYPE, VISIT_STATUS_ATTRIBUTE_TYPE);
        }
        if (currentEncounter.getEncounterType().getName().equalsIgnoreCase(ADMISSION_ENCOUNTER_TYPE)) {
            visitStatus.setValueReferenceInternal(IPD_VISIT_TYPE);
        }
        visit.setAttribute(visitStatus);
    }

    private VisitAttribute createVisitAttribute(Visit visit, String value, String visitAttributeTypeName) {
        VisitAttribute visitStatus = new VisitAttribute();
        visitStatus.setVisit(visit);
        visitStatus.setAttributeType(getVisitAttributeType(visitAttributeTypeName));
        visitStatus.setValueReferenceInternal(value);
        return visitStatus;
    }

    private VisitAttributeType getVisitAttributeType(String visitAttributeTypeName) {
        for (VisitAttributeType visitAttributeType : visitService.getAllVisitAttributeTypes()) {
            if (visitAttributeType.getName().equalsIgnoreCase(visitAttributeTypeName)) {
                return visitAttributeType;
            }
        }
        return null;
    }

    private VisitAttribute findVisitAttribute(Visit visit, String visitAttributeTypeName) {
        for (VisitAttribute visitAttribute : visit.getAttributes()) {
            if (visitAttribute.getAttributeType().getName().equalsIgnoreCase(visitAttributeTypeName)) {
                return visitAttribute;
            }
        }
        return null;
    }
}
