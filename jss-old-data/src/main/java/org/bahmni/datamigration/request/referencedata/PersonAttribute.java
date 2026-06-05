package org.bahmni.datamigration.request.referencedata;

import static org.bahmni.datamigration.DataScrub.scrubData;

public class PersonAttribute {
    private String uuid;
    private String display;
    private String name;
    private String description;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = scrubData(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = scrubData(description);
    }
}