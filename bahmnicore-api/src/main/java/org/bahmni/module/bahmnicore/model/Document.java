package org.bahmni.module.bahmnicore.model;

public class Document {
    private String content;
    private String format;
    private String encounterTypeName;
    private String patientUuid;
    private String fileType;
    private String fileName;

    public Document() {
    }

    public Document(String content, String format, String encounterTypeName, String patientUuid, String fileType, String fileName) {
        this.content = content;
        this.format = format;
        this.encounterTypeName = encounterTypeName;
        this.patientUuid = patientUuid;
        this.fileType = fileType;
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getEncounterTypeName() {
        return encounterTypeName;
    }

    public void setEncounterTypeName(String encounterTypeName) {
        this.encounterTypeName = encounterTypeName;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}

