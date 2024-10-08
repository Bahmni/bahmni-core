package org.bahmni.module.elisatomfeedclient.api.worker;

import groovy.lang.GroovyClassLoader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.elisatomfeedclient.api.Constants;
import org.bahmni.module.elisatomfeedclient.api.ElisAtomFeedProperties;
import org.bahmni.module.elisatomfeedclient.api.domain.AccessionDiff;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccession;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisAccessionNote;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.bahmni.module.elisatomfeedclient.api.elisFeedInterceptor.ElisFeedAccessionInterceptor;
import org.bahmni.module.elisatomfeedclient.api.elisFeedInterceptor.ElisFeedEncounterInterceptor;
import org.bahmni.module.elisatomfeedclient.api.exception.OpenElisFeedException;
import org.bahmni.module.elisatomfeedclient.api.mapper.AccessionHelper;
import org.bahmni.webclients.HttpClient;
import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;
import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.auditlog.service.AuditLogService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.impl.BahmniVisitAttributeService;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class OpenElisAccessionEventWorker implements EventWorker {
    public static final String LAB_VISIT = "LAB VISIT";
    public static final String LAB_MANAGER_NOTES = "Lab Manager Notes";
    public static final String LAB_MANAGER_IDENTIFIER = "LABMANAGER";
    public static final String ACCESSION_UUID_CONCEPT = "Accession Uuid";
    private static final String ACCESSION_NOTE_ENCOUNTER_TYPE = "VALIDATION NOTES";
    private static Logger logger = LogManager.getLogger(OpenElisAccessionEventWorker.class);
    private final EncounterHelper encounterHelper;
    private final ProviderHelper providerHelper;
    private ElisAtomFeedProperties atomFeedProperties;
    private HttpClient httpClient;
    private EncounterService encounterService;
    private ConceptService conceptService;
    private AccessionHelper accessionHelper;
    private ProviderService providerService;
    private BahmniVisitAttributeService bahmniVisitAttributeSaveCommand;
    private AuditLogService auditLogService;


    //TODO : add the new service classes to bean initialization
    public OpenElisAccessionEventWorker(ElisAtomFeedProperties atomFeedProperties,
                                        HttpClient httpClient,
                                        EncounterService encounterService,
                                        ConceptService conceptService,
                                        AccessionHelper accessionHelper,
                                        ProviderService providerService,
                                        BahmniVisitAttributeService bahmniVisitAttributeSaveCommand,
                                        AuditLogService auditLogService) {

        this.atomFeedProperties = atomFeedProperties;
        this.httpClient = httpClient;
        this.encounterService = encounterService;
        this.conceptService = conceptService;
        this.accessionHelper = accessionHelper;
        this.providerService = providerService;
        this.bahmniVisitAttributeSaveCommand = bahmniVisitAttributeSaveCommand;
        this.encounterHelper = new EncounterHelper(encounterService);
        this.providerHelper = new ProviderHelper(providerService);
        this.auditLogService = auditLogService;
    }

    @Override
    public void process(Event event) {
        String accessionUrl = atomFeedProperties.getOpenElisUri() + event.getContent();
        logger.info("Processing event : {}", accessionUrl);
        try {
            OpenElisAccession openElisAccession = httpClient.get(accessionUrl, OpenElisAccession.class);
            if (accessionHelper.shouldIgnoreAccession(openElisAccession)) {
                logger.warn(String.format("Ignoring accession event. Patient with UUID %s is not present in OpenMRS.", openElisAccession.getPatientUuid()));
                return;
            }
            runInterceptor(ElisFeedAccessionInterceptor.class, openElisAccession);

            for(OpenElisTestDetail openElisTestDetail : openElisAccession.getTestDetails()) {
                if(openElisTestDetail.getTestUuid() == null) {
                    throw new RuntimeException("Concept for lab test'"+openElisTestDetail.getTestName()+"' not found in openmrs");
                }
            }
            Encounter orderEncounter = encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid());

            boolean shouldSaveOrderEncounter = false;
            if (orderEncounter != null) {
                AccessionDiff diff = openElisAccession.getDiff(orderEncounter);
                if (diff.hasDifference()) {
                    logger.info("updating encounter for accession : {}" , accessionUrl);
                    accessionHelper.addOrDiscontinueOrderDifferences(openElisAccession, diff, orderEncounter);
                    shouldSaveOrderEncounter = true;
                }
            } else {
                logger.info("creating new encounter for accession : {}" , accessionUrl);
                orderEncounter = accessionHelper.mapToNewEncounter(openElisAccession, LAB_VISIT);
                shouldSaveOrderEncounter = true;
            }

            if (shouldSaveOrderEncounter) {
                //will save new visit as well
                Encounter encounter = encounterService.saveEncounter(orderEncounter);
                bahmniVisitAttributeSaveCommand.save(encounter);
                logEncounter(encounter);
            }
            if (openElisAccession.getAccessionNotes() != null && !openElisAccession.getAccessionNotes().isEmpty()) {
                processAccessionNotes(openElisAccession, orderEncounter);
            }
            Set<Encounter> updatedEncounters = associateTestResultsToOrder(openElisAccession);
            runInterceptor(ElisFeedEncounterInterceptor.class, updatedEncounters);

            saveUpdatedEncounters(updatedEncounters);
        } catch (IOException e) {
            logger.error("openelisatomfeedclient:error processing event : {} {} {}", accessionUrl , e.getMessage(), e);
            throw new OpenElisFeedException("could not read accession data", e);
        } catch (ParseException pe) {
            logger.error("openelisatomfeedclient:error processing lab results. Invalid result data type : " + accessionUrl + pe.getMessage(), pe);
            throw new OpenElisFeedException("could not read accession data. Invalid result data type.", pe);
        }
    }

    void runInterceptor(Class className, Object object) {
        GroovyClassLoader gcl = new GroovyClassLoader();
        String applicationDataDirectory = OpenmrsUtil.getApplicationDataDirectory();
        if (!applicationDataDirectory.endsWith(File.separator)) {
            applicationDataDirectory += File.separator;
        }
        File directory = new File(applicationDataDirectory + "elisFeedInterceptor");
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                Class clazz;
                try {
                    clazz = gcl.parseClass(file);
                    if (className.equals(ElisFeedEncounterInterceptor.class)
                            && ElisFeedEncounterInterceptor.class.isAssignableFrom(clazz)) {
                        logger.info("BahmniEncounterTransactionUpdateAdvice : Using rules in {}" , clazz.getName());
                        ElisFeedEncounterInterceptor elisFeedEncounterInterceptor = (ElisFeedEncounterInterceptor) clazz.newInstance();
                        Set<Encounter> encounters = (HashSet<Encounter>) object;
                        elisFeedEncounterInterceptor.run(encounters);
                        logger.info("BahmniEncounterTransactionUpdateAdvice : Done");
                    } else if (className.equals(ElisFeedAccessionInterceptor.class)
                            && ElisFeedAccessionInterceptor.class.isAssignableFrom(clazz)) {
                        logger.info("BahmniEncounterTransactionUpdateAdvice : Using rules in {}" , clazz.getName());
                        ElisFeedAccessionInterceptor elisFeedAccessionInterceptor = (ElisFeedAccessionInterceptor) clazz.newInstance();
                        elisFeedAccessionInterceptor.run((OpenElisAccession) object);
                        logger.info("BahmniEncounterTransactionUpdateAdvice : Done");
                    }
                } catch (IOException | IllegalAccessException | InstantiationException e) {
                    logger.error(e);
                }
            }
        }

    }

    private void saveUpdatedEncounters(Set<Encounter> updatedEncounters) {
        for (Encounter updatedEncounter : updatedEncounters) {
            Encounter savedEncounter = encounterService.saveEncounter(updatedEncounter);
            logEncounter(savedEncounter);
        }
    }

    private void logEncounter(Encounter savedEncounter) {
        Boolean isAuditLogEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("bahmni.enableAuditLog"));
        if (isAuditLogEnabled) {
            Map<String, String> params = new HashMap<>();
            params.put("encounterUuid", savedEncounter.getUuid());
            params.put("encounterType", savedEncounter.getEncounterType().getName());
            auditLogService.createAuditLog(savedEncounter.getPatient().getUuid(), "EDIT_ENCOUNTER", "EDIT_ENCOUNTER_MESSAGE", params, "OpenElis");
        }
    }

    private void processAccessionNotes(OpenElisAccession openElisAccession, Encounter orderEncounter) throws ParseException {

        EncounterType labNotesEncounterType = getLabNotesEncounterType();
        Provider defaultLabManagerProvider = providerService.getProviderByIdentifier(LAB_MANAGER_IDENTIFIER);

        Concept labNotesConcept = getLabNotesConcept();
        Concept accessionConcept = getAccessionConcept();
        Set<Encounter> encountersForAccession = encounterHelper.getEncountersForAccession(openElisAccession.getAccessionUuid(), labNotesEncounterType, orderEncounter.getVisit());
        AccessionDiff accessionNoteDiff = openElisAccession.getAccessionNoteDiff(encountersForAccession, labNotesConcept, defaultLabManagerProvider);
        if (accessionNoteDiff.hasDifferenceInAccessionNotes()) {
            for (OpenElisAccessionNote note : accessionNoteDiff.getAccessionNotesToBeAdded()) {
                Encounter noteEncounter = getEncounterForNote(note, encountersForAccession, labNotesEncounterType, orderEncounter);
                if (!encounterHelper.hasObservationWithText(openElisAccession.getAccessionUuid(), noteEncounter)) {
                    noteEncounter.addObs(createObsWith(openElisAccession.getAccessionUuid(), accessionConcept, note.getDateTimeAsDate()));
                }
                noteEncounter.addObs(createObsWith(note.getNote(), labNotesConcept, note.getDateTimeAsDate()));
                Encounter newEncounter = encounterService.saveEncounter(noteEncounter);
                logEncounter(newEncounter);
                encountersForAccession.add(newEncounter);
            }
        }
    }

    private Encounter getEncounterForNote(OpenElisAccessionNote note, Set<Encounter> encountersForAccession, EncounterType encounterType, Encounter orderEncounter) {
        Provider provider = providerHelper.getProviderByUuidOrReturnDefault(note.getProviderUuid(), LAB_MANAGER_IDENTIFIER);
        Encounter encounterWithDefaultProvider = null;

        if (encountersForAccession != null) {
            for (Encounter encounter : encountersForAccession) {
                if (note.isProviderInEncounter(encounter)) {
                    return encounter;
                } else if (ProviderHelper.getProviderFrom(encounter).equals(provider)) {
                    encounterWithDefaultProvider = encounter;
                }
            }
        }
        return encounterWithDefaultProvider != null ? encounterWithDefaultProvider : encounterHelper.createNewEncounter(orderEncounter.getVisit(), encounterType, orderEncounter.getEncounterDatetime(), orderEncounter.getPatient(), provider, orderEncounter.getLocation());
    }

    private Concept getAccessionConcept() {
        return conceptService.getConcept(ACCESSION_UUID_CONCEPT);
    }

    private Concept getLabNotesConcept() {
        return conceptService.getConcept(LAB_MANAGER_NOTES);
    }

    private EncounterType getLabNotesEncounterType() {
        return encounterService.getEncounterType(ACCESSION_NOTE_ENCOUNTER_TYPE);
    }

    private Obs createObsWith(String textValue, Concept concept, Date obsDateTime) {
        Obs observation = new Obs();
        observation.setConcept(concept);
        observation.setValueText(textValue);
        observation.setObsDatetime(obsDateTime);
        return observation;
    }

    protected Set<Encounter> associateTestResultsToOrder(OpenElisAccession openElisAccession) throws ParseException {
        Encounter orderEncounter = encounterService.getEncounterByUuid(openElisAccession.getAccessionUuid());

        if (!orderEncounter.getOrders().isEmpty()) {
            for (OpenElisTestDetail testDetail : openElisAccession.getTestDetails()) {
                if (testDetail == null || testDetail.getStatus() == null) {
                    continue;
                }
                for (Order order : orderEncounter.getOrders()) {
                    if (testDetail.getTestUuid().equals(order.getConcept().getUuid()) || order.getConcept().getUuid().equals(testDetail.getPanelUuid())) {
                        if ("Canceled".equals(testDetail.getStatus())) {
                            continue;
                        }
                        order.setAccessionNumber(openElisAccession.getAccessionUuid());
                    }
                }
            }
        }

        final EncounterType labResultEncounterType = getLabResultEncounterType();
        final Set<OpenElisTestDetail> allTests = openElisAccession.getTestDetails();

        List<Encounter> labResultEncounters = findVisitEncountersOfType(orderEncounter.getVisit(), labResultEncounterType);
        HashSet<Encounter> resultEncounters = new HashSet<>(labResultEncounters);
        Set<Encounter> updatedEncounters = new HashSet<>();
        ResultObsHelper resultObsHelper = new ResultObsHelper(conceptService);
        List<Provider> labResultProviders = new ArrayList<>();
        Visit resultVisit = orderEncounter.getVisit();
        for (OpenElisTestDetail testDetail : allTests) {
            if (testDetail.isReferredOut() && StringUtils.isBlank(testDetail.getDateTime())) {
                testDetail.setDateTime(openElisAccession.getDateTime());
            }
            if (StringUtils.isNotBlank(testDetail.getDateTime())) {
                Order testOrder = identifyOrder(orderEncounter, testDetail);
                Encounter resultEncounterForTest = identifyResultEncounter(resultEncounters, testDetail, testOrder);
                Provider testProvider = getProviderForResults(labResultProviders, testDetail.getProviderUuid());
                boolean isResultUpdated = true;

                Date testDate = DateTime.parse(testDetail.getDateTime()).toDate();
                if (resultEncounterForTest != null) {
                    Obs prevObs = identifyResultObs(resultEncounterForTest, testDetail, testOrder);
                    isResultUpdated = prevObs.getObsDatetime().getTime() < testDate.getTime();
                    if (isResultUpdated) {
                        resultObsHelper.voidObs(prevObs, testDate);
                    }
                }

                if (isResultUpdated) {
                    resultEncounterForTest = encounterHelper.findOrInitializeEncounter(resultVisit, testProvider,
                            labResultEncounterType, orderEncounter.getEncounterDatetime(), orderEncounter.getLocation());
                    resultEncounterForTest.addObs(resultObsHelper.createNewObsForOrder(testDetail, testOrder, resultEncounterForTest));
                    resultVisit.addEncounter(resultEncounterForTest);
                    updatedEncounters.add(resultEncounterForTest);
                    labResultEncounters.add(resultEncounterForTest);
                }
            }
        }
        return updatedEncounters;
    }

    private List<Encounter> findVisitEncountersOfType(Visit visit, EncounterType encounterType) {
        List<Encounter> encounters = new ArrayList<>();
        for (Encounter encounter : visit.getEncounters()) {
            if (encounter.getEncounterType().equals(encounterType)) {
                encounters.add(encounter);
            }
        }
        return encounters;
    }

    private EncounterType getLabResultEncounterType() {
        return encounterService.getEncounterType(Constants.DEFAULT_LAB_RESULT_ENCOUNTER_TYPE);
    }

    /**
     * For a given test/panel result, there ought to be only one encounter containing non voided corresponding observation
     *
     * @param labResultEncounters
     * @param testDetail
     * @param testOrder
     * @return
     */
    private Encounter identifyResultEncounter(HashSet<Encounter> labResultEncounters, OpenElisTestDetail testDetail, Order testOrder) {
        for (Encounter encounter : labResultEncounters) {
            final Obs resultObs = identifyResultObs(encounter, testDetail, testOrder);
            if (resultObs != null) {
                return encounter;
            }
        }
        return null;
    }

    /**
     * This method currenly checks at the topLevel Obs.
     * if its a panel, then it goes through the next level and identifes a test by the concept at the next level
     * If its a test, then it just checks at the top level concept
     * <p/>
     * However, for future multi-value tests, in both the cases (panel and indiv test), it would need go to one more
     * level down and return the matching observation.
     *
     * @param resultEncounter
     * @param testDetail
     * @param testOrder
     * @return
     */
    private Obs identifyResultObs(Encounter resultEncounter, OpenElisTestDetail testDetail, Order testOrder) {
        boolean isPanel = StringUtils.isNotBlank(testDetail.getPanelUuid());
        final Set<Obs> obsAtTopLevel = resultEncounter.getObsAtTopLevel(false);
        for (Obs obs : obsAtTopLevel) {
            if (isPanel && obs.getConcept().getUuid().equals(testDetail.getPanelUuid()) && obs.getOrder().getId().equals(testOrder.getId())) {
                for (Obs member : obs.getGroupMembers()) {
                    if (member.getConcept().getUuid().equals(testDetail.getTestUuid())
                            && member.getOrder().getId().equals(testOrder.getId())) {
                        return member;
                    }
                }
            } else if (obs.getConcept().getUuid().equals(testDetail.getTestUuid())
                    && obs.getOrder().getId().equals(testOrder.getId())) {
                return obs;
            }
        }
        return null;
    }

    private Order identifyOrder(Encounter orderEncounter, OpenElisTestDetail testDetail) {
        for (Order order : orderEncounter.getOrders()) {
            String testConceptUuid = StringUtils.isBlank(testDetail.getPanelUuid()) ? testDetail.getTestUuid() : testDetail.getPanelUuid();
            if (order.getConcept() != null && order.getConcept().getUuid().equals(testConceptUuid)) {
                return order;
            }
        }
        return null; //this should never be the case.
    }

    private Provider getProviderForResults(List<Provider> labResultProviders, String providerUuid) {
        for (Provider labResultProvider : labResultProviders) {
            if (labResultProvider.getUuid().equals(providerUuid)) {
                return labResultProvider;
            }
        }

        Provider provider = null;
        if (StringUtils.isNotBlank(providerUuid)) {
            provider = providerService.getProviderByUuid(providerUuid);
        }

        //the lab results provider may not be register as provider in MRS,
        //hence instead of failing, toString the system provider
        if (provider == null) {
            provider = providerService.getProviderByIdentifier(Constants.DEFAULT_LAB_SYSTEM_IDENTIFIER);
        }

        labResultProviders.add(provider);
        return provider;
    }

    @Override
    public void cleanUp(Event event) {

    }
}
