package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.BaseIntegrationTest;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConceptControllerIT extends BaseIntegrationTest {
    
    @Autowired
    private ConceptService conceptService;

    @Test
    public void shouldCreateConcept() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        org.bahmni.module.referencedata.labconcepts.contract.Concept createdConcept = deserialize(response, org.bahmni.module.referencedata.labconcepts.contract.Concept.class);

        assertNotNull(createdConcept);

        Concept concept = conceptService.getConceptByUuid(createdConcept.getUuid());

        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(className, concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(dataType, concept.getDatatype().getName());
    }

    @Test
    public void shouldCreateCodedConceptWithAnswers() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "shortName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "Coded";
        String answerConceptName = "HIV PROGRAM";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"," +
                "\"answers\":" + "[\"" + answerConceptName + "\"]" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        org.bahmni.module.referencedata.labconcepts.contract.Concept createdConcept = deserialize(response, org.bahmni.module.referencedata.labconcepts.contract.Concept.class);

        assertNotNull(createdConcept);

        Concept concept = conceptService.getConceptByUuid(createdConcept.getUuid());

        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(className, concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(dataType, concept.getDatatype().getName());
        assertEquals(answerConceptName, concept.getAnswers().iterator().next().getAnswerConcept().getName(Context.getLocale()).getName());
    }

    @Test
    public void shouldCreateCodedConceptWithoutAnswers() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "shortName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "Coded";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        org.bahmni.module.referencedata.labconcepts.contract.Concept createdConcept = deserialize(response, org.bahmni.module.referencedata.labconcepts.contract.Concept.class);

        assertNotNull(createdConcept);

        Concept concept = conceptService.getConceptByUuid(createdConcept.getUuid());

        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(className, concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(dataType, concept.getDatatype().getName());
        assertEquals(0, concept.getAnswers().size());
    }

    @Test
    public void shouldMaintainTheSortOrderOfAnswers() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "shortName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "Coded";
        String answerConceptName1 = "HIV PROGRAM";
        String answerConceptName2 = "ASPIRIN";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"," +
                "\"answers\":" + "[\"" + answerConceptName1 + "\", \"" + answerConceptName2 + "\"]" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        org.bahmni.module.referencedata.labconcepts.contract.Concept createdConcept = deserialize(response, org.bahmni.module.referencedata.labconcepts.contract.Concept.class);

        assertNotNull(createdConcept);

        Concept concept = conceptService.getConceptByUuid(createdConcept.getUuid());

        assertEquals(uniqueName, concept.getFullySpecifiedName(Context.getLocale()).getName());
        assertEquals(displayName, concept.getShortestName(Context.getLocale(), false).getName());
        assertEquals(className, concept.getConceptClass().getName());
        assertEquals(description, concept.getDescription(Context.getLocale()).getDescription());
        assertEquals(dataType, concept.getDatatype().getName());
        Collection<ConceptAnswer> answers = concept.getAnswers();
        for (ConceptAnswer answer : answers) {
            if (answer.getAnswerConcept().getName(Context.getLocale()).getName().equals("HIV PROGRAM")) {
                assertTrue(answer.getSortWeight().equals(1.0));
            }
            if (answer.getAnswerConcept().getName(Context.getLocale()).getName().equals("ASPIRIN")) {
                assertTrue(answer.getSortWeight().equals(2.0));
            }
        }
    }

    @Test
    public void shouldNotCreateConceptIfTheConceptDataTypeIsNotCodedAndAnswerIsSpecified() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "shortName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "N/A";
        String answerConceptName = "HIV PROGRAM";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"," +
                "\"answers\":" + "[\"" + answerConceptName + "\"]" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void shouldCreateConceptWithoutShortName() throws Exception {
        String uniqueName = "uniqueName";
        String description = "Sample basic concept being created";
        String className = "Misc";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.CREATED.value());
    }

    @Test
    public void shouldNotCreateConceptForWrongDatatype() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Finding";
        String description = "Sample basic concept being created";
        String dataType = "NA";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForWrongConceptClass() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForEmptyConceptUniqueName() throws Exception {
        String uniqueName = "";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";
        String dataType = "N/A";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"," +
                "\"dataType\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }


    @Test
    public void shouldNotCreateConceptForEmptyConceptDataType() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String className = "Ramesh";
        String description = "Sample basic concept being created";

        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + className + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void shouldNotCreateConceptForEmptyConceptClass() throws Exception {
        String uniqueName = "uniqueName";
        String displayName = "uniqueName";
        String description = "Sample basic concept being created";
        String dataType = "N/A";


        String conceptDataJson = "{" +
                "\"uniqueName\":\"" + uniqueName + "\"," +
                "\"displayName\":\"" + displayName + "\"," +
                "\"description\":\"" + description + "\"," +
                "\"className\":\"" + dataType + "\"" +
                "}";

        MockHttpServletRequest request = newPostRequest("/rest/v1/reference-data/concept", conceptDataJson);
        MockHttpServletResponse response = handle(request);
        assertEquals(response.getStatus(), HttpStatus.BAD_REQUEST.value());
    }

}