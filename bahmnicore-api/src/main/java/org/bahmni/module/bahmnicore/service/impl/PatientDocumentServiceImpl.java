package org.bahmni.module.bahmnicore.service.impl;

import liquibase.util.file.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.BahmniCoreException;
import org.bahmni.module.bahmnicore.bahmniexceptions.FileTypeNotSupportedException;
import org.bahmni.module.bahmnicore.bahmniexceptions.VideoFormatNotSupportedException;
import org.bahmni.module.bahmnicore.model.VideoFormats;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.bahmni.module.bahmnicore.service.PatientDocumentService;
import org.bahmni.module.bahmnicore.service.ThumbnailGenerator;
import org.imgscalr.Scalr;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Lazy
public class PatientDocumentServiceImpl implements PatientDocumentService {
    private static final String PDF = "pdf";
    private static final String INVALID_FILE_SPECIFIED = "Invalid file specified";
    private static final String FILE_NAME_PARAM_REQUIRED = "[Required String parameter 'filename' is empty]";
    private Log log = LogFactory.getLog(PatientDocumentServiceImpl.class);
    private static final String patientImagesFormat = "jpeg";
    private final Integer NO_OF_PATIENT_FILE_IN_A_DIRECTORY = 100;
    private final String VIDEO_FILE_TYPE = "video";
    private final String IMAGE_FILE_TYPE = "image";
    private static final String LAB_RESULT_ENCOUNTER_TYPE = "LAB_RESULT";

    protected void setThumbnailGenerators(List<ThumbnailGenerator> thumbnailGenerators) {
        this.thumbnailGenerators = thumbnailGenerators;
    }

    @Autowired
    List<ThumbnailGenerator> thumbnailGenerators;



    @Override
    public void saveImage(String patientIdentifier, String image) {
        try {
            if (image == null || image.isEmpty()) return;

            File outputFile = new File(String.format("%s/%s.%s", BahmniCoreProperties.getProperty("bahmnicore.images.directory"), patientIdentifier, patientImagesFormat));
            saveDocumentInFile(image, patientImagesFormat, outputFile, "image");
        } catch (IOException e) {
            throw new BahmniCoreException("[%s] : Could not save patient image", e);
        }
    }

    @Override
    public String saveDocument(Integer patientId, String encounterTypeName, String content, String format, String fileType, String fileName) {
        String basePath;
        try {
            if (content == null || content.isEmpty()) return null;
            basePath = getBasePathByEncounterType(encounterTypeName);

            String relativeFilePath = createFilePath(basePath, patientId, encounterTypeName, format, fileName);

            File outputFile = new File(String.format("%s/%s", basePath, relativeFilePath));
            saveDocumentInFile(content, format, outputFile, fileType);

            return relativeFilePath;

        } catch (IOException e) {
            throw new BahmniCoreException("[%s] : Could not save patient Document ", e);
        }
    }

    private String getBasePathByEncounterType(String encounterTypeName) {
        String basePath;
        if(encounterTypeName.equalsIgnoreCase(LAB_RESULT_ENCOUNTER_TYPE)){
            basePath = BahmniCoreProperties.getProperty("bahmnicore.documents.lablitebaseDirectory");
        } else{
            basePath = getBasePath();
        }
        return basePath;
    }

    private String getBasePath() {
        return BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory");
    }

    private String createFileName(Integer patientId, String encounterTypeName, Object format, String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        if (StringUtils.isNotBlank(originalFileName)) {
            originalFileName = "__" + originalFileName;
        }
        return String.format("%s-%s-%s%s.%s", patientId, encounterTypeName, uuid, originalFileName, format);
    }

    protected String createFilePath(String basePath, Integer patientId, String encounterTypeName, String format, String originalFileName) {
        String fileName = createFileName(patientId, encounterTypeName, format, originalFileName);
        String documentDirectory = findDirectoryForDocumentsByPatientId(patientId);
        String absoluteFilePath = String.format("%s/%s", basePath, documentDirectory);
        File absoluteFileDirectory = new File(absoluteFilePath);
        if (!absoluteFileDirectory.exists()) {
            absoluteFileDirectory.mkdirs();
        }
        return String.format("%s/%s", documentDirectory, fileName);
    }

    private String findDirectoryForDocumentsByPatientId(Integer patientId) {
        Integer directory = (patientId / NO_OF_PATIENT_FILE_IN_A_DIRECTORY + 1) * NO_OF_PATIENT_FILE_IN_A_DIRECTORY;
        return directory.toString();
    }

    private void saveDocumentInFile(String content, String format, File outputFile, String fileType) throws IOException {
        log.info(String.format("Creating patient document of format %s at %s", format, outputFile));
        byte[] decodedBytes = DatatypeConverter.parseBase64Binary(content);
        if (VIDEO_FILE_TYPE.equals(fileType)) {
            if (!isVideoFormatSupported(format)) {
                throw new VideoFormatNotSupportedException(String.format("The video format '%s' is not supported. Supported formats are %s", format, Arrays.toString(VideoFormats.values())));
            }

            FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
            createAndSaveThumbnailForVideo(outputFile, format);

        } else if (PDF.equals(format)) {
            FileUtils.writeByteArrayToFile(outputFile, decodedBytes);
        } else if (IMAGE_FILE_TYPE.equals(fileType)){
            try {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
                boolean results = ImageIO.write(bufferedImage, format, outputFile);
                if(!results) {
                    throw new FileTypeNotSupportedException(String.format("The image format '%s' is not supported. Supported formats are %s", format, Arrays.toString(new String[]{"png", "jpeg", "gif"})));
                }
                saveThumbnail(bufferedImage, outputFile, null, 100);
                bufferedImage.flush();
                log.info(String.format("Successfully created patient image at %s", outputFile));
            } catch (Exception exception) {
                throw new FileTypeNotSupportedException(String.format("The image format '%s' is not supported. Supported formats are %s", format, Arrays.toString(new String[]{"png", "jpeg", "gif"})));
            }
        } else {
            throw new FileTypeNotSupportedException(String.format("The file type is not supported. Supported types are %s/%s/%s", IMAGE_FILE_TYPE, VIDEO_FILE_TYPE, PDF));
        }
    }

    private boolean isVideoFormatSupported(String format) {
        return VideoFormats.isFormatSupported(format);
    }


    private void createAndSaveThumbnailForVideo(File outputVideoFile, String format) throws IOException {
        ThumbnailGenerator thumbnailGenerator = getSupportedThumbnailGenerator(format);
        BufferedImage bufferedImage = null;
        if(thumbnailGenerator != null) {
            bufferedImage = thumbnailGenerator.generateThumbnail(outputVideoFile);
            saveThumbnail(bufferedImage, outputVideoFile, "jpg", 300);
        }
    }

    private ThumbnailGenerator getSupportedThumbnailGenerator(String format) throws IOException {
        for(ThumbnailGenerator thumbnailGenerator: thumbnailGenerators) {
            if (thumbnailGenerator.isFormatSupported(format)) {
             return thumbnailGenerator;
            }
        }
        return null;
    }

    private void saveThumbnail(BufferedImage image, File outputFile, String imageFileType, int imageSize) throws IOException {
        String nameWithoutExtension = FilenameUtils.removeExtension(outputFile.getAbsolutePath());
        if(imageFileType == null){
        imageFileType = FilenameUtils.getExtension(outputFile.getAbsolutePath());
        }
        File thumbnailFile = new File(String.format("%s_thumbnail.%s", nameWithoutExtension, imageFileType));
        BufferedImage reSizedImage = Scalr.resize(image, imageSize);
        ImageIO.write(reSizedImage, imageFileType, thumbnailFile);
        image.flush();
        reSizedImage.flush();
    }

    @Override
    public ResponseEntity<Object> retriveImage(String patientUuid) {
        File file = getPatientImageFile(patientUuid);
        return readImage(file);
    }

    @Override
    public void delete(String fileName) {
        validateFileToBeDeleted(fileName);
        File file = new File(getBasePath() + "/" + fileName);
        deleteThumbnailFile(file);
        deleteFile(file);
    }

    private void validateFileToBeDeleted(String fileName) {
        log.debug(String.format("Patient document file to be deleted: %s",  fileName));
        if (StringUtils.isBlank(fileName)) {
            log.error(FILE_NAME_PARAM_REQUIRED);
            throw new RuntimeException(FILE_NAME_PARAM_REQUIRED);
        }
        String docLocation = getBasePath();
        log.debug(String.format("Document path: %s", docLocation));
        Path docLocationPath = Paths.get(docLocation);
        Path filePath = Paths.get(docLocation, fileName).normalize();
        if (!filePath.startsWith(docLocationPath) || !filePath.toFile().exists()) {
            String invalidFileError = String.format(INVALID_FILE_SPECIFIED.concat(": %s"), fileName);
            log.error(invalidFileError);
            throw new RuntimeException(invalidFileError);
        }
    }

    private void deleteThumbnailFile(File file) {
        String absolutePath = file.getAbsolutePath();
        String nameWithoutExtension = FilenameUtils.removeExtension(absolutePath);
        String extension = FilenameUtils.getExtension(absolutePath);
        FileUtils.deleteQuietly(new File(String.format("%s_thumbnail.%s", nameWithoutExtension, extension)));
    }

    private void deleteFile(File file) {
        boolean deleted = FileUtils.deleteQuietly(file);
        if (deleted)
            log.info(String.format("%s file is deleted successfully", file.getAbsolutePath()));
        else
            log.warn(String.format("Unable to delete %s", file.getAbsolutePath()));
    }

    private File getPatientImageFile(String patientUuid) {
        File file = new File(String.format("%s/%s.%s", BahmniCoreProperties.getProperty("bahmnicore.images.directory"), patientUuid, patientImagesFormat));
        if (file.exists() && file.isFile()) {
            return file;
        }
        return new File(BahmniCoreProperties.getProperty("bahmnicore.images.directory.defaultImage"));
    }

    private ResponseEntity<Object> readImage(File file) {
        byte[] fileByteArray = new byte[(int) file.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            fileInputStream.read(fileByteArray);
            return new ResponseEntity<Object>(fileByteArray, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
