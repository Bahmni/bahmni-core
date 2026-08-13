package org.bahmni.module.bahmnicore.handler;

import org.bahmni.module.bahmnicore.exception.DuplicateVisitException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

public class DuplicateVisitSaveHandlerTest {

    @InjectMocks
    private DuplicateVisitSaveHandler handler;

    @Mock
    private VisitService visitService;

    private Patient patient;
    private VisitType visitType;
    private Location location;

    @Before
    public void setUp() {
        initMocks(this);
        patient = new Patient();
        visitType = new VisitType();
        visitType.setId(1);
        location = new Location();
        location.setId(1);
    }

    private Visit newVisit() {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitType);
        visit.setLocation(location);
        return visit;
    }

    @Test
    public void shouldRejectWhenAnActiveVisitOfSameTypeAndLocationWasJustCreated() {
        Visit newVisit = newVisit();

        Visit existingVisit = newVisit();
        existingVisit.setId(1);
        existingVisit.setDateCreated(new Date());
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(existingVisit));

        try {
            handler.handle(newVisit, null, new Date(), null);
            fail("Expected a DuplicateVisitException to be thrown for the duplicate visit");
        } catch (DuplicateVisitException e) {
            assertTrue(e.getMessage().contains("already"));
        }
    }

    @Test
    public void shouldNotRejectWhenNoActiveVisitsExist() {
        Visit newVisit = newVisit();
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(new ArrayList<Visit>());

        handler.handle(newVisit, null, new Date(), null);
    }

    @Test
    public void shouldNotRejectWhenExistingActiveVisitIsOfDifferentType() {
        Visit newVisit = newVisit();

        VisitType differentVisitType = new VisitType();
        differentVisitType.setId(2);

        Visit existingVisit = newVisit();
        existingVisit.setId(1);
        existingVisit.setVisitType(differentVisitType);
        existingVisit.setDateCreated(new Date());
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(existingVisit));

        handler.handle(newVisit, null, new Date(), null);
    }

    @Test
    public void shouldNotRejectWhenExistingActiveVisitIsAtDifferentLocation() {
        Visit newVisit = newVisit();

        Location differentLocation = new Location();
        differentLocation.setId(2);

        Visit existingVisit = newVisit();
        existingVisit.setId(1);
        existingVisit.setLocation(differentLocation);
        existingVisit.setDateCreated(new Date());
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(existingVisit));

        handler.handle(newVisit, null, new Date(), null);
    }

    @Test
    public void shouldNotRejectWhenExistingActiveVisitWasCreatedLongAgo() {
        Visit newVisit = newVisit();

        Visit existingVisit = newVisit();
        existingVisit.setId(1);
        existingVisit.setDateCreated(new Date(System.currentTimeMillis() - 60000));
        Mockito.when(visitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(existingVisit));

        handler.handle(newVisit, null, new Date(), null);
    }

    @Test
    public void shouldNotHandleWhenVisitIsAlreadyPersisted() {
        Visit existingVisit = newVisit();
        existingVisit.setId(1);

        handler.handle(existingVisit, null, new Date(), null);

        Mockito.verifyNoInteractions(visitService);
    }
}
