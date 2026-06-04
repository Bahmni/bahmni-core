package org.openmrs.module.bahmniemrapi.document.contract;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class Document {
    private String image;
    private String format;
    private String testUuid;
    private String obsUuid;
    private Date obsDateTime;
    private boolean voided;
    private String comment;

    public Document() {
    }

    public Document(String image, String format, String testUuid, String obsUuid, Date obsDateTime, boolean voided) {
        this.image = image;
        this.format = format;
        this.testUuid = testUuid;
        this.obsUuid = obsUuid;
        this.obsDateTime = obsDateTime;
        this.voided = voided;
    }

    public Document(String image, String format, String testUuid, String obsUuid, Date obsDateTime, boolean voided, String comment) {
        this.image = image;
        this.format = format;
        this.testUuid = testUuid;
        this.obsUuid = obsUuid;
        this.obsDateTime = obsDateTime;
        this.voided = voided;
        this.comment = comment;
    }

    public boolean isNew() {
        return StringUtils.isBlank(getObsUuid());
    }

    public boolean shouldVoidDocument() {
        return !StringUtils.isBlank(getObsUuid()) && isVoided();
    }

    public boolean hasConceptChanged(String referenceUuid) {
        return !referenceUuid.equals(getTestUuid());
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTestUuid() {
        return testUuid;
    }

    public void setTestUuid(String testUuid) {
        this.testUuid = testUuid;
    }

    public String getObsUuid() {
        return obsUuid;
    }

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    public Date getObsDateTime() {
        return obsDateTime;
    }

    public void setObsDateTime(Date obsDateTime) {
        this.obsDateTime = obsDateTime;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
