package org.bahmni.module.bahmnicore.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationServiceImpl;
import org.bahmni.module.bahmnicore.service.RowMapper;
import org.bahmni.module.bahmnicore.service.SqlSearchService;
import org.bahmni.module.bahmnicore.util.SqlQueryHelper;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
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
    private SqlSearchParamStoreImpl searchParamStore;

    private static Logger logger = LogManager.getLogger(SqlSearchServiceImpl.class);

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public void setSearchParamStore(SqlSearchParamStoreImpl searchParamStore) {
        this.searchParamStore = searchParamStore;
    }

    @Override
    public List<SimpleObject>  search(String queryId, Map<String, String[]> params) {
        Map<String, String[]> updatedParams = conditionallyAddVisitLocation(params);
        updatedParams.put("user_locale", new String[] { Context.getUserContext().getLocale().getLanguage() } );
        Map<String, String[]> mergedParams = addQueryParamsFromStore(updatedParams);
        List<SimpleObject> results = new ArrayList<>();
        SqlQueryHelper sqlQueryHelper = new SqlQueryHelper();
        String query = getSql(queryId);
        debugPrintQueryParams(queryId, mergedParams);
        try( Connection conn = DatabaseUpdater.getConnection();
            PreparedStatement statement = sqlQueryHelper.constructPreparedStatement(query,mergedParams,conn);
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

    private void debugPrintQueryParams(String queryId, Map<String, String[]> mergedParams) {
        logger.debug(String.format("Query Params for %s : %s", queryId,
            mergedParams.entrySet().stream()
                .map(entry -> entry.getKey())
                .reduce((s, e) -> s.concat(",").concat(e))
                .orElse("")));
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
            String[] visitLocationValue = {visitLocation};
            updatedParams.put("visit_location_uuid", visitLocationValue);
        }
        return updatedParams;
    }

    private Map<String, String[]> addQueryParamsFromStore(Map<String, String[]> params) {
        Map<String, String[]> updatedParams = new HashMap<>(params);
        if (!searchParamStore.isInitialized()) {
            searchParamStore.initQueryParamStore();
        }
        Map<String, Object> searchableParameters = searchParamStore.getSearchableParameters();
        searchableParameters.entrySet().forEach(entry -> {
            updatedParams.put(entry.getKey(), new String[] { entry.getValue().toString() } );
        });
        return updatedParams;
    }
}
