package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Person;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.EncounterResource1_9;

import java.util.Set;

@Resource(name = RestConstants.VERSION_1 + "/encounter", supportedClass = Encounter.class, supportedOpenmrsVersions = {"1.9.* - 2.*"},order=2)

public class BahmniEncounterResource extends EncounterResource1_9 {

    @PropertyGetter("provider")
    public static Person getProvider(Encounter encounter) {
        Set<EncounterProvider> encounterProviders  = encounter.getEncounterProviders();
        if (encounterProviders == null || encounterProviders.isEmpty()) {
            return null;
        } else {
            for (EncounterProvider encounterProvider : encounterProviders) {
                // Return the first non-voided provider associated with a person in the list
                if (!encounterProvider.isVoided() && encounterProvider.getProvider().getPerson() != null) {
                    return encounterProvider.getProvider().getPerson();
                }
            }
        }
        return null;
    }
}
