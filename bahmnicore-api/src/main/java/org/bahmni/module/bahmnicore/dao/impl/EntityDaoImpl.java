package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.EntityDao;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EntityDaoImpl implements EntityDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public <T> T getByUuid(String uuid, Class<T> className) {
        Session currentSession = sessionFactory.getCurrentSession();
        Criteria criteria = currentSession.createCriteria(className);
        criteria.add(Restrictions.eq("uuid", uuid));
        List list = criteria.list();
        return list.size() > 0? (T) list.get(0) : null;
    }
}
