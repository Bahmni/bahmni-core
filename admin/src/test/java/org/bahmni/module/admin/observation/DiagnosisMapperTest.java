package org.bahmni.module.admin.observation;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DiagnosisMapperTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() {
        initMocks(this);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }
    @Test
    public void ignoreEmptyDiagnosis() throws ParseException {
        List<KeyValue> diagnosesKeyValues = Arrays.asList(new KeyValue("diagnosis", " "));

        ConceptService mockConceptService = mock(ConceptService.class);
        DiagnosisMapper diagnosisMapper = new DiagnosisMapper(mockConceptService);

        EncounterRow encounterRow = new EncounterRow();
        encounterRow.encounterDateTime = "2012-01-01";
        encounterRow.diagnosesRows = diagnosesKeyValues;
        List<BahmniDiagnosisRequest> bahmniDiagnosis = diagnosisMapper.getBahmniDiagnosis(encounterRow);

        Assert.isTrue(bahmniDiagnosis.isEmpty(), "Should ignore empty diagnoses");
    }

    @Test
    public void diagnosisWithUnknownConcepts() throws ParseException {
        List<KeyValue> diagnosesKeyValues = Arrays.asList(new KeyValue("diagnosis", "ABCD"));

        ConceptService mockConceptService = mock(ConceptService.class);
        when(mockConceptService.getConceptByName("diagnosis")).thenReturn(null);
        DiagnosisMapper diagnosisMapper = new DiagnosisMapper(mockConceptService);

        EncounterRow encounterRow = new EncounterRow();
        encounterRow.encounterDateTime = "2012-01-01";
        encounterRow.diagnosesRows = diagnosesKeyValues;
        List<BahmniDiagnosisRequest> bahmniDiagnosis = diagnosisMapper.getBahmniDiagnosis(encounterRow);

        assertEquals(bahmniDiagnosis.size(), 1);
        assertEquals(bahmniDiagnosis.get(0).getFreeTextAnswer(),"ABCD");
    }
}
