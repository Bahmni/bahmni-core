package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class VisitFormsSearchHandlerIT  extends BahmniMainResourceControllerTest {
    private static final String VISIT_FORM_DATA_SET_XML = "visitFormDataSet.xml";

    @Before
    public void init() throws Exception {
        executeDataSet(VISIT_FORM_DATA_SET_XML);
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
     */
    @Override
    public String getURI() {
        return "obs";
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
     */
    @Override
    public long getAllCount() {
        return 0l;
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
     */
    @Override
    public String getUuid() {
        return null;
    }

    /**
     * @verifies return location by tag uuid
     */
    @Test
    public void shouldRetrieveObservationsForConcept() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("patient", "a76e8d23-0c38-408c-b2a8-ea5540f01b51");
        req.addParameter("numberOfVisits", "10");
        req.addParameter("s", "byPatientUuid");
        req.addParameter("conceptNames","HIV");

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(2, hits.size());
    }

    /**
     * @verifies return location by tag uuid
     */
    @Test
    public void shouldRetrieveObservationsForAllConcepts() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("patient", "a76e8d23-0c38-408c-b2a8-ea5540f01b51");
        req.addParameter("numberOfVisits", "10");
        req.addParameter("s", "byPatientUuid");

        SimpleObject result = deserialize(handle(req));
        List<Object> hits = (List<Object>) result.get("results");
        Assert.assertEquals(4, hits.size());
    }
}
