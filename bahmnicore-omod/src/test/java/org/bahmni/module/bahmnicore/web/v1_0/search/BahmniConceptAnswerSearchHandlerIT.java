package org.bahmni.module.bahmnicore.web.v1_0.search;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;

public class BahmniConceptAnswerSearchHandlerIT extends BahmniMainResourceControllerTest {
    @Override
    public String getURI() {
        return "bahmniconceptanswer";
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public long getAllCount() {
        return 0;
    }

    @Before
    public void setup() throws Exception {
        executeDataSet("search/conceptAnswerSearch/testData.xml");
    }

    @Test
    public void shouldSearchAllConceptAnswersOfQuery() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "SIN");
        req.addParameter("s", "byQuestion");
        req.addParameter("question", "CIVIL STATUS");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List bahmniConceptAnswers =  result.get("results");
        Assert.assertEquals(1, bahmniConceptAnswers.size());

        HashMap bahmniConceptAnswer = (HashMap) bahmniConceptAnswers.get(0);
        HashMap concept = (HashMap) bahmniConceptAnswer.get("concept");
        String conceptUuid = (String) concept.get("uuid");
        Assert.assertEquals("32d3611a-6699-4d52-823f-b4b788bac3e3",conceptUuid);

        HashMap drug = (HashMap) bahmniConceptAnswer.get("drug");
        Assert.assertNull(drug);
    }

    @Test
    public void shouldPerformACaseInsensitiveSearch() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "sIn");
        req.addParameter("s", "byQuestion");
        req.addParameter("question", "CIVIL STATUS");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List bahmniConceptAnswers =  result.get("results");
        Assert.assertEquals(1, bahmniConceptAnswers.size());

        HashMap bahmniConceptAnswer = (HashMap) bahmniConceptAnswers.get(0);
        HashMap concept = (HashMap) bahmniConceptAnswer.get("concept");
        String conceptUuid = (String) concept.get("uuid");
        Assert.assertEquals("32d3611a-6699-4d52-823f-b4b788bac3e3",conceptUuid);

        HashMap drug = (HashMap) bahmniConceptAnswer.get("drug");
        Assert.assertNull(drug);
    }

    @Test
    public void shouldSearchConceptDrugAsWell() throws Exception{
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "Asperen");
        req.addParameter("s", "byQuestion");
        req.addParameter("question", "Diagnosis");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List bahmniConceptAnswers = result.get("results");
        HashMap bahmniConceptAnswer = (HashMap) bahmniConceptAnswers.get(0);
        HashMap drug = (HashMap) bahmniConceptAnswer.get("drug");
        String drugName = (String) drug.get("display");
        Assert.assertEquals("Asperen 79 mg",drugName);

        HashMap concept = (HashMap) bahmniConceptAnswer.get("concept");
        Assert.assertNull(concept);
    }
}
