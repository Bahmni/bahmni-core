package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@org.openmrs.module.webservices.rest.web.annotation.Resource(name = RestConstants.VERSION_1 + "/obs", supportedClass = Obs.class, supportedOpenmrsVersions = {"1.10.* - 2.*"}, order = 0)
public class BahmniObsResource extends ObsResource1_11 {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription representationDescription = super.getRepresentationDescription(rep);
        if (representationDescription == null && rep instanceof NamedRepresentation && rep.getRepresentation().equals("visitFormDetails")) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("concept", new NamedRepresentation("bahmniAnswer"));
            description.addProperty("display");
            description.addProperty("obsDatetime");
            description.addProperty("visitUuid");
            description.addProperty("encounterUuid");
            description.addProperty("visitStartDateTime");
            description.addProperty("providerList");
            return description;
        }
        return representationDescription;
    }

    @PropertyGetter("visitUuid")
    public String getvisitUuid(Obs obs) {
        Encounter encounter = obs.getEncounter();
        Visit visit = encounter.getVisit();
        return visit.getUuid();
    }

    @PropertyGetter("visitStartDateTime")
    public Date getVisitDateTime(Obs obs) {
        Encounter encounter = obs.getEncounter();
        Visit visit = encounter.getVisit();
        return visit.getStartDatetime();
    }

    @PropertyGetter("encounterUuid")
    public String getEncounterUuid(Obs obs) {
        Encounter encounter = obs.getEncounter();
        return encounter.getUuid();
    }

    @PropertyGetter("providerList")
    public List<EncounterTransaction.Provider> getProviderList(Obs obs) {
        List<EncounterTransaction.Provider> providerList = new ArrayList<>();
        BahmniProviderMapper bahmniProviderMapper = new BahmniProviderMapper();
        for (EncounterProvider encounterProvider : obs.getEncounter().getEncounterProviders()) {
            providerList.add(bahmniProviderMapper.map(encounterProvider.getProvider()));
        }
        return providerList;
    }
}

