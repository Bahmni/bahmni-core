package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Component
public class VisitIdentificationHelper implements VisitMatcher {
    protected VisitService visitService;

    private BahmniVisitLocationService bahmniVisitLocationService;

    @Autowired
    public VisitIdentificationHelper(VisitService visitService, BahmniVisitLocationService bahmniVisitLocationService) {
        this.visitService = visitService;
        this.bahmniVisitLocationService = bahmniVisitLocationService;
    }

    public Visit getVisitFor(Patient patient, String visitTypeForNewVisit, Date orderDate, Date visitStartDate, Date visitEndDate, String locationUuid) {
        bahmniVisitLocationService = bahmniVisitLocationService != null ? bahmniVisitLocationService : Context.getService(BahmniVisitLocationService.class);
        String visitLocationUuid = bahmniVisitLocationService.getVisitLocationUuid(locationUuid);
        Date nextDate = getEndOfTheDay(orderDate);
        List<Visit> visits = visitService.getVisits(null, Collections.singletonList(patient), null, null, null, nextDate, orderDate, null, null, true, false);
        List<Visit> matchingVisits = getMatchingVisitsFromLocation(visits, visitLocationUuid);

        if (!matchingVisits.isEmpty()) {
            Visit matchingVisit = getVisitMatchingOrderDate(orderDate, matchingVisits);
            return stretchVisits(orderDate, matchingVisit);
        }
        return createNewVisit(patient, orderDate, visitTypeForNewVisit, visitStartDate, visitEndDate, visitLocationUuid);
    }

    public Visit getVisitFor(Patient patient, String visitTypeForNewVisit, Date orderDate, String locationUuid) {
        return getVisitFor(patient, visitTypeForNewVisit, orderDate, null, null, locationUuid);
    }

    public boolean hasActiveVisit(Patient patient) {
        return CollectionUtils.isNotEmpty(visitService.getActiveVisitsByPatient(patient));
    }

    private List<Visit> getMatchingVisitsFromLocation(List<Visit> visits, String locationUuid) {
        List<Visit> matchingVisits = new ArrayList<>();
        for (Visit visit : visits) {
            Location location = visit.getLocation();
            if (location != null && locationUuid != null && location.getUuid().equals(locationUuid)) {
                matchingVisits.add(visit);
            } else if (location == null && locationUuid != null) {
                Location visitLocation = Context.getLocationService().getLocationByUuid(locationUuid);
                visit.setLocation(visitLocation);
                matchingVisits.add(visit);
            }
        }
        return matchingVisits;
    }

    private Visit stretchVisits(Date orderDate, Visit matchingVisit) {
        if (matchingVisit.getStartDatetime().after(orderDate)) {
            matchingVisit.setStartDatetime(orderDate);
        }
        if (matchingVisit.getStopDatetime() != null && matchingVisit.getStopDatetime().before(orderDate)) {
            matchingVisit.setStopDatetime(orderDate);
        }
        return matchingVisit;
    }

    private Visit getVisitMatchingOrderDate(Date orderDate, List<Visit> visits) {
        for (Visit visit : visits) {
            Date visitStartDatetime = visit.getStartDatetime();
            Date visitStopDatetime = visit.getStopDatetime();
            if (visitStopDatetime != null) {
                if ((orderDate.equals(visitStartDatetime) || visitStartDatetime.before(orderDate)) &&
                        (orderDate.equals(visitStopDatetime) || visitStopDatetime.after(orderDate)))
                    return visit;
            } else {
                if (orderDate.equals(visitStartDatetime) || visitStartDatetime.before(orderDate))
                    return visit;
            }
        }
        return visits.get(visits.size() - 1);
    }

    public Visit createNewVisit(Patient patient, Date date, String visitTypeForNewVisit, Date visitStartDate, Date visitEndDate, String visitLocationUuid) {

        Location location = Context.getLocationService().getLocationByUuid(visitLocationUuid);
        VisitType visitTypeByName = getVisitTypeByName(visitTypeForNewVisit);
        if (visitTypeByName == null) {
            throw new RuntimeException("Visit type:'" + visitTypeForNewVisit + "' not found.");
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitTypeByName);
        visit.setLocation(location);
        visit.setStartDatetime(visitStartDate == null ? date : visitStartDate);
        if (!DateUtils.isSameDay(date, new Date())) {
            if (visitEndDate == null)
                visit.setStopDatetime(new DateTime(date).toDateMidnight().toDateTime().plusDays(1).minusSeconds(1).toDate());
            else
                visit.setStopDatetime(new DateTime(visitEndDate).toDateMidnight().toDateTime().plusDays(1).minusSeconds(1).toDate());
        }
        visit.setEncounters(new HashSet<Encounter>());
        return visitService.saveVisit(visit);
    }

    public VisitType getVisitTypeByName(String visitTypeName) {
        List<VisitType> visitTypes = visitService.getVisitTypes(visitTypeName);
        return visitTypes.isEmpty() ? null : visitTypes.get(0);
    }

    private static Date getEndOfTheDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


}