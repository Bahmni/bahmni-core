package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.PersonNameDao;
import org.bahmni.module.bahmnicore.model.ResultList;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.PersonName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PersonNameDaoImpl implements PersonNameDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@SuppressWarnings("unchecked")
	@Override
	public ResultList getUnique(String key, String query) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(PersonName.class);
		criteria.add(Restrictions.ilike(key, query + "%"));
		criteria.setProjection(Projections.distinct(Projections.property(key)));
		criteria.setMaxResults(20);
		return new ResultList(criteria.list());
	}
}
