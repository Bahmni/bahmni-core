package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bacteriology.api.BacteriologyService;
import org.openmrs.module.bacteriology.api.encounter.domain.Specimen;
import org.openmrs.module.bacteriology.api.encounter.domain.Specimens;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BacteriologySpecimenSearchHandlerTest {

    @Mock private ConceptService conceptService;
    @Mock private BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock private ObsService obsService;
    @Mock private RequestContext requestContext;
    @Mock private BacteriologyService bacteriologyService;
    @Mock private UserContext userContext;

    private BacteriologySpecimenSearchHandler bacteriologySpecimenSearchHandler;
    private final String BACTERIOLOGY_CONCEPT_SET = "BACTERIOLOGY CONCEPT SET";

    @Before
    public void before() {
        initMocks(this);
        when(requestContext.getLimit()).thenReturn(5);
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(BacteriologyService.class)).thenReturn(bacteriologyService);

        bacteriologySpecimenSearchHandler = new BacteriologySpecimenSearchHandler(bahmniProgramWorkflowService,
            conceptService, obsService);
    }

    @Test
    public void shouldSearchByPatientProgramUuid() {
        Concept bacteriologyConceptSet = new Concept();
        Encounter encounter = new Encounter();
        Obs observation = new Obs();
        Specimen specimen = new Specimen();
        Specimens specimens = new Specimens(Arrays.asList(specimen));

        List<Encounter> encounters = Arrays.asList(encounter);
        List<Concept> concepts = Arrays.asList(bacteriologyConceptSet);

        when(requestContext.getParameter("patientProgramUuid")).thenReturn("sample-patientProgramUuid");
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid("sample-patientProgramUuid")).thenReturn(encounters);
        when(conceptService.getConceptByName(BACTERIOLOGY_CONCEPT_SET)).thenReturn(bacteriologyConceptSet);
        List<Obs> observations = Arrays.asList(observation);
        when(obsService.getObservations(null, encounters, concepts, null, null, null, null, null, null, null, null, false))
                .thenReturn(observations);
        when(bacteriologyService.getSpecimens(observations)).thenReturn(specimens);

        NeedsPaging<Specimen> pageableResult = (NeedsPaging)bacteriologySpecimenSearchHandler.search(requestContext);
        Specimens resultSpecimens = new Specimens(pageableResult.getPageOfResults());
        assertEquals(1, resultSpecimens.size());
        assertEquals(specimens, resultSpecimens);
    }
}
