/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model;

public enum VideoFormatsForThumbnailGeneration {
	_3GP("3GPP"), MP4("MP4"), MOV("MOV");

	private final String value;

	VideoFormatsForThumbnailGeneration(String value) {
		this.value = value;
	}

	public static boolean isFormatSupported(String givenFormat) {
		for (VideoFormatsForThumbnailGeneration format : VideoFormatsForThumbnailGeneration.values()) {
			if (givenFormat.toUpperCase().contains(format.value))
				return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return value.toLowerCase();
	}
}
