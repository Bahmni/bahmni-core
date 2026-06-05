package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.service.OrderService;
import org.bahmni.module.bahmnicore.web.v1_0.search.BahmniMainResourceControllerTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class BahmniOrderResourceITBahmni extends BahmniMainResourceControllerTest {

    @Autowired
    OrderService orderService;


    @Before
    public void setUp() throws Exception {
        executeDataSet("drugOrdersForDelete.xml");
    }

    @Test
    public void shouldVoidChildOrders() throws Exception {
        Order order = orderService.getOrderByUuid("6d0ae386-707a-4629-9850-f15206e63ab0");
        assertTrue(!order.isVoided());
        assertTrue(!order.getPreviousOrder().isVoided());
        assertNotNull(order);
        Order childOrder = orderService.getChildOrder(order);
        MockHttpServletRequest mockHttpServletRequest = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
        handle(mockHttpServletRequest);
        order = orderService.getOrderByUuid("6d0ae386-707a-4629-9850-f15206e63ab0");
        assertTrue(!order.getPreviousOrder().isVoided());
        assertTrue(order.isVoided());
        assertTrue(childOrder.isVoided());

    }

    @Test
    public void shouldVoidDiscontinuedOrder() throws Exception {
        Order order = orderService.getOrderByUuid("DISCONTINUED_ORDER");
        assertTrue(!order.isVoided());
        assertTrue(!order.getPreviousOrder().isVoided());
        assertNotNull(order);
        MockHttpServletRequest mockHttpServletRequest = request(RequestMethod.DELETE, getURI() + "/" + "DISCONTINUED_ORDER");
        handle(mockHttpServletRequest);
        order = orderService.getOrderByUuid("DISCONTINUED_ORDER");
        assertTrue(!order.getPreviousOrder().isVoided());
        assertNull(order.getPreviousOrder().getDateStopped());
        assertTrue(order.isVoided());
        assertNull(orderService.getChildOrder(order));

    }

    @Override
    public String getURI() {
        return "order";
    }

    @Override
    public String getUuid() {
        return "6d0ae386-707a-4629-9850-f15206e63ab0";
    }

    @Override
    public long getAllCount() {
        return 0;
    }
}