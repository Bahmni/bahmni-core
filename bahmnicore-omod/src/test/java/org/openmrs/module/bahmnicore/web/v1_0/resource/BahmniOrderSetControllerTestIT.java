package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.apache.commons.beanutils.PropertyUtils;
import org.bahmni.module.bahmnicore.web.v1_0.search.BahmniMainResourceControllerTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.OrderSet;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;

public class BahmniOrderSetControllerTestIT extends BahmniMainResourceControllerTest {

    private OrderSetService orderSetService;

    @Before
    public void init() throws Exception {
        orderSetService = Context.getOrderSetService();
        executeDataSet(RestConstants.TEST_DATA_SET);
    }

    /**
     * @see BahmniMainResourceControllerTest#getURI()
     */
    @Override
    public String getURI() {
        return "bahmniorderset";
    }

    /**
     * @see BahmniMainResourceControllerTest#getAllCount()
     */
    @Override
    public long getAllCount() {
        return orderSetService.getOrderSets(false).size();
    }

    @Override
    public String getUuid() {
        return RestConstants.ORDER_SET_UUID;
    }

    public String getName() {
        return "orderSet1";
    }

    @Test
    public void shouldListAllUnRetiredOrderSets() throws Exception {

        MockHttpServletRequest req = request(RequestMethod.GET, getURI());
        SimpleObject result = deserialize(handle(req));

        Assert.assertNotNull(result);
        Assert.assertEquals(getAllCount(), Util.getResultsSize(result));
    }

    @Test
    public void shouldGetAnOrderSetByUuid() throws Exception {

        MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + getUuid());
        SimpleObject result = deserialize(handle(req));

        OrderSet orderSet = orderSetService.getOrderSetByUuid(getUuid());

        Assert.assertNotNull(result);
        Assert.assertEquals(orderSet.getUuid(), PropertyUtils.getProperty(result, "uuid"));
        Assert.assertEquals(orderSet.getName(), PropertyUtils.getProperty(result, "name"));
        Assert.assertEquals(orderSet.getDescription(), PropertyUtils.getProperty(result, "description"));
        Assert.assertEquals(orderSet.getRetired(), PropertyUtils.getProperty(result, "retired"));
    }

    @Test
    public void shouldCreateAnOrderSet() throws Exception {
        long originalCount = getAllCount();

        SimpleObject orderSet = new SimpleObject();
        orderSet.add("name", "New OrderSet");
        orderSet.add("description", "OrderSet description");
        orderSet.add("operator", "ALL");

        String json = new ObjectMapper().writeValueAsString(orderSet);

        MockHttpServletRequest req = request(RequestMethod.POST, getURI());
        req.setContent(json.getBytes());

        SimpleObject newOrderSet = deserialize(handle(req));

        Assert.assertNotNull(PropertyUtils.getProperty(newOrderSet, "uuid"));
        Assert.assertEquals(originalCount + 1, getAllCount());
    }

    @Test
    public void shouldCreateAnOrderSetWithSomeOrderSetMembers() throws Exception {
        long originalCount = getAllCount();

        String json = "{\n" +
                "  \"name\": \"A\",\n" +
                "  \"description\": \"OSA\",\n" +
                "  \"operator\": \"ALL\",\n" +
                "  \"orderSetMembers\": [\n" +
                "      {\n" +
                "      \"orderType\": {\n" +
                "        \"uuid\": \"131168f4-15f5-102d-96e4-000c29c2a5d7\"\n" +
                "      },\n" +
                "      \"concept\": {\n" +
                "        \"name\": \"Amoxicillin\",\n" +
                "        \"uuid\": \"concept_uuid1\"\n" +
                "      }\n" +
                "    }\n" +
                "    ]\n" +
                "}";

        System.out.println(json);
        MockHttpServletRequest req = request(RequestMethod.POST, getURI());
        req.setContent(json.getBytes());

        SimpleObject newOrderSet = deserialize(handle(req));

        Assert.assertNotNull(PropertyUtils.getProperty(newOrderSet, "uuid"));
        Assert.assertEquals(originalCount + 1, getAllCount());
    }

    @Test
    public void shouldEditAnOrderSet() throws Exception {

        final String editedName = "OrderSet Edited";
        String json = "{ \"name\":\"" + editedName + "\" }";
        MockHttpServletRequest req = request(RequestMethod.POST, getURI() + "/" + getUuid());
        req.setContent(json.getBytes());
        handle(req);

        OrderSet editedOrderSet = orderSetService.getOrderSetByUuid(getUuid());

        Assert.assertNotNull(editedOrderSet);
        Assert.assertEquals(editedName, editedOrderSet.getName());
    }

    @Test
    public void shouldRetireAnOrderSet() throws Exception {
        assertEquals(false, orderSetService.getOrderSetByUuid(getUuid()).getRetired());

        MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
        req.addParameter("!purge", "");
        req.addParameter("reason", "random reason");
        handle(req);

        OrderSet retiredOrderSet = orderSetService.getOrderSetByUuid(getUuid());

        Assert.assertTrue(retiredOrderSet.isRetired());
        Assert.assertEquals("random reason", retiredOrderSet.getRetireReason());
    }

}
