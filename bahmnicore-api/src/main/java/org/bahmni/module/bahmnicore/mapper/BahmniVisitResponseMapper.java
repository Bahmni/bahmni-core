package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.contract.visit.BahmniVisitResponse;
import org.openmrs.Encounter;
import org.openmrs.Visit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class BahmniVisitResponseMapper {

    public BahmniVisitResponse map(Visit visit) {
        BahmniVisitResponse bahmniVisitResponse =new BahmniVisitResponse();
        bahmniVisitResponse.setUuid(visit.getUuid());
        bahmniVisitResponse.setVisitTypeUuid(visit.getVisitType().getUuid());
        bahmniVisitResponse.setPatientUuid(visit.getPatient().getUuid());
        bahmniVisitResponse.setLocationUuid(visit.getLocation().getUuid());
        DateFormat f= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        bahmniVisitResponse.setStartDatetime(f.format(visit.getStartDatetime()));
        bahmniVisitResponse.setStopDatetime(visit.getStopDatetime()!=null?f.format(visit.getStopDatetime()):null);
        return bahmniVisitResponse;
    }
}
