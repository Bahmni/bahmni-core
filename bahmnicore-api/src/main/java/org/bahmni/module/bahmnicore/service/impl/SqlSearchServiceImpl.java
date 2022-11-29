package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.RowMapper;
import org.bahmni.module.bahmnicore.service.SqlSearchService;
import org.bahmni.module.bahmnicore.util.SqlQueryHelper;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.util.DatabaseUpdater;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlSearchServiceImpl implements SqlSearchService {
    private AdministrationService administrationService;

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    @Override
    public List<SimpleObject>  search(String queryId, Map<String, String[]> params) {
        Map<String, String[]> updatedParams = conditionallyAddVisitLocation(params);
        List<SimpleObject> results = new ArrayList<>();
        SqlQueryHelper sqlQueryHelper = new SqlQueryHelper();
        String query = getSql(queryId);
        try( Connection conn = DatabaseUpdater.getConnection();
             PreparedStatement statement = sqlQueryHelper.constructPreparedStatement(query,updatedParams,conn);
             ResultSet resultSet = statement.executeQuery()) {

            RowMapper rowMapper = new RowMapper();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getSql(String queryId) {
        String query = administrationService.getGlobalProperty(queryId);
        if (query == null) throw new RuntimeException("No such query:" + queryId);
        return query;
    }

    private Map<String, String[]> conditionallyAddVisitLocation(Map<String, String[]> params) {
        Map<String, String[]> updatedParams = new HashMap<>(params);
        if (params.containsKey("location_uuid")) {
            String locationUuid = params.get("location_uuid")[0];
            String visitLocation = new BahmniVisitLocationServiceImpl(Context.getLocationService()).getVisitLocationUuid(locationUuid);
            String[] visitLcoationValue = {visitLocation};
            updatedParams.put("visit_location_uuid", visitLcoationValue);
        }
        return updatedParams;
    }
}
