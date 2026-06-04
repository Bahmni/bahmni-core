package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniOrderSetDao;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OrderSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BahmniOrderSetDaoImpl implements BahmniOrderSetDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<OrderSet> getOrderSetByQuery(String searchTerm) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderSet.class);
        criteria.add(Restrictions.or(Restrictions.like("name", searchTerm, MatchMode.ANYWHERE),
                Restrictions.like("description", searchTerm, MatchMode.ANYWHERE)));
        criteria.add(Restrictions.eq("retired", Boolean.FALSE));
        return criteria.list();
    }
}
