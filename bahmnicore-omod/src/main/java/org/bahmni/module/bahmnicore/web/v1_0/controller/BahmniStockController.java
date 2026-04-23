package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.service.StockService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BahmniStockController extends BaseRestController {

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/available-batches-for-vaccine";

    @Autowired
    private StockService stockService;

    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public AvailableStockResponse getAvailableBatchesForVaccine(
            @RequestParam(value = "productUuid") String productUuid,
            @RequestParam(value = "locationUuid") String locationUuid) {
        return stockService.getAvailableStocks(productUuid, locationUuid);
    }
}
