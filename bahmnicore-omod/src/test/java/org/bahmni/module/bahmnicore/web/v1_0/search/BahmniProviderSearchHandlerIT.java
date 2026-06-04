package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public class BahmniProviderSearchHandlerIT extends  BahmniMainResourceControllerTest {
    @Override
    public String getURI() {
        return "provider";
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
        executeDataSet("providerDataSet.xml");
    }

    @Test
    public void searchByOrgAttribute() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "");
        req.addParameter("s", BahmniProviderSearchHandler.SEARCH_BY_ATTRIBUTE);
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_NAME, "organization");
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_VALUE, "JSS");
        SimpleObject result = deserialize(handle(req));
        List providers = result.get("results");
        Assert.assertEquals(1, providers.size());
        Assert.assertTrue(providers.stream().anyMatch(provider -> "a3a5913e-6b94-11e0-93c3-18a905e044dc".equals(getProviderProperty(provider, "uuid"))));
    }

    @Test
    public void searchByApptAvailabilityAttribute() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "");
        req.addParameter("s", BahmniProviderSearchHandler.SEARCH_BY_ATTRIBUTE);
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_NAME, "Available for appointments");
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_VALUE, "true");
        SimpleObject result = deserialize(handle(req));
        List providers = result.get("results");
        Assert.assertEquals(2, providers.size());
        Assert.assertTrue(providers.stream().anyMatch(provider -> "a3a5913e-6b94-11e0-93c3-18a905e044dc".equals(getProviderProperty(provider, "uuid"))));
        Assert.assertTrue(providers.stream().anyMatch(provider -> "161b3002-6b95-11e0-93c3-18a905e044dc".equals(getProviderProperty(provider, "uuid"))));
    }

    @Test
    public void searchByLocationAttribute() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "");
        req.addParameter("s", BahmniProviderSearchHandler.SEARCH_BY_ATTRIBUTE);
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_NAME, "Department");
        req.addParameter(BahmniProviderSearchHandler.PARAM_ATTRIBUTE_VALUE, "c36006e5-9fbb-4f20-866b-0ece245615a1");
        SimpleObject result = deserialize(handle(req));
        List providers = result.get("results");
        Assert.assertEquals(1, providers.size());
        Assert.assertTrue(providers.stream().anyMatch(provider -> "ae401f88-6b94-11e0-93c3-18a905e044dc".equals(getProviderProperty(provider, "uuid"))));
    }

    private Object getProviderProperty(Object provider, String propertyName) {
        Map<String, Object> providerMap = (Map<String, Object>) provider;
        return providerMap.get(propertyName);
    }
}