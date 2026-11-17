package org.bahmni.module.bahmnicore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.client.BahmniOdooClient;
import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;
import org.bahmni.module.bahmnicore.contract.stock.OdooAvailableStockResponse;
import org.bahmni.module.bahmnicore.contract.stock.OdooStockData;
import org.bahmni.module.bahmnicore.contract.stock.StockData;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.bahmni.module.bahmnicore.service.InventoryStockService;
import org.bahmni.module.bahmnicore.util.OdooUrlBuilder;
import org.bahmni.webclients.WebClientsException;

import java.util.ArrayList;
import java.util.List;

public class InventoryStockServiceImpl implements InventoryStockService {

    private static final Logger logger = LogManager.getLogger(InventoryStockServiceImpl.class);

    private final BahmniOdooClient bahmniOdooClient;
    private final ObjectMapper objectMapper;

    public InventoryStockServiceImpl(BahmniOdooClient bahmniOdooClient) {
        this.bahmniOdooClient = bahmniOdooClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AvailableStockResponse getAvailableStocksFromInventory(String productUuid, String locationUuid) {
        logger.info("Fetching available stocks for product: {}, location: {}", productUuid, locationUuid);
        String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, locationUuid);
        try {
            String json = bahmniOdooClient.get(url);
            OdooAvailableStockResponse odooResponse = objectMapper.readValue(json, OdooAvailableStockResponse.class);
            AvailableStockResponse response = toAvailableStockResponse(odooResponse);
            logger.info("Successfully fetched {} stock entries", response != null ? response.getCount() : 0);
            return response;
        } catch (WebClientsException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            int statusCode = msg.startsWith("Bad response code of ")
                    ? Integer.parseInt(msg.replace("Bad response code of ", "").trim())
                    : 0;
            logger.warn("Odoo request failed with status {}: {}", statusCode, msg);
            throw new OdooApiException("Error fetching available stocks: " + msg, statusCode, e);
        } catch (Exception e) {
            logger.error("Error fetching available stocks from Odoo", e);
            throw new OdooApiException("Error fetching available stocks: " + e.getMessage(), e);
        }
    }

    private AvailableStockResponse toAvailableStockResponse(OdooAvailableStockResponse odooResponse) {
        if (odooResponse == null) {
            return null;
        }
        AvailableStockResponse response = new AvailableStockResponse();
        response.setCount(odooResponse.getCount());
        if (odooResponse.getData() != null) {
            List<StockData> data = new ArrayList<>(odooResponse.getData().size());
            for (OdooStockData odooStock : odooResponse.getData()) {
                data.add(toStockData(odooStock));
            }
            response.setData(data);
        }
        return response;
    }

    private StockData toStockData(OdooStockData odooStock) {
        StockData stockData = new StockData();
        stockData.setStockLocationName(odooStock.getStockLocationName());
        stockData.setAvailableQuantity(odooStock.getAvailableQuantity());
        stockData.setOnHandQuantity(odooStock.getOnHandQuantity());
        stockData.setUnit(odooStock.getUnit());
        stockData.setBatchNumber(odooStock.getBatchNumber());
        stockData.setExpiryDate(odooStock.getExpiryDate());
        return stockData;
    }
}
