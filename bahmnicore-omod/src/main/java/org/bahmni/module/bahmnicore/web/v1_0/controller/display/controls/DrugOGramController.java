package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOGram/regimen")
public class DrugOGramController {

    private BahmniDrugOrderService bahmniDrugOrderService;
    private DrugOrderToRegimenMapper drugOrderToTreatmentRegimenMapper;
    private BahmniConceptService bahmniConceptService;
    private BahmniExtensions bahmniExtensions;

    @Autowired
    public DrugOGramController(BahmniDrugOrderService bahmniDrugOrderService, DrugOrderToRegimenMapper drugOrderToTreatmentRegimenMapper, BahmniConceptService bahmniConceptService, BahmniExtensions bahmniExtensions) {
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.drugOrderToTreatmentRegimenMapper = drugOrderToTreatmentRegimenMapper;
        this.bahmniConceptService = bahmniConceptService;
        this.bahmniExtensions = bahmniExtensions;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public TreatmentRegimen getRegimen(@RequestParam(value = "patientUuid", required = true) String patientUuid,
                                       @RequestParam(value = "patientProgramUuid", required = false) String patientProgramUuid,
                                       @RequestParam(value = "drugs", required = false) List<String> drugs) throws ParseException {
        Set<Concept> conceptsForDrugs = getConceptsForDrugs(drugs);


        List<Order> allDrugOrders = bahmniDrugOrderService.getAllDrugOrders(patientUuid, patientProgramUuid, conceptsForDrugs,null, null);
        if (!CollectionUtils.isEmpty(conceptsForDrugs)) {
            conceptsForDrugs = filterConceptsForDrugOrders(conceptsForDrugs, allDrugOrders);
        }
        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(allDrugOrders, conceptsForDrugs);
        BaseTableExtension<TreatmentRegimen> extension = (BaseTableExtension<TreatmentRegimen>) bahmniExtensions.getExtension("treatmentRegimenExtension"
                , "TreatmentRegimenExtension.groovy");
        if (extension != null)
            extension.update(treatmentRegimen, patientUuid, patientProgramUuid);
        return treatmentRegimen;
    }

    private Set<Concept> filterConceptsForDrugOrders(Set<Concept> conceptsForDrugs, List<Order> allDrugOrders) {
        Set<Concept> drugConcepts = new LinkedHashSet<>();
        for (Concept conceptsForDrug : conceptsForDrugs) {
            for (Order drugOrder : allDrugOrders) {
                if (conceptsForDrug.equals(drugOrder.getConcept()) && !drugConcepts.contains(conceptsForDrug)) {
                    drugConcepts.add(conceptsForDrug);
                }
            }
        }
        return drugConcepts;
    }

    private Set<Concept> getConceptsForDrugs(List<String> drugs) {
        if (drugs == null) return null;
        Set<Concept> drugConcepts = new LinkedHashSet<>();
        for (String drug : drugs) {
            Concept concept = bahmniConceptService.getConceptByFullySpecifiedName(drug);
            getDrugs(concept, drugConcepts);
        }
        return drugConcepts;
    }

    private void getDrugs(Concept concept, Set<Concept> drugConcepts) {
        if (concept == null) {
            return;
        }
        if (concept.isSet()) {
            for (Concept drugConcept : concept.getSetMembers()) {
                getDrugs(drugConcept, drugConcepts);
            }
        } else {
            drugConcepts.add(concept);
        }
    }
}
