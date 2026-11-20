package org.bahmni.module.bahmnicore.customdatatype.datatype;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CodedConceptDatatypeTest {
    @Mock
    ConceptService conceptService;

    private CodedConceptDatatype codedConceptDatatype;
    private Concept concept;

    private MockedStatic<Context> contextMockedStatic;

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        concept = new Concept();
        concept.setId(1);

        ConceptAnswer answer = new ConceptAnswer();
        answer.setId(2);
        answer.setAnswerConcept(concept);

        concept.setAnswers(Collections.singletonList(answer));
        codedConceptDatatype = new CodedConceptDatatype();

        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getConceptService).thenReturn(conceptService);

        when(conceptService.getConcept(1)).thenReturn(concept);
        when(conceptService.getConceptByUuid("abcd")).thenReturn(concept);
    }

    @Test
    public void shouldGetCorrectConceptWhenSettingConfiguration() throws Exception {
        codedConceptDatatype.setConfiguration("1");

        verify(conceptService).getConcept(1);
    }

    @Test
    public void shouldReturnConceptIdWhenSerialized() throws Exception {
        String conceptId = codedConceptDatatype.serialize(concept);

        assertEquals("1", conceptId);
    }

    @Test
    public void shouldReturnConceptWhenDeserializedUsingConceptId() throws Exception {
        Concept deserializedConcept = codedConceptDatatype.deserialize("1");

        assertEquals(concept, deserializedConcept);
    }

    @Test
    public void shouldReturnConceptWhenDeserializedUsingConceptUuid() throws Exception {
        Concept deserializedConcept = codedConceptDatatype.deserialize("abcd");

        assertEquals(concept, deserializedConcept);
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenAConceptIsACorrectAnswer() throws Exception {
        codedConceptDatatype.setConfiguration("1");
        codedConceptDatatype.validate(concept);
    }

    @Test(expected = InvalidCustomValueException.class)
    public void shouldThrowExceptionWhenAConceptIsAnIncorrectAnswer() throws Exception {
        codedConceptDatatype.setConfiguration("1");
        Concept concept = new Concept();
        concept.setId(2);
        codedConceptDatatype.validate(concept);
    }
}