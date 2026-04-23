package org.bahmni.module.bahmnicore.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.client.OdooClient;
import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.bahmni.module.bahmnicore.service.StockService;
import org.bahmni.module.bahmnicore.util.OdooUrlBuilder;

public class StockServiceImpl implements StockService {

    private static final Logger logger = LogManager.getLogger(StockServiceImpl.class);

    private final OdooClient odooClient;

    public StockServiceImpl(OdooClient odooClient) {
        this.odooClient = odooClient;
    }

    @Override
    public AvailableStockResponse getAvailableStocks(String productUuid, String locationUuid) {
        logger.info("Fetching available stocks for product: {}, location: {}", productUuid, locationUuid);
        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, locationUuid);
        try {
            AvailableStockResponse response = odooClient.getWithAuthRetry(url, AvailableStockResponse.class);
            logger.info("Successfully fetched {} stock entries", response != null ? response.getCount() : 0);
            return response;
        } catch (Exception e) {
            logger.error("Error fetching available stocks from Odoo", e);
            throw new OdooApiException("Error fetching available stocks: " + e.getMessage(), e);
        }
    }
}
