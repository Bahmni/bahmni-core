package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisPatient;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisPatientAttribute;
import org.junit.Test;
import org.openmrs.PersonAttributeType;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class BahmniPatientMapperTest {

    @Test
    public void shouldMapPatientAttributes() throws Exception {

        List<PersonAttributeType> attributeTypes = new ArrayList<PersonAttributeType>() {{
            this.add(new PersonAttributeType() {{
                setName("occupation");
                setFormat("org.openmrs.Concept");
            }});
            this.add(new PersonAttributeType() {{
                setName("primaryRelative");
                setFormat("java.lang.String");
            }});
        }};

        BahmniPatientMapper bahmniPatientMapper = new BahmniPatientMapper(attributeTypes);
        final List<OpenElisPatientAttribute> attributes = new ArrayList<OpenElisPatientAttribute>() {{
            add( new OpenElisPatientAttribute("OCCUPATION", "Tailor"));
            add( new OpenElisPatientAttribute("PRIMARYRELATIVE", "Milka Singh"));
        }};

        OpenElisPatient openElisPatient = new OpenElisPatient() {{
            setAttributes(attributes);
        }};

        BahmniPatient bahmniPatient = bahmniPatientMapper.map(openElisPatient);
        assertEquals(1, bahmniPatient.getAttributes().size());
        assertEquals("Milka Singh", bahmniPatient.getAttributes().get(0).getValue());
    }

    @Test
    public void shouldMapPatientUUID() throws Exception {
        List<PersonAttributeType> attributeTypes = new ArrayList<PersonAttributeType>() {{
            this.add(new PersonAttributeType() {{
                setName("occupation");
                setFormat("org.openmrs.Concept");
            }});
            this.add(new PersonAttributeType() {{
                setName("primaryRelative");
                setFormat("java.lang.String");
            }});
        }};

        BahmniPatientMapper bahmniPatientMapper = new BahmniPatientMapper(attributeTypes);
        final List<OpenElisPatientAttribute> attributes = new ArrayList<OpenElisPatientAttribute>() {{
            add( new OpenElisPatientAttribute("OCCUPATION", "Tailor"));
            add( new OpenElisPatientAttribute("PRIMARYRELATIVE", "Milka Singh"));
        }};

        OpenElisPatient openElisPatient = new OpenElisPatient() {{
            setAttributes(attributes);
        }};
        openElisPatient.setPatientUUID("UUID");
        BahmniPatient bahmniPatient = bahmniPatientMapper.map(openElisPatient);
        assertEquals("UUID", bahmniPatient.getUuid());

    }
}
