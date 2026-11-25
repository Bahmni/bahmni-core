package org.bahmni.module.bahmnicore.contract.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AvailableStockResponseJsonTest {

    private static final Set<String> EXPECTED_KEYS = new TreeSet<>(Arrays.asList(
            "stockLocationName", "availableQuantity", "onHandQuantity", "unit", "batchNumber", "expiryDate"));

    private AvailableStockResponse buildResponse() {
        StockData stock = new StockData();
        stock.setStockLocationName("Stock");
        stock.setAvailableQuantity(25.0);
        stock.setOnHandQuantity(30.0);
        stock.setUnit("Units");
        stock.setBatchNumber("AB0001");
        stock.setExpiryDate("2027-01-31T00:00:00Z");

        AvailableStockResponse response = new AvailableStockResponse();
        response.setCount(1);
        response.setData(Arrays.asList(stock));
        return response;
    }

    private Set<String> fieldNames(JsonNode node) {
        Set<String> names = new TreeSet<>();
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            names.add(it.next());
        }
        return names;
    }

    @Test
    public void jackson2_shouldWriteCamelCaseKeysForUi() throws Exception {
        String json = new ObjectMapper().writeValueAsString(buildResponse());

        JsonNode root = new ObjectMapper().readTree(json);
        assertEquals(new TreeSet<>(Arrays.asList("count", "data")), fieldNames(root));
        assertEquals(EXPECTED_KEYS, fieldNames(root.get("data").get(0)));
        assertEquals("AB0001", root.get("data").get(0).get("batchNumber").asText());
        assertEquals("2027-01-31T00:00:00Z", root.get("data").get(0).get("expiryDate").asText());
        assertEquals("Stock", root.get("data").get(0).get("stockLocationName").asText());
    }

    @Test
    public void jackson1_shouldWriteTheSameCamelCaseKeys() throws Exception {
        org.codehaus.jackson.map.ObjectMapper mapper = new org.codehaus.jackson.map.ObjectMapper();
        String json = mapper.writeValueAsString(buildResponse());

        JsonNode root = new ObjectMapper().readTree(json);
        assertEquals(EXPECTED_KEYS, fieldNames(root.get("data").get(0)));
    }

    @Test
    public void shouldNotExposeOdooSnakeCaseKeys() throws Exception {
        JsonNode stock = new ObjectMapper().readTree(new ObjectMapper().writeValueAsString(buildResponse()))
                .get("data").get(0);

        assertFalse(stock.has("batch_number"));
        assertFalse(stock.has("expiry_date"));
        assertFalse(stock.has("stock_location_name"));
        assertFalse(stock.has("available_quantity"));
        assertFalse(stock.has("on_hand_quantity"));
    }
}
