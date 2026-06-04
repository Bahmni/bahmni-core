package org.bahmni.module.admin.config.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

import java.io.Serializable;
import java.util.Date;

public class BahmniConfig extends BaseOpenmrsObject implements Auditable, Serializable {
    private Integer configId;

    private String appName;

    private String configName;

    private User creator;

    private Date dateCreated;

    private User changedBy;

    private Date dateChanged;

    private String config;

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Override
    public Integer getId() {
        return getConfigId();
    }

    @Override
    public void setId(Integer id) {
        setConfigId(id);
    }

    @Override
    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    @Override
    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    @JsonIgnore
    public User getChangedBy() {
        return changedBy;
    }

    @Override
    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    @Override
    public Date getDateChanged() {
        return dateChanged;
    }

    @Override
    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }
}
