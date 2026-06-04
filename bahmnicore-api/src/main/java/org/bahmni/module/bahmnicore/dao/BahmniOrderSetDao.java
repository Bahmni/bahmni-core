package org.bahmni.module.bahmnicore.dao;


import org.openmrs.OrderSet;

import java.util.List;

public interface BahmniOrderSetDao {
   List<OrderSet> getOrderSetByQuery(String searchTerm);
}
