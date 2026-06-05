package org.bahmni.module.bahmnicore.web.v1_0.controller.search;

import org.bahmni.module.bahmnicore.dao.PersonNameDao;
import org.bahmni.module.bahmnicore.model.ResultList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PersonNameSearchControllerTest {
	
	@Mock
	PersonNameDao lastNameList;
	
	@Before
	public void setup() {
		initMocks(this);
	}
	
	@Test
	public void shouldCallDaoToSearchForPatientLastNames() {
		String query = "family";
		String key = "familyName";
		List<String> requiredResult = Arrays.asList("familyName1", "familyName2", "familyName3");
		when(lastNameList.getUnique(key, query)).thenReturn(new ResultList(requiredResult));
		PersonNameSearchController controller = new PersonNameSearchController(lastNameList);
		
		ResultList resultList = controller.searchFor(query, key);
		
		verify(lastNameList).getUnique(key, query);
		assertEquals(requiredResult.size(), resultList.size());
		for (String name : requiredResult) {
			assertTrue(resultList.getResults().contains(name));
		}
	}
}
