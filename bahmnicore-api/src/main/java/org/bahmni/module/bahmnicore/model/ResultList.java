package org.bahmni.module.bahmnicore.model;

import java.util.ArrayList;
import java.util.List;

public class ResultList {
	
	private List<String> results;
	
	public ResultList(List<String> results) {
		this.results = results == null ? new ArrayList<String>() : results;
	}
	
	public List<String> getResults() {
		return results;
	}
	
	public int size() {
		return results.size();
	}
}
