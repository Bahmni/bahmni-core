package org.bahmni.module.admin.observation.handler;


import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class Form1CSVObsHandlerTest {

    private Form1CSVObsHandler form1CSVObsHandler;

    private CSVObservationHelper csvObservationHelper;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    @Before
    public void setUp() {
        initMocks(this);
        csvObservationHelper = mock(CSVObservationHelper.class);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");
    }

    @Test
    public void shouldFilterForm1CSVObs() {
        final KeyValue form1CSVObservation = new KeyValue("Vitals.Height", "100");
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals.Height", "100");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form1CSVObservation, form2CSVObservation);

        when(csvObservationHelper.isForm1Type(form1CSVObservation)).thenReturn(true);
        when(csvObservationHelper.isForm1Type(form2CSVObservation)).thenReturn(false);

        form1CSVObsHandler = new Form1CSVObsHandler(csvObservationHelper);

        final List<KeyValue> form1CSVObs = form1CSVObsHandler.getRelatedCSVObs(encounterRow);

        assertEquals(1, form1CSVObs.size());
        assertEquals(form1CSVObservation, form1CSVObs.get(0));
        verify(csvObservationHelper).isForm1Type(form1CSVObservation);
        verify(csvObservationHelper).isForm1Type(form2CSVObservation);
    }

    @Test
    public void shouldVerifyCreateObservationsIsCalledWhileHandlingObservations() throws ParseException {
        final KeyValue form1CSVObservation = new KeyValue("Vitals.Height", "100");
        final KeyValue form2CSVObservation = new KeyValue("form2.Vitals.Height", "100");

        final EncounterRow encounterRow = new EncounterRow();
        encounterRow.obsRows = asList(form1CSVObservation, form2CSVObservation);
        encounterRow.encounterDateTime = "2019-11-11";

        when(csvObservationHelper.isForm1Type(form1CSVObservation)).thenReturn(true);
        when(csvObservationHelper.isForm1Type(form2CSVObservation)).thenReturn(false);

        final List<String> conceptNames = asList("Vitals", "Height");
        when(csvObservationHelper.getCSVHeaderParts(form1CSVObservation)).thenReturn(conceptNames);
        doNothing().when(csvObservationHelper).verifyNumericConceptValue(form1CSVObservation, conceptNames);
        doNothing().when(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), any(KeyValue.class), anyListOf(String.class));

        form1CSVObsHandler = new Form1CSVObsHandler(csvObservationHelper);

        form1CSVObsHandler.handle(encounterRow);

        verify(csvObservationHelper).isForm1Type(form1CSVObservation);
        verify(csvObservationHelper).isForm1Type(form2CSVObservation);
        verify(csvObservationHelper).getCSVHeaderParts(form1CSVObservation);
        verify(csvObservationHelper).verifyNumericConceptValue(form1CSVObservation, conceptNames);
        verify(csvObservationHelper).createObservations(anyListOf(EncounterTransaction.Observation.class),
                any(Date.class), any(KeyValue.class), anyListOf(String.class));

    }

}