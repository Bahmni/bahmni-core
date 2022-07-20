package org.openmrs.module.bahmniemrapi.laborder.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.emrapi.test.builder.ConceptDataTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LabOrderResultMapperTest {
    private LabOrderResultMapper labOrderResultMapper;
    @Mock
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        ConceptDatatype coded = new ConceptDataTypeBuilder().text();
        Concept labReportConcept = new Concept();
        labReportConcept.setDatatype(coded);
        when(conceptService.getConceptByName(LabOrderResultMapper.LAB_REPORT)).thenReturn(labReportConcept);
        labOrderResultMapper = new LabOrderResultMapper(conceptService);
    }


    @Test
    public void shouldMapTestOrderAndLabOrderResultToObs() throws Exception {
        LabOrderResult labOrderResult = new LabOrderResult();
        String resultUuid = "abcd-1234";
        labOrderResult.setResultUuid(resultUuid);
        labOrderResult.setResult("A+ve");
        labOrderResult.setAccessionUuid("accession-uuid");
        labOrderResult.setTestName("Blood Group");

        ConceptDatatype codedDataType = new ConceptDataTypeBuilder().coded();
        Concept testConcept = new Concept(1);
        testConcept.setDatatype(codedDataType);
        Concept resultConcept = new Concept(2);
        resultConcept.setDatatype(codedDataType);

        Order testOrder = new Order(1);
        when(conceptService.getConceptByUuid(resultUuid)).thenReturn(resultConcept);

        Obs topLevelObs = labOrderResultMapper.map(labOrderResult, testOrder, testConcept);

        assertEquals(testConcept, topLevelObs.getConcept());
        assertEquals(testOrder, topLevelObs.getOrder());
        List<Obs> testObs = new ArrayList<>(topLevelObs.getGroupMembers());
        List<Obs> resultObs = new ArrayList<>(testObs.get(0).getGroupMembers());
        assertEquals(1, resultObs.size());
        assertEquals(testConcept.getId(), resultObs.get(0).getConcept().getId());
        assertEquals(testOrder, resultObs.get(0).getOrder());
        assertEquals(resultConcept, resultObs.get(0).getValueCoded());
        assertNull(resultObs.get(0).getValueText());
    }

    @Test
    public void shouldNotAddObsIfResultIsNotAnswerForCodedConcept() throws Exception {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResultUuid(null);
        labOrderResult.setResult("A+ve");
        labOrderResult.setAccessionUuid("accession-uuid");
        labOrderResult.setTestName("Blood Group");

        ConceptDatatype coded = new ConceptDataTypeBuilder().coded();
        Concept testConcept = new Concept(1);
        testConcept.setDatatype(coded);
        Order testOrder = new Order(1);

        Obs topLevelObs = labOrderResultMapper.map(labOrderResult, testOrder, testConcept);

        List<Obs> testObs = new ArrayList<>(topLevelObs.getGroupMembers());
        Set<Obs> resultObs = testObs.get(0).getGroupMembers();
        assertEquals(1, testObs.size());
        assertTrue(CollectionUtils.isEmpty(resultObs));

    }

    @Test
    public void shouldMapTestOrderAndLabOrderResultToObsForNumericConcepts() throws Exception {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResultUuid(null);
        labOrderResult.setResult("15");
        labOrderResult.setAccessionUuid("accession-uuid");
        labOrderResult.setTestName("Haemoglobin");

        ConceptDatatype coded = new ConceptDataTypeBuilder().numeric();
        Concept testConcept = new Concept(1);
        testConcept.setDatatype(coded);
        Order testOrder = new Order(1);

        Obs topLevelObs = labOrderResultMapper.map(labOrderResult, testOrder, testConcept);

        assertEquals(testConcept, topLevelObs.getConcept());
        assertEquals(testOrder, topLevelObs.getOrder());
        List<Obs> testObs = new ArrayList<>(topLevelObs.getGroupMembers());
        List<Obs> resultObs = new ArrayList<>(testObs.get(0).getGroupMembers());
        assertEquals(1, resultObs.size());
        assertEquals(testConcept, resultObs.get(0).getConcept());
        assertEquals(testOrder, resultObs.get(0).getOrder());
        assertEquals(new Double(15), resultObs.get(0).getValueNumeric());
    }

    @Test
    public void shouldNotAddEmptyObsWhenResultIsNotPresent() throws Exception {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResultUuid(null);
        labOrderResult.setResult(null);
        labOrderResult.setAccessionUuid("accession-uuid");
        labOrderResult.setTestName("Haemoglobin");

        ConceptDatatype coded = new ConceptDataTypeBuilder().numeric();
        Concept testConcept = new Concept(1);
        testConcept.setDatatype(coded);
        Order testOrder = new Order(1);

        Obs topLevelObs = labOrderResultMapper.map(labOrderResult, testOrder, testConcept);

        assertEquals(testConcept, topLevelObs.getConcept());
        assertEquals(testOrder, topLevelObs.getOrder());
        List<Obs> testObs = new ArrayList<>(topLevelObs.getGroupMembers());
        Set<Obs> resultObs = testObs.get(0).getGroupMembers();
        assertEquals(1, testObs.size());
        assertTrue(CollectionUtils.isEmpty(resultObs));

    }

    @Test
    public void shouldMapFileNameEvenEvenWhenResultIsNotPresent() throws Exception {
        String uploadedFileName = "ResultsDoc";
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setResultUuid(null);
        labOrderResult.setResult(null);
        labOrderResult.setUploadedFileName(uploadedFileName);
        labOrderResult.setAccessionUuid("accession-uuid");
        labOrderResult.setTestName("Haemoglobin");

        ConceptDatatype coded = new ConceptDataTypeBuilder().numeric();
        Concept testConcept = new Concept(1);
        testConcept.setDatatype(coded);
        Order testOrder = new Order(1);

        Obs topLevelObs = labOrderResultMapper.map(labOrderResult, testOrder, testConcept);

        assertEquals(testConcept, topLevelObs.getConcept());
        assertEquals(testOrder, topLevelObs.getOrder());
        List<Obs> testObs = new ArrayList<>(topLevelObs.getGroupMembers());
        List<Obs> resultObs = new ArrayList<>(testObs.get(0).getGroupMembers());

        assertEquals(1, testObs.size());
        assertEquals(1, resultObs.size());
        assertEquals(uploadedFileName, resultObs.get(0).getValueText());
    }

}
