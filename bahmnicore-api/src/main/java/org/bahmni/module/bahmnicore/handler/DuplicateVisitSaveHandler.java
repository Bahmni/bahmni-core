package org.bahmni.module.bahmnicore.handler;

import org.bahmni.module.bahmnicore.exception.DuplicateVisitException;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.api.VisitService;
import org.openmrs.api.handler.SaveHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Handler(supports = {Visit.class})
public class DuplicateVisitSaveHandler implements SaveHandler<Visit> {

    private static final long DUPLICATE_VISIT_WINDOW_MILLIS = 10000;

    @Autowired
    private VisitService visitService;

    @Override
    public void handle(Visit visit, User currentUser, Date currentDate, String reason) {
        if (visit.getVisitId() != null || visit.getPatient() == null || visit.getVisitType() == null) {
            return;
        }

        List<Visit> activeVisits = visitService.getActiveVisitsByPatient(visit.getPatient());
        long now = new Date().getTime();
        for (Visit activeVisit : activeVisits) {
            if (isRecentDuplicate(visit, activeVisit, now)) {
                throw new DuplicateVisitException("A visit has already just been started for this patient at this location.");
            }
        }
    }

    private boolean isRecentDuplicate(Visit newVisit, Visit existingVisit, long now) {
        boolean sameVisitType = existingVisit.getVisitType() != null && existingVisit.getVisitType().equals(newVisit.getVisitType());
        boolean sameLocation = existingVisit.getLocation() != null && existingVisit.getLocation().equals(newVisit.getLocation());
        boolean recentlyCreated = existingVisit.getDateCreated() != null
                && (now - existingVisit.getDateCreated().getTime()) < DUPLICATE_VISIT_WINDOW_MILLIS;
        return sameVisitType && sameLocation && recentlyCreated;
    }
}
