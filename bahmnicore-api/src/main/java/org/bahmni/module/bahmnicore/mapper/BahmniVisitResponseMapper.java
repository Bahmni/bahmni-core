package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.contract.visit.BahmniVisitResponse;
import org.bahmni.module.bahmnicore.contract.visit.VisitTypeResponse;

import org.openmrs.Visit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class BahmniVisitResponseMapper {

    public BahmniVisitResponse map(Visit visit) {
        BahmniVisitResponse bahmniVisitResponse =new BahmniVisitResponse();
        bahmniVisitResponse.setUuid(visit.getUuid());
        VisitTypeResponse visitTypeResponse =new VisitTypeResponse();
        visitTypeResponse.setUuid(visit.getVisitType().getUuid());
        visitTypeResponse.setDisplay(visit.getVisitType().getName());
        bahmniVisitResponse.setVisitType(visitTypeResponse);
        bahmniVisitResponse.setPatientUuid(visit.getPatient().getUuid());
        bahmniVisitResponse.setLocationUuid(visit.getLocation().getUuid());
        DateFormat f= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        bahmniVisitResponse.setStartDatetime(f.format(visit.getStartDatetime()));
        bahmniVisitResponse.setStopDatetime(visit.getStopDatetime()!=null?f.format(visit.getStopDatetime()):null);
        return bahmniVisitResponse;
    }
}
