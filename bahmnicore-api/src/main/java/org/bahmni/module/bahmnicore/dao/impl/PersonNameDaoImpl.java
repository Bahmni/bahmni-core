/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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
