package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class BahmniConceptSearchByDataTypeHandlerIT extends BahmniMainResourceControllerTest {
    @Override
    public String getURI() {
        return "concept";
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
        executeDataSet("search/conceptDataWithDatatypes.xml");
        updateSearchIndex();
    }

    public void updateSearchIndex() {
        for (Class<?> indexType : getIndexedTypes()) {
            Context.updateSearchIndexForType(indexType);
        }
    }

    @Test
    public void shouldSearchConceptsWithRequiredDataTypes() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("name", "Tem");
        req.addParameter("s", "byDataType");
        req.addParameter("dataTypes", "Boolean, Numeric, Text");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List concepts =  result.get("results");

        Assert.assertEquals(4, concepts.size());

        List<String> expectedConceptDisplayNames = Arrays.asList("Temperature-2", "Temperature notes", "Temperature Abnormal", "Temperature");

        Iterator<Map<String,String>> iterator = concepts.iterator();

        while(iterator.hasNext()){
            String actualConceptDisplayName = iterator.next().get("display").toString();
            assertTrue(expectedConceptDisplayNames.contains(actualConceptDisplayName));
        }
    }

    @Test
    public void shouldReturnAllConceptsWhenDatatypesAreNotSpecified() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("name", "Tem");
        req.addParameter("s", "byDataType");
        req.addParameter("dataTypes", "");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List concepts =  result.get("results");

        Assert.assertEquals(6, concepts.size());
    }

    @Test
    public void shouldSearchConceptsWithOneRequiredDataTypes() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("name", "Tem");
        req.addParameter("s", "byDataType");
        req.addParameter("dataTypes", "Boolean");
        req.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject result = deserialize(handle(req));
        List concepts =  result.get("results");

        Assert.assertEquals(1, concepts.size());

        HashMap bahmniConcept = (HashMap) concepts.get(0);
        Assert.assertEquals("abnormal_concept_uuid", bahmniConcept.get("uuid"));
        Assert.assertEquals("Temperature Abnormal", ((HashMap)bahmniConcept.get("name")).get("name"));
    }
}
