package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;

public interface StockService {
    AvailableStockResponse getAvailableStocks(String productUuid, String locationUuid);
}
