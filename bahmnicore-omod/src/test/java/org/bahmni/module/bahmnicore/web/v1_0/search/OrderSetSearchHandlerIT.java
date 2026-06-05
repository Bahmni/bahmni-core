package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.InvalidSearchException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

public class OrderSetSearchHandlerIT extends BahmniMainResourceControllerTest {

    @Override
    public String getURI() {
        return "bahmniorderset";
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
        executeDataSet("orderSet.xml");
    }

    @Test
    public void shouldRetrieveAllOrderSetsWhichContainsSearchPramsInNameOrDescription() throws Exception {
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "Order");
        req.addParameter("s", "byQuery");

        SimpleObject result = deserialize(handle(req));
        List orderSets = result.get("results");
        Map<String,Object> firstOrderSet = (Map<String, Object>) orderSets.get(0);
        Assert.assertEquals(3, orderSets.size());
        Assert.assertEquals("Order_Set_1", firstOrderSet.get("display"));
    }

    @Test(expected = InvalidSearchException.class)
    public void shouldNotRetriveOrderSetsIfSeachStringIsNotProper() throws Exception{
        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        req.addParameter("q", "Order");
        req.addParameter("s", "byQsuery");
        deserialize(handle(req));
    }
}