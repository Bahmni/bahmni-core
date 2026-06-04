package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.model.ResultList;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@Ignore
public class PersonAttributeDaoImplIT extends BaseIntegrationTest {
	
	@Autowired
	private PersonAttributeDaoImpl personAttributeDao;
	
	@Test
    @Ignore
	public void shouldRetrieveUniqueCasteList() throws Exception {
        assertEquals(0, personAttributeDao.getUnique("caste", "caste").size());

        executeDataSet("apiTestData.xml");
		
		ResultList result = personAttributeDao.getUnique("caste", "caste");
		assertEquals(2, result.size());
	}
	
	@Test
    @Ignore
	public void shouldRetrieveOnly20Results() throws Exception {
		executeDataSet("apiTestData.xml");
		
		ResultList result = personAttributeDao.getUnique("caste", "test");
		assertTrue(result.size() <= 20);
	}
}
