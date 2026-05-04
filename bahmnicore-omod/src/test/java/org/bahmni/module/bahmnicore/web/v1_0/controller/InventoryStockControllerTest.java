package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.contract.stock.StockData;
import org.bahmni.module.bahmnicore.service.InventoryStockService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InventoryStockControllerTest {

    private static final String PRODUCT_UUID = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";
    private static final String LOCATION_UUID = "7672b695-1872-40de-9ae8-a2bb38038208";

    @Mock
    private InventoryStockService inventoryStockService;

    @InjectMocks
    private InventoryStockController inventoryStockController;


    @Test
    public void getAvailableInventoryStockDetailsForProduct_shouldDelegateToServiceAndReturnResponse() {
        AvailableStockResponse expectedResponse = buildSampleResponse();
        when(inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID))
                .thenReturn(expectedResponse);

        AvailableStockResponse result =
                inventoryStockController.getAvailableInventoryStockDetailsForProduct(PRODUCT_UUID, LOCATION_UUID);

        assertNotNull(result);
        assertEquals(2, result.getCount());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());
        verify(inventoryStockService).getAvailableStocksFromInventory(PRODUCT_UUID, LOCATION_UUID);
    }

    @Test
    public void getAvailableInventoryStockDetailsForProduct_shouldWorkWithNullLocationUuid() {
        AvailableStockResponse expectedResponse = buildSampleResponse();
        when(inventoryStockService.getAvailableStocksFromInventory(PRODUCT_UUID, null))
                .thenReturn(expectedResponse);

        AvailableStockResponse result =
                inventoryStockController.getAvailableInventoryStockDetailsForProduct(PRODUCT_UUID, null);

        assertNotNull(result);
        verify(inventoryStockService).getAvailableStocksFromInventory(PRODUCT_UUID, null);
    }

    @Test
    public void getAvailableInventoryStockDetailsForProduct_shouldThrowIllegalArgumentExceptionWhenProductUuidIsNull() {
        try {
            inventoryStockController.getAvailableInventoryStockDetailsForProduct(null, LOCATION_UUID);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void getAvailableInventoryStockDetailsForProduct_shouldThrowIllegalArgumentExceptionWhenProductUuidIsBlank() {
        try {
            inventoryStockController.getAvailableInventoryStockDetailsForProduct("   ", LOCATION_UUID);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void getAvailableInventoryStockDetailsForProduct_shouldThrowIllegalArgumentExceptionWhenProductUuidIsEmpty() {
        try {
            inventoryStockController.getAvailableInventoryStockDetailsForProduct("", LOCATION_UUID);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void handleOdooClientError_shouldReturnResponseEntityWithExceptionStatusCodeAndBody() {
        String errorBody = "{\"error\":\"Not Found\"}";
        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.NOT_FOUND, "Not Found",
                errorBody.getBytes(), java.nio.charset.StandardCharsets.UTF_8);

        ResponseEntity<String> responseEntity =
                inventoryStockController.handleOdooClientError(exception);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(errorBody, responseEntity.getBody());
    }

    @Test
    public void handleOdooClientError_shouldPreserveOriginalStatusCodeFrom401() {
        HttpClientErrorException exception =
                new HttpClientErrorException(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> responseEntity =
                inventoryStockController.handleOdooClientError(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void handleValidationError_shouldReturn400BadRequestWithJsonErrorMessage() {
        IllegalArgumentException exception =
                new IllegalArgumentException("productUuid is required");

        ResponseEntity<String> responseEntity =
                inventoryStockController.handleValidationError(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        String body = responseEntity.getBody();
        assert body.contains("bad_request");
        assert body.contains("productUuid is required");
    }

    // ---- helpers ----

    private AvailableStockResponse buildSampleResponse() {
        StockData stock1 = new StockData();
        stock1.setStockLocationName("Stock");
        stock1.setAvailableQuantity(100.0);
        stock1.setBatchNumber("AB0001");
        stock1.setExpiryDate("2026-06-26T10:10:08Z");

        StockData stock2 = new StockData();
        stock2.setStockLocationName("Spare Stock");
        stock2.setAvailableQuantity(200.0);

        AvailableStockResponse response = new AvailableStockResponse();
        response.setCount(2);
        response.setData(Arrays.asList(stock1, stock2));
        return response;
    }
}
