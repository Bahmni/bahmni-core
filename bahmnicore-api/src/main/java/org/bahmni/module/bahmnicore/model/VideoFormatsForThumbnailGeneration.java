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
