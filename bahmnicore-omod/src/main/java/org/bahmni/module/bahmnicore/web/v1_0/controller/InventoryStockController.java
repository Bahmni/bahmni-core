package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.service.StockService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

@Controller
public class InventoryStockController extends BaseRestController {

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "inventory/availableStocks";

    @Autowired
    private StockService stockService;

    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public AvailableStockResponse getAvailableBatchesForVaccine(
            @RequestParam(value = "productUuid") String productUuid,
            @RequestParam(value = "locationUuid", required = false) String locationUuid) {

        if (productUuid == null || productUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("productUuid is required");
        }

        return stockService.getAvailableStocks(productUuid, locationUuid);
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
