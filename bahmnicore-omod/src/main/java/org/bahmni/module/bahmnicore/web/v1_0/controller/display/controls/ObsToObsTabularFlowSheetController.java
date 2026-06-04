package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniFormBuilderObsToTabularViewMapper;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniObservationsToTabularViewMapper;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/observations/flowSheet")
public class ObsToObsTabularFlowSheetController {

    public static final String CONCEPT_DETAILS = "Concept Details";
    private BahmniObsService bahmniObsService;
    private ConceptService conceptService;
    private BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper;
    private BahmniConceptService bahmniConceptService;
    private BahmniFormBuilderObsToTabularViewMapper bahmniFormBuilderObsToTabularViewMapper;
    private ConceptMapper conceptMapper;
    private BahmniExtensions bahmniExtensions;
    public static final String FLOWSHEET_EXTENSION = "flowsheetExtension";

    private static Logger logger = LogManager.getLogger(ObsToObsTabularFlowSheetController.class);

    @Autowired
    public ObsToObsTabularFlowSheetController(BahmniObsService bahmniObsService, ConceptService conceptService,
                                              BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper,
                                              BahmniExtensions bahmniExtensions,
                                              BahmniConceptService bahmniConceptService,
                                              BahmniFormBuilderObsToTabularViewMapper bahmniFormBuilderObsToTabularViewMapper) {
        this.bahmniObsService = bahmniObsService;
        this.conceptService = conceptService;
        this.bahmniObservationsToTabularViewMapper = bahmniObservationsToTabularViewMapper;
        this.conceptMapper = new ConceptMapper();
        this.bahmniExtensions = bahmniExtensions;
        this.bahmniConceptService = bahmniConceptService;
        this.bahmniFormBuilderObsToTabularViewMapper = bahmniFormBuilderObsToTabularViewMapper;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public PivotTable constructPivotTableFor(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
            @RequestParam(value = "conceptSet", required = false) String conceptSet,
            @RequestParam(value = "groupByConcept", required = true) String groupByConcept,
            @RequestParam(value = "orderByConcept", required = false) String orderByConcept,
            @RequestParam(value = "conceptNames", required = false) List<String> conceptNames,
            @RequestParam(value = "initialCount", required = false) Integer initialCount,
            @RequestParam(value = "latestCount", required = false) Integer latestCount,
            @RequestParam(value = "name", required = false) String groovyExtension,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "enrollment", required = false) String patientProgramUuid,
            @RequestParam(value = "formNames", required = false) List<String> formNames) throws ParseException {

        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);

        PivotTable pivotTable;
        if (conceptSet != null) {
            pivotTable = getPivotTableByConceptSet(patientUuid, numberOfVisits, conceptSet, groupByConcept,
                    conceptNames, initialCount, latestCount, startDate, endDate, patientProgramUuid);
        } else {
            pivotTable = getPivotTableByFormNames(patientUuid, numberOfVisits, groupByConcept, conceptNames,
                    initialCount, latestCount, startDate, endDate, patientProgramUuid, formNames);
        }
        setNormalRangeAndUnits(pivotTable.getHeaders());

        if(orderByConcept != null) {
            orderPivotTableByGivenConcept(pivotTable, orderByConcept);
        }
        if(StringUtils.isEmpty(groovyExtension)){
            return pivotTable;
        }

        BaseTableExtension<PivotTable> extension = (BaseTableExtension<PivotTable>) bahmniExtensions.getExtension(FLOWSHEET_EXTENSION, groovyExtension + BahmniExtensions.GROOVY_EXTENSION);
        if (extension != null)
            extension.update(pivotTable, patientUuid, patientProgramUuid);
        return pivotTable;
    }

    private PivotTable getPivotTableByConceptSet(String patientUuid, Integer numberOfVisits, String conceptSet,
                                                 String groupByConcept, List<String> conceptNames,
                                                 Integer initialCount, Integer latestCount, Date startDate,
                                                 Date endDate, String patientProgramUuid) {
        Concept rootConcept = conceptService.getConceptByName(conceptSet);
        Concept childConcept = conceptService.getConceptByName(groupByConcept);
        validate(conceptSet, groupByConcept, rootConcept, childConcept);

        Collection<BahmniObservation> bahmniObservations = bahmniObsService.observationsFor(
                patientUuid, rootConcept, childConcept, numberOfVisits, startDate, endDate, patientProgramUuid);

        Set<EncounterTransaction.Concept> leafConcepts = new LinkedHashSet<>();
        if (CollectionUtils.isEmpty(conceptNames)) {
            getAllLeafConcepts(rootConcept, leafConcepts);
        } else {
            getSpecifiedLeafConcepts(rootConcept, conceptNames, leafConcepts);
        }
        if (!CollectionUtils.isEmpty(conceptNames)) {
            leafConcepts = sortConcepts(conceptNames, leafConcepts);
        }
        if (conceptNames != null && !conceptNames.contains(groupByConcept)) {
            leafConcepts.add(conceptMapper.map(childConcept));
        }
        bahmniObservations = filterDataByCount(bahmniObservations, initialCount, latestCount);
        return bahmniObservationsToTabularViewMapper.constructTable(leafConcepts, bahmniObservations, groupByConcept);
    }

    private PivotTable getPivotTableByFormNames(String patientUuid, Integer numberOfVisits, String groupByConceptName,
                                                List<String> conceptNames, Integer initialCount, Integer latestCount,
                                                Date startDate, Date endDate, String patientProgramUuid,
                                                List<String> formNames) {
        if (isNull(conceptNames) || isNull(formNames) || formNames.size() < 1) {
            logger.warn("Form name(s) and concept name(s) are required for forms 2.0");
            return new PivotTable();
        }
        Collection<BahmniObservation> bahmniObservations = bahmniObsService.getObsForFormBuilderForms(patientUuid,
                formNames, numberOfVisits, startDate, endDate, patientProgramUuid);
        conceptNames.add(groupByConceptName);
        Set<EncounterTransaction.Concept> leafConcepts =
                bahmniConceptService.getConceptsByFullySpecifiedName(conceptNames)
                        .stream().map(concept -> conceptMapper.map(concept)).collect(Collectors.toCollection(LinkedHashSet::new));
        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(leafConcepts,
                bahmniObservations, groupByConceptName);
        List<PivotRow> rows = (List<PivotRow>) filterDataByCount(pivotTable.getRows(), initialCount, latestCount);
        pivotTable.setRows(bahmniFormBuilderObsToTabularViewMapper.getNonEmptyRows(rows, groupByConceptName));
        return pivotTable;
    }

    private void setNormalRangeAndUnits(Set<EncounterTransaction.Concept> headers) {
        for (EncounterTransaction.Concept header : headers) {
            if (CONCEPT_DETAILS.equals(header.getConceptClass())) {
                List<Concept> setMembers = conceptService.getConceptsByConceptSet(conceptService.getConceptByUuid(header.getUuid()));
                Concept primaryConcept = getNumeric(setMembers);
                if (primaryConcept == null) continue;
                header.setHiNormal(getHiNormal(primaryConcept));
                header.setLowNormal(getLowNormal(primaryConcept));
                header.setUnits(getUnits(primaryConcept));
            }
        }
    }

    private String getUnits(Concept primaryConcept) {
        return conceptService.getConceptNumeric(primaryConcept.getConceptId()).getUnits();
    }

    private Double getLowNormal(Concept primaryConcept) {
        return conceptService.getConceptNumeric(primaryConcept.getConceptId()).getLowNormal();
    }

    private Double getHiNormal(Concept primaryConcept) {
        return conceptService.getConceptNumeric(primaryConcept.getConceptId()).getHiNormal();
    }

    private Concept getNumeric(List<Concept> setMembers) {
        for (Concept setMember : setMembers) {
            if (setMember.getDatatype().isNumeric()) {
                return setMember;
            }
        }
        return null;
    }

    private Set<EncounterTransaction.Concept> sortConcepts(List<String> conceptNames, Set<EncounterTransaction.Concept> leafConcepts) {
        Set<EncounterTransaction.Concept> sortedConcepts = new LinkedHashSet<>();
        for (String conceptName : conceptNames) {
            for (EncounterTransaction.Concept leafConcept : leafConcepts) {
                if (conceptName.equals(leafConcept.getName())) {
                    sortedConcepts.add(leafConcept);
                }
            }
        }
        return sortedConcepts;
    }

    private <T> Collection<T> filterDataByCount(Collection<T> observations, Integer initialCount,
                                                            Integer latestCount) {
        if (initialCount == null && latestCount == null) return observations;
        Collection<T> observationCollection = new ArrayList<>();

        if (observations.size() < (getIntegerValue(initialCount) + getIntegerValue(latestCount))) {
            latestCount = observations.size();
            initialCount = 0;
        }
        observationCollection.addAll(filter(observations, 0, getIntegerValue(initialCount)));
        observationCollection.addAll(filter(observations, observations.size() - getIntegerValue(latestCount), observations.size()));

        return observationCollection;
    }

    private <T> Collection<T> filter(Collection<T> observations, Integer fromIndex, Integer toIndex) {
        Collection<T> observationCollection = new ArrayList<>();
        fromIndex = (fromIndex > observations.size() || fromIndex < 0) ? 0 : fromIndex;
        toIndex = (toIndex > observations.size()) ? observations.size() : toIndex;
        for (int index = fromIndex; index < toIndex; index++) {
            observationCollection.add((T) CollectionUtils.get(observations, index));
        }
        return observationCollection;
    }

    private int getIntegerValue(Integer value) {
        if (value == null) return 0;
        return value;
    }

    private void getSpecifiedLeafConcepts(Concept rootConcept, List<String> conceptNames, Set<EncounterTransaction.Concept> leafConcepts) {
        for (Concept concept : rootConcept.getSetMembers()) {
            if (conceptNames.contains(concept.getName().getName())) {
                getAllLeafConcepts(concept, leafConcepts);
            } else {
                getSpecifiedLeafConcepts(concept, conceptNames, leafConcepts);
            }
        }
    }

    private void getAllLeafConcepts(Concept rootConcept, Set<EncounterTransaction.Concept> leafConcepts) {
        if (!rootConcept.isSet() || rootConcept.getConceptClass().getName().equals(CONCEPT_DETAILS)) {
            leafConcepts.add(conceptMapper.map(rootConcept));
        } else {
            for (Concept concept : rootConcept.getSetMembers()) {
                getAllLeafConcepts(concept, leafConcepts);
            }
        }
    }

    private void validate(String conceptSet, String groupByConcept, Concept rootConcept, Concept childConcept) {
        if (rootConcept == null) {
            logger.error("Root concept not found for the name: {} ", conceptSet);
            throw new RuntimeException("Root concept not found for the name:  " + conceptSet);
        }

        if (!rootConcept.getSetMembers().contains(childConcept)) {
            logger.error("GroupByConcept: {} doesn't belong to the Root concept: {}  ", groupByConcept, conceptSet);
            throw new RuntimeException("GroupByConcept: " + groupByConcept + " doesn't belong to the Root concept:  " + conceptSet);
        }
    }

    private void orderPivotTableByGivenConcept(PivotTable pivotTable, String orderByConcept) throws ParseException {
        TreeMap<Object, List<PivotRow>> orderConceptToPivotRowMap = new TreeMap<>();
        List<String> allowedDataTypesForOrdering = Arrays.asList("Date", "Numeric", "Coded", "Text");
        Concept orderConcept = conceptService.getConceptByName(orderByConcept);
        if(allowedDataTypesForOrdering.contains(orderConcept.getDatatype().getName())) {
            List<PivotRow> orderedRows = new ArrayList<>();
            for(PivotRow pivotRow : pivotTable.getRows()) {
                List<BahmniObservation> bahmniObservations = pivotRow.getColumns().get(orderByConcept);
                Object value = null;
                if(CollectionUtils.isEmpty(bahmniObservations)) {
                    value = getValueForNull(orderConcept.getDatatype().getName());
                } else {
                    value = getValue(bahmniObservations.get(0));
                }
                if(orderConceptToPivotRowMap.containsKey(value)) {
                    orderConceptToPivotRowMap.get(value).add(pivotRow);
                } else {
                    List<PivotRow> pivotRows = new ArrayList<>();
                    pivotRows.add(pivotRow);
                    orderConceptToPivotRowMap.put(value, pivotRows);
                }
            }
            for(Entry<Object, List<PivotRow>> entry : orderConceptToPivotRowMap.entrySet()) {
                orderedRows.addAll(entry.getValue());
            }
            pivotTable.setRows(orderedRows);
        }
    }

    private Object getValue(BahmniObservation bahmniObservation) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        if("Date".equals(bahmniObservation.getType())) {
            return simpleDateFormat.parse(bahmniObservation.getValueAsString());
        } else if("Coded".equals(bahmniObservation.getType())) {
            return ((EncounterTransaction.Concept)bahmniObservation.getValue()).getName();
        } else if("Text".equals(bahmniObservation.getType())) {
            return bahmniObservation.getValue();
        } else if("Numeric".equals(bahmniObservation.getType())) {
            return bahmniObservation.getValue();
        }
        return null;
    }

    private Object getValueForNull(String type) {
        if ("Date".equals(type)) {
            return new Date();
        } else if ("Numeric".equals(type)) {
            return new Double(0);
        } else {
            return "";
        }
    }
}
