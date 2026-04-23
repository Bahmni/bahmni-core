package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.stock.AvailableStockResponse;

public interface InventoryStockDetailsService {
    AvailableStockResponse getAvailableStocksFromInventory(String productUuid, String locationUuid);
}
