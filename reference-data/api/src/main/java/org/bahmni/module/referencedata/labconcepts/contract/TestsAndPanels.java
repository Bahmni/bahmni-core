package org.bahmni.module.referencedata.labconcepts.contract;

import java.util.HashSet;
import java.util.Set;

public class TestsAndPanels extends Resource {
    private Set<LabTest> tests;
    private Set<Panel> panels;

    public Set<LabTest> getTests() {
        if (tests == null) {
            tests = new HashSet<>();
        }
        return tests;
    }

    public void setTests(Set<LabTest> tests) {
        this.tests = tests;
    }

    public Set<Panel> getPanels() {
        if (panels == null) {
            panels = new HashSet<>();
        }
        return panels;
    }

    public void setPanels(Set<Panel> panels) {
        this.panels = panels;
    }

    public void addTest(LabTest labTest) {
        getTests().add(labTest);
    }

    public void addPanel(Panel panel) {
        getPanels().add(panel);
    }
}
