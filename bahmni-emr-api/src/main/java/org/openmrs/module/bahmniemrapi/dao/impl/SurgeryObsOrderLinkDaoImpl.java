package org.openmrs.module.bahmniemrapi.dao.impl;

import org.hibernate.SessionFactory;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.dao.SurgeryObsOrderLinkDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SurgeryObsOrderLinkDaoImpl implements SurgeryObsOrderLinkDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public SurgeryObsOrderLinkDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void assignOrderToUnlinkedObs(Order order, Encounter encounter) {
        sessionFactory.getCurrentSession()
                .createQuery("UPDATE Obs o SET o.order = :order WHERE o.encounter = :encounter AND o.voided = false AND o.order IS NULL")
                .setParameter("order", order)
                .setParameter("encounter", encounter)
                .executeUpdate();
    }
}
