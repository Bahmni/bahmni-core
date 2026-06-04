package org.bahmni.module.bahmnicore.model;

public enum VideoFormats {

    OGG("OGG"), _3GP("3GPP"), MP4("MP4"), MPEG("MPEG"), WMV("WMV"), AVI("AVI"), MOV("MOV"), FLV("FLV"), WEBM("WEBM"), MKV("MKV");

    private final String value;

    VideoFormats(String value) {
        this.value = value;
    }

    public static boolean isFormatSupported(String givenFormat) {
        for (VideoFormats format : VideoFormats.values()) {
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

