package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class ReferenceTermRow extends CSVEntity {
    @CSVHeader(name = "Code")
    private String code;

    @CSVHeader(name = "Source")
    private String source;

    @CSVHeader(name = "Name")
    private String name;

    @CSVHeader(name = "Description", optional = true)
    private String description;

    @CSVHeader(name = "Version", optional = true)
    private String version;

    public ReferenceTermRow() {
    }

    public ReferenceTermRow(String code, String source, String name, String description, String version) {
        this.code = code;
        this.source = source;
        this.name = name;
        this.description = description;
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
