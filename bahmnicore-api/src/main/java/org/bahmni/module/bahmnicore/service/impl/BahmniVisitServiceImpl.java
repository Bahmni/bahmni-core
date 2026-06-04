package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.VisitDao;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BahmniVisitServiceImpl implements BahmniVisitService {

    private VisitDao visitDao;

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
}
