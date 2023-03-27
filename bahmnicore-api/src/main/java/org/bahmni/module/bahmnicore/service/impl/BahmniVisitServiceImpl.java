package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Service
public class BahmniVisitServiceImpl implements BahmniVisitService {

    private final VisitDao visitDao;

    @Autowired
    public BahmniVisitServiceImpl(VisitDao visitDao) {
        this.visitDao = visitDao;
    }

    @Override
    public Visit getLatestVisit(String patientUuid, String conceptName) {
        return visitDao.getLatestVisit(patientUuid, conceptName);
    }

    @Override
    public Visit getVisitSummary(String visitUuid) {
        return visitDao.getVisitSummary(visitUuid);
    }

    @Override
    public List<Encounter> getAdmitAndDischargeEncounters(Integer visitId) {
        return visitDao.getAdmitAndDischargeEncounters(visitId);
    }

    @Override
    public Boolean alreadyExistingVisit(String patientUuid, String locationUuid) {
        return !visitDao.commonVisit(patientUuid, locationUuid).isEmpty();
    }
    @Override
    public Visit saveVisitByPatient(String patientUuid, String locationUuid, String visitTypeUuid) {
        Visit visit = new Visit();
        visit.setPatient(Context.getPatientService().getPatientByUuid(patientUuid));
        visit.setLocation(Context.getLocationService().getLocationByUuid(locationUuid));
        visit.setVisitType(Context.getVisitService().getVisitTypeByUuid(visitTypeUuid));
        Date d= new Timestamp(System.currentTimeMillis());
        visit.setStartDatetime(d);
        Context.getVisitService().saveVisit(visit);
        return visit;
    }

}
