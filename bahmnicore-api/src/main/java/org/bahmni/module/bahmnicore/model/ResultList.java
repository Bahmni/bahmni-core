/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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
