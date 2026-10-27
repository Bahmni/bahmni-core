package org.openmrs.module.bahmniemrapi.disposition.service;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Visit;
import org.openmrs.api.OrderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.bahmniemrapi.disposition.mapper.BahmniDispositionMapper;
import org.openmrs.module.emrapi.encounter.DispositionMapper;
import org.openmrs.module.emrapi.encounter.EncounterProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BahmniDispositionServiceImpl implements BahmniDispositionService {

    private static final String ADT_ORDER_TYPE_NAME = "ADT Order";

    private VisitService visitService;

    private DispositionMapper dispositionMapper;

    private ObservationTypeMatcher observationTypeMatcher;

    private EncounterProviderMapper encounterProviderMapper;

    private BahmniDispositionMapper bahmniDispositionMapper;

    private OrderService orderService;

    @Autowired
    public BahmniDispositionServiceImpl(VisitService visitService, DispositionMapper dispositionMapper,
                                        ObservationTypeMatcher observationTypeMatcher, EncounterProviderMapper encounterProviderMapper,
                                        BahmniDispositionMapper bahmniDispositionMapper, OrderService orderService){
        this.visitService = visitService;
        this.dispositionMapper = dispositionMapper;
        this.observationTypeMatcher = observationTypeMatcher;
        this.encounterProviderMapper = encounterProviderMapper;
        this.bahmniDispositionMapper = bahmniDispositionMapper;
        this.orderService = orderService;
    }

    @Override
    public List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid , Locale locale) {
        Assert.notNull(visitUuid);

        Visit visit = visitService.getVisitByUuid(visitUuid);

        if(visit == null){
            return new ArrayList<>();
        }

        return getDispositionByVisit(visit, locale);
    }
    @Override
    public List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid) {
        Assert.notNull(visitUuid);

        Visit visit = visitService.getVisitByUuid(visitUuid);

        if(visit == null){
            return new ArrayList<>();
        }

        return getDispositionByVisit(visit , new Locale("en"));
    }

    public List<BahmniDisposition> getDispositionByVisits(List<Visit> visits , Locale locale){
        List<BahmniDisposition> dispositions = new ArrayList<>();

        for(Visit visit: visits){
            dispositions.addAll(getDispositionByVisit(visit , locale));
        }

        return  dispositions;
    }
    public List<BahmniDisposition> getDispositionByVisits(List<Visit> visits){
        List<BahmniDisposition> dispositions = new ArrayList<>();

        for(Visit visit: visits){
            dispositions.addAll(getDispositionByVisit(visit , new Locale("en")));
        }

        return  dispositions;
    }
    private List<BahmniDisposition> getDispositionByVisit(Visit visit , Locale locale) {
        List<BahmniDisposition> dispositions = new ArrayList<>();
        for (Encounter encounter : visit.getEncounters()) {
            Set<Obs> observations = encounter.getObsAtTopLevel(false);
            Set<EncounterTransaction.Provider> eTProvider = encounterProviderMapper.convert(encounter.getEncounterProviders());

            int dispositionsBefore = dispositions.size();
            for (Obs observation : observations) {
                if(ObservationTypeMatcher.ObservationType.DISPOSITION.equals(observationTypeMatcher.getObservationType(observation))){
                    addBahmniDisposition(dispositions, eTProvider, observation , locale);
                }
            }

            // Old clinic writes the Disposition Observation; new clinic writes an ADT Order
            // instead. Only fall back to the order when this encounter has no disposition
            // observation, so an encounter that still dual-writes both isn't reported twice.
            if (dispositions.size() == dispositionsBefore) {
                addBahmniDispositionsFromOrders(dispositions, encounter, eTProvider, locale);
            }
        }
        return dispositions;
    }

    private void addBahmniDispositionsFromOrders(List<BahmniDisposition> dispositions, Encounter encounter,
                                                  Set<EncounterTransaction.Provider> eTProvider, Locale locale) {
        OrderType adtOrderType = orderService.getOrderTypeByName(ADT_ORDER_TYPE_NAME);
        if (adtOrderType == null || encounter.getOrders() == null) {
            return;
        }
        for (Order order : encounter.getOrders()) {
            if (order.getOrderType() != null && order.isType(adtOrderType)) {
                dispositions.add(bahmniDispositionMapper.mapFromOrder(order, eTProvider, locale));
            }
        }
    }

    private void addBahmniDisposition(List<BahmniDisposition> dispositions,
                                      Set<EncounterTransaction.Provider> eTProvider,
                                      Obs observation ,
                                      Locale locale) {
        EncounterTransaction.Disposition eTDisposition = dispositionMapper.getDisposition(observation);
        if(eTDisposition!=null){
            dispositions.add(bahmniDispositionMapper.map(eTDisposition, eTProvider, observation.getCreator(),locale));
        }
    }
}
