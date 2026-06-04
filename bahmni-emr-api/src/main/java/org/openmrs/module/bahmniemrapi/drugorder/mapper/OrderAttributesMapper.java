package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderAttributesMapper {

    public List<BahmniDrugOrder> map(List<BahmniDrugOrder> drugOrders, Collection<BahmniObservation> observations){
        Map<String, BahmniDrugOrder> bahmniDrugOrderMap = createOrderUuidToDrugOrderMap(drugOrders);
        if(CollectionUtils.isNotEmpty(observations) && MapUtils.isNotEmpty(bahmniDrugOrderMap)){
            for(BahmniObservation bahmniObservation : observations){
                if(bahmniDrugOrderMap.containsKey(bahmniObservation.getOrderUuid())){
                    BahmniDrugOrder bahmniDrugOrder = bahmniDrugOrderMap.get(bahmniObservation.getOrderUuid());
                    BahmniOrderAttribute bahmniOrderAttribute =
                            new BahmniOrderAttribute(
                                    bahmniObservation.getConcept().getName(),
                                    bahmniObservation.getValue().toString(),
                                    bahmniObservation.getUuid(),
                                    bahmniObservation.getConceptUuid(),
                                    bahmniObservation.getEncounterUuid());
                    addOrderAttributes(bahmniDrugOrder, bahmniOrderAttribute);
                }
            }
        }
        return new ArrayList<>(bahmniDrugOrderMap.values());
    }

    private void addOrderAttributes(BahmniDrugOrder bahmniDrugOrder, BahmniOrderAttribute bahmniOrderAttribute) {
        if(CollectionUtils.isNotEmpty(bahmniDrugOrder.getOrderAttributes())){
            bahmniDrugOrder.getOrderAttributes().add(bahmniOrderAttribute);
        }else{
            List<BahmniOrderAttribute> bahmniOrderAttributes = new ArrayList<>();
            bahmniOrderAttributes.add(bahmniOrderAttribute);
            bahmniDrugOrder.setOrderAttributes(bahmniOrderAttributes);
        }
    }

    private Map<String, BahmniDrugOrder> createOrderUuidToDrugOrderMap(List<BahmniDrugOrder> drugOrders){
        Map<String, BahmniDrugOrder> bahmniDrugOrderMap = new LinkedHashMap<>();
        if(CollectionUtils.isNotEmpty(drugOrders)){
            for(BahmniDrugOrder bahmniDrugOrder : drugOrders){
                bahmniDrugOrderMap.put(bahmniDrugOrder.getUuid(), bahmniDrugOrder);
            }
        }
        return bahmniDrugOrderMap;
    }
}
