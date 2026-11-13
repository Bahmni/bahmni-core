package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.model.ConceptMetaData;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ConceptSetMapperTest {

    private ConceptSetMapper conceptSetMapper;

    @Mock
    private ConceptMetaData conceptMetaData;

    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        conceptSetMapper = new ConceptSetMapper();
        Locale defaultLocale = new Locale("en", "GB");
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getLocale).thenReturn(defaultLocale);
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void mapConceptSetNameToOpenmrsConceptname() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setUniqueName("Some");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>() , conceptMetaData);
        assertEquals("Some", mappedConcept.getFullySpecifiedName(Context.getLocale()).getName());
    }

    @Test
    public void mapShortName() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDisplayName("ShortName");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals("ShortName", mappedConcept.getShortestName(Context.getLocale(), false).getName());
    }

    @Test
    public void mapDescription() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDescription("Description");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals("Description", mappedConcept.getDescription(Context.getLocale()).getDescription());
    }

    @Test
    public void mapConceptClass() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setClassName("ClassName");
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName("ClassName");

        when(conceptMetaData.getConceptClass()).thenReturn(conceptClass);
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(),conceptMetaData);
        assertEquals("ClassName", mappedConcept.getConceptClass().getName());
    }

    @Test
    public void mapSetMembers() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        List<String> children = new ArrayList<>();
        children.add("1");
        children.add("2");
        conceptSet.setChildren(children);
        Concept child1 = new ConceptBuilder().withName("1").build();
        Concept child2 = new ConceptBuilder().withName("2").build();
        ArrayList<Concept> childConcepts = new ArrayList<>();
        childConcepts.add(child1);
        childConcepts.add(child2);
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, childConcepts,  conceptMetaData);
        List<org.openmrs.Concept> setMembers = mappedConcept.getSetMembers();
        assertEquals(2, setMembers.size());
        assertEquals("1", setMembers.get(0).getName(Context.getLocale()).getName());
        assertEquals("2", setMembers.get(1).getName(Context.getLocale()).getName());
    }

    @Test
    public void dontMapShortNameIfDoesNotExist() throws Exception {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setDisplayName(null);
        conceptSet.setUniqueName("uniqueName");
        conceptSet.setClassName("conceptClass");
        org.openmrs.Concept mappedConcept = conceptSetMapper.map(conceptSet, new ArrayList<Concept>(), conceptMetaData);
        assertEquals(0, mappedConcept.getShortNames().size());
    }
}
