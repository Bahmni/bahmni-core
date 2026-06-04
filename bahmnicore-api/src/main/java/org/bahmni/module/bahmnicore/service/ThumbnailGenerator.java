package org.bahmni.module.bahmnicore.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public interface ThumbnailGenerator {
	boolean isFormatSupported(String format);
	BufferedImage generateThumbnail(File video) throws IOException;
}
