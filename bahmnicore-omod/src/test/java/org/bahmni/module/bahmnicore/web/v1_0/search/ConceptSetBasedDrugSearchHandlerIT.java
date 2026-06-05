package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class ConceptSetBasedDrugSearchHandlerIT extends BahmniMainResourceControllerTest {

    @Override
    public String getURI() {
        return "drug";
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
    public void setUp() throws Exception {
        executeDataSet("search/conceptSetBasedDrug/drugsWithConcepts.xml");
    }

    @Test
    public void shouldReturnDrugsThatAreChildrenOfGivenConceptName() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "All TB Drugs");
        req.addParameter("s", "byConceptSet");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(3, hits.size());
    }

    @Test(expected = ConceptNotFoundException.class)
    public void shouldThrowExceptionWhenConceptSetNotProvided() throws Exception {
        MockHttpServletRequest requestWithoutAConceptSetName = request(RequestMethod.GET, getURI());
        requestWithoutAConceptSetName.addParameter("s", "byConceptSet");
        requestWithoutAConceptSetName.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);
        handle(requestWithoutAConceptSetName);
    }

    @Test
    public void shouldSearchForDrugBySearchTermAndConceptSet() throws Exception {
        MockHttpServletRequest request = request(RequestMethod.GET, getURI());
        request.addParameter("s", "byConceptSet");
        request.addParameter("searchTerm", "aceta");
        request.addParameter("q", "All TB Drugs");
        request.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);
        SimpleObject result = deserialize(handle(request));
        List<Drug> results = (List<Drug>) result.get("results");
        Assert.assertEquals(2, results.size());
        assertTrue(results.toString().contains("name=Thioacetazone"));
        assertTrue(results.toString().contains("name=Paracetamol High Dose"));
    }
}
