package org.bahmni.module.elisatomfeedclient.api.worker;

import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.bahmni.module.elisatomfeedclient.api.mapper.OpenElisTestDetailMapper;
import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.mapper.LabOrderResultMapper;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;

public class ResultObsHelper {
    public static final String VOID_REASON = "updated since by lab technician";

    private final ConceptService conceptService;

    public ResultObsHelper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public Obs createNewObsForOrder(OpenElisTestDetail testDetail, Order testOrder, Encounter resultEncounter) throws ParseException {
        Date obsDate = DateTime.parse(testDetail.getDateTime()).toDate();
        if(testDetail.getPanelUuid() != null) {
            Obs panelObs = createOrFindPanelObs(testDetail, testOrder, resultEncounter, obsDate);
            Concept testConcept = conceptService.getConceptByUuid(testDetail.getTestUuid());
            panelObs.addGroupMember(createObsForTest(testDetail, testOrder, testConcept));
            return panelObs;
        } else {
            return createObsForTest(testDetail, testOrder, testOrder.getConcept());
        }
    }

    public void voidObs(Obs obs, Date testDate) {
        obs.setVoided(true);
        obs.setVoidReason(VOID_REASON);
        obs.setDateVoided(testDate);
        final Set<Obs> groupMembers = obs.getGroupMembers();
        if ((groupMembers != null) && (groupMembers.size() > 0)) {
            for (Obs member : groupMembers) {
                voidObs(member, testDate);
            }
        }
    }

    private Obs createObsForTest(OpenElisTestDetail testDetail, Order order, Concept testConcept) {
        LabOrderResult labOrderResult = new OpenElisTestDetailMapper().map(testDetail, testConcept);
        return new LabOrderResultMapper(conceptService).map(labOrderResult, order, testConcept);
    }

    private Obs createOrFindPanelObs(OpenElisTestDetail testDetail, Order testOrder, Encounter resultEncounter, Date obsDate) {
        Obs panelObs = null;
        for (Obs obs : resultEncounter.getObsAtTopLevel(false)) {
            if(obs.getConcept().getUuid().equals(testDetail.getPanelUuid()) && obs.getOrder().getId().equals(testOrder.getId())){
                panelObs = obs;
                break;
            }
        }
        return panelObs != null ? panelObs : newObs(testOrder, testOrder.getConcept(), obsDate);
    }

    private Obs newObs(Order order, Concept concept, Date obsDate) {
        Obs labObs = new Obs();
        labObs.setConcept(concept);
        labObs.setOrder(order);
        labObs.setObsDatetime(obsDate);
        return labObs;
    }

}
