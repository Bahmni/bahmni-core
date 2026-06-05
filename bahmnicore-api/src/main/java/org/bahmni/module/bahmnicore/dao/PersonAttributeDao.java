package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.model.ResultList;

public interface PersonAttributeDao {
	
	public ResultList getUnique(String personAttribute, String query);
}
