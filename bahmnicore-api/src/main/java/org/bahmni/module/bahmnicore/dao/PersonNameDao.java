package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.model.ResultList;

public interface PersonNameDao {
	
	public ResultList getUnique(String key, String query);
}
