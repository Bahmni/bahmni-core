package org.openmrs.module.bahmniemrapi.dao;

import org.openmrs.Encounter;
import org.openmrs.Order;

public interface SurgeryObsOrderLinkDao {

    void assignOrderToUnlinkedObs(Order order, Encounter encounter);
}
