package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.model.VideoFormatsForThumbnailGeneration;
import org.bahmni.module.bahmnicore.service.ThumbnailGenerator;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class ThumbnailGeneratorImpl implements ThumbnailGenerator {

	@Override
	public boolean isFormatSupported(String format) {
		return VideoFormatsForThumbnailGeneration.isFormatSupported(format);
	}

	@Override
	public BufferedImage generateThumbnail(File video) throws IOException {
		BufferedImage bufferedImage;
		try{
			Picture picture =  FrameGrab.getFrameFromFile(video,0);
			bufferedImage = AWTUtil.toBufferedImage(picture);
		}
		catch (JCodecException e) {
			throw new RuntimeException(e);
		}
		return bufferedImage;
	}
}
