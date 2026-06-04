package org.bahmni.module.bahmnicore.customdatatype.datatype;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class CodedConceptDatatypeTest {
    @Mock
    ConceptService conceptService;

    private CodedConceptDatatype codedConceptDatatype;
    private Concept concept;

    @Before
    public void setUp() {
        concept = new Concept();
        concept.setId(1);

        ConceptAnswer answer = new ConceptAnswer();
        answer.setId(2);
        answer.setAnswerConcept(concept);

        concept.setAnswers(Collections.singletonList(answer));
        codedConceptDatatype = new CodedConceptDatatype();

        PowerMockito.mockStatic(Context.class);
        BDDMockito.when(Context.getConceptService()).thenReturn(conceptService);

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