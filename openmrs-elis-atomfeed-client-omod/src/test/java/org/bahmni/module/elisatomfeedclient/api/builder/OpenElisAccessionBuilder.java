package org.bahmni.module.elisatomfeedclient.api.builder;


import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccessionNote;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;

import java.util.Arrays;
import java.util.Set;

public class OpenElisAccessionBuilder {

    private OpenElisAccession openElisAccession;

    public OpenElisAccessionBuilder() {
        openElisAccession = new OpenElisAccession();
        openElisAccession.setAccessionUuid("1234");
        openElisAccession.addTestDetail(new OpenElisTestDetailBuilder().build());
        openElisAccession.setPatientIdentifier("GAN12345");
        openElisAccession.setLabLocationUuid("be69741b-29e9-49a1-adc9-2a726e6610e4");
    }

    public OpenElisAccession build() {
        return openElisAccession;
    }

    public OpenElisAccessionBuilder withTestDetails(Set<OpenElisTestDetail> testDetails) {
        openElisAccession.setTestDetails(testDetails);
        return this;
    }

    public OpenElisAccessionBuilder withDateTime(String dateTime) {
        openElisAccession.setDateTime(dateTime);
        return this;
    }

    public OpenElisAccessionBuilder withPatientUuid(String uuid) {
        openElisAccession.setPatientUuid(uuid);
        return this;
    }
    public OpenElisAccessionBuilder withAccessionNotes(OpenElisAccessionNote... accessionNotes) {
        openElisAccession.setAccessionNotes(Arrays.asList(accessionNotes));
        return this;
    }

    public OpenElisAccessionBuilder withLabLocationUuid(String labLocationUuid) {
        openElisAccession.setLabLocationUuid(labLocationUuid);
        return this;
    }

    public OpenElisAccessionBuilder withPatientIdentifier(String patientIdentifier) {
        openElisAccession.setPatientIdentifier(patientIdentifier);
        return this;
    }
}