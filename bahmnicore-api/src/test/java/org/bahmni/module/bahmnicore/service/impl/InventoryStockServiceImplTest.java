package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.client.BahmniOdooClient;
import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InventoryStockServiceImplTest {

    private static final String PRODUCT_UUID = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";
    private static final String LOCATION_UUID = "7672b695-1872-40de-9ae8-a2bb38038208";

    private static final String VALID_RESPONSE_JSON = "{"
            + "\"count\":3,"
            + "\"data\":["
            + "  {\"stock_location_name\":\"Stock\",\"available_quantity\":100.0,\"on_hand_quantity\":150.0,\"unit\":\"Units\",\"batch_number\":\"AB0001\",\"expiry_date\":\"2026-06-26T10:10:08Z\"},"
            + "  {\"stock_location_name\":\"Stock\",\"available_quantity\":200.0},"
            + "  {\"stock_location_name\":\"Spare Stock\",\"available_quantity\":300.0,\"on_hand_quantity\":400.0,\"unit\":\"Tablets\",\"batch_number\":\"AB0002\",\"expiry_date\":\"2026-08-29T10:10:37Z\"}"
            + "]}";

    @Mock
    private BahmniOdooClient bahmniOdooClient;

    private InventoryStockServiceImpl inventoryStockService;

    @Before
    public void setUp() {
        inventoryStockService = new InventoryStockServiceImpl(bahmniOdooClient);
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldReturnParsedResponseOnSuccess() {
        when(bahmniOdooClient.get(anyString())).thenReturn(VALID_RESPONSE_JSON);

        AvailableStockResponse response =
                inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);

        assertNotNull(response);
        assertEquals(3, response.getCount());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size());
        assertEquals("Stock", response.getData().get(0).getStockLocationName());
        assertEquals(100.0, response.getData().get(0).getAvailableQuantity(), 0.001);
        assertEquals(150.0, response.getData().get(0).getOnHandQuantity(), 0.001);
        assertEquals("Units", response.getData().get(0).getUnit());
        assertEquals("AB0001", response.getData().get(0).getBatchNumber());
        assertEquals("2026-06-26T10:10:08Z", response.getData().get(0).getExpiryDate());
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldHandleStockEntriesWithoutBatchInfo() {
        when(bahmniOdooClient.get(anyString())).thenReturn(VALID_RESPONSE_JSON);

        AvailableStockResponse response =
                inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);

        // Second entry has no batch_number or expiry_date
        assertNotNull(response.getData().get(1));
        assertEquals("Stock", response.getData().get(1).getStockLocationName());
        assertEquals(200.0, response.getData().get(1).getAvailableQuantity(), 0.001);
        assertEquals(null, response.getData().get(1).getOnHandQuantity());
        assertEquals(null, response.getData().get(1).getUnit());
        assertEquals(null, response.getData().get(1).getBatchNumber());
        assertEquals(null, response.getData().get(1).getExpiryDate());
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldCallOdooClientWithUrlContainingProductUuid() {
        when(bahmniOdooClient.get(anyString())).thenReturn(VALID_RESPONSE_JSON);

        inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);

        verify(bahmniOdooClient).get(
                org.mockito.Matchers.contains("product_uuid=" + PRODUCT_UUID));
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldRethrowHttpClientErrorExceptionFromOdooClient() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        when(bahmniOdooClient.get(anyString())).thenThrow(exception);

        try {
            inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);
            fail("Expected HttpClientErrorException to be rethrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldWrapGeneralExceptionInOdooApiException() {
        when(bahmniOdooClient.get(anyString()))
                .thenReturn("this is not valid json {{{");

        try {
            inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);
            fail("Expected OdooApiException to be thrown");
        } catch (OdooApiException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void getAvailableStocks_FromInventory_shouldWrapRuntimeExceptionInOdooApiException() {
        when(bahmniOdooClient.get(anyString()))
                .thenThrow(new RuntimeException("Something went wrong"));

        try {
            inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);
            fail("Expected OdooApiException to be thrown");
        } catch (OdooApiException ex) {
            assertNotNull(ex.getMessage());
            assertNotNull(ex.getCause());
        }
    }
}
