package org.bahmni.module.bahmnicore.service;


import org.openmrs.OrderSet;

import java.util.List;

public interface BahmniOrderSetService {
    List<OrderSet> getOrderSetByQuery(String searchTerm);
}
