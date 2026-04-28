package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.service.InventoryStockService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class InventoryStockController extends BaseRestController {

    private static final String BASE_URL = "/rest/" + RestConstants.VERSION_1 + "/availableStocks";

    private final InventoryStockService inventoryStockService;

    public InventoryStockController(InventoryStockService inventoryStockService) {
        this.inventoryStockService = inventoryStockService;
    }

    @GetMapping(BASE_URL)
    @ResponseBody
    public AvailableStockResponse getAvailableInventoryStockDetailsForProduct(
            @RequestParam(value = "productUuid") String productUuid,
            @RequestParam(value = "locationUuid", required = false) String locationUuid) {

        if (productUuid == null || productUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("productUuid is required");
        }

        return inventoryStockService.getAvailableStocksFromInventory(productUuid, locationUuid);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    @ResponseBody
    public ResponseEntity<String> handleOdooClientError(HttpClientErrorException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getResponseBodyAsString());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<String> handleValidationError(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("{\"status\": \"bad_request\", \"message\": \"" + ex.getMessage() + "\"}");
    }
}
