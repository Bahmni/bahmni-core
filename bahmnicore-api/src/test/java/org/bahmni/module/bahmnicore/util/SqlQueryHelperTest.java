package org.bahmni.module.bahmnicore.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.AdministrationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SqlQueryHelperTest {

    @Mock
    private AdministrationService administrationService;
    private SqlQueryHelper sqlQueryHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        sqlQueryHelper = new SqlQueryHelper();
    }

    @Test
    public void shouldReturnQueryParamsInOrder(){
        String queryString ="select * from encounter where date_started=${en_date_started} AND visit_id=${en_visit_id} and patient_id=${en_patient_id}";
        List<String> paramNamesFromPlaceHolders = sqlQueryHelper.getParamNamesFromPlaceHolders(queryString);
        assertEquals("en_date_started",paramNamesFromPlaceHolders.get(0));
        assertEquals("en_visit_id",paramNamesFromPlaceHolders.get(1));
        assertEquals("en_patient_id",paramNamesFromPlaceHolders.get(2));
    }

    @Test
    public void shouldTransformQueryIntoPreparedStatementFormat(){
        String queryString ="select * from encounter where date_started=${en_date_started} AND visit_id=${en_visit_id} and patient_id=${en_patient_id}";
        String expectQueryString = "select * from encounter where date_started=? AND visit_id=? and patient_id=?";
        String result = sqlQueryHelper.transformIntoPreparedStatementFormat(queryString);
        assertEquals(expectQueryString,result);
    }

    @Test
    public void shouldEscapeSQLInjection() {
        assertEquals("0X3", SqlQueryHelper.escapeSQL("0x3", true, null));
        assertEquals("DROP sampletable\\;--", SqlQueryHelper.escapeSQL("DROP sampletable;--", true, null));
        assertEquals("admin\\'--", SqlQueryHelper.escapeSQL("admin'--", true, null));
        assertEquals("admin\\'\\\\/*", SqlQueryHelper.escapeSQL("admin'/*", true, null));
    }

    @Test
    public void shouldDetectSameParams(){
        String queryString ="select * from location where name=${location_name} or location_id=${location_id} or location_id in (select l.location_id from location l where l.name=${location_name})";
        String expectQueryString = "select * from location where name=? or location_id=? or location_id in (select l.location_id from location l where l.name=?)";
        String result = sqlQueryHelper.transformIntoPreparedStatementFormat(queryString);
        assertEquals(expectQueryString,result);
        List<String> paramNamesFromPlaceHolders = sqlQueryHelper.getParamNamesFromPlaceHolders(queryString);
        assertEquals(3, paramNamesFromPlaceHolders.size());
        assertEquals("location_name", paramNamesFromPlaceHolders.get(0));
        assertEquals("location_id", paramNamesFromPlaceHolders.get(1));
        assertEquals("location_name", paramNamesFromPlaceHolders.get(2));
    }

    @Test
    public void shouldParseAdditionalParams() {
        String queryString = "SELECT *\n" +
                "FROM person p\n" +
                "  INNER JOIN person_name pn ON pn.person_id = p.person_id\n" +
                "  INNER join (SELECT * FROM obs\n" +
                "              WHERE concept_id IN\n" +
                "                                   (SELECT concept_id\n" +
                "                                    FROM concept_name cn cn.name in (${testName}))  as tests on tests.person_id = p.person_id";
        String additionalParams = "{\"tests\": \"'HIV (Blood)','Gram Stain (Sputum)'\"}";

        Map<String, String[]> params = new HashMap<>();
        String result = sqlQueryHelper.parseAdditionalParams(additionalParams, queryString, params);

        String expectedQueryString = "SELECT *\n" +
                "FROM person p\n" +
                "  INNER JOIN person_name pn ON pn.person_id = p.person_id\n" +
                "  INNER join (SELECT * FROM obs\n" +
                "              WHERE concept_id IN\n" +
                "                                   (SELECT concept_id\n" +
                "                                    FROM concept_name cn cn.name in (${testName_0},${testName_1}))  as tests on tests.person_id = p.person_id";
        assertEquals(expectedQueryString, result);
        assertEquals("HIV (Blood)", params.get("testName_0")[0]);
        assertEquals("Gram Stain (Sputum)", params.get("testName_1")[0]);
    }

    @Test
    public void shouldBindTestNameFromAdditionalParamsSafely() throws Exception {
        String queryString = "SELECT * FROM obs WHERE test_name = ${testName}";
        Map<String, String[]> params = new HashMap<>();
        params.put("additionalParams", new String[]{"{\"tests\":\"CBC\"}"});

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        sqlQueryHelper.constructPreparedStatement(queryString, params, mockConn);

        verify(mockConn).prepareStatement(eq("SELECT * FROM obs WHERE test_name = ?"));
        verify(mockPreparedStatement).setObject(eq(1), eq("CBC"));
    }

    @Test
    public void shouldBindSqlInjectionAttemptAsLiteralValue() throws Exception {
        String queryString = "SELECT * FROM obs WHERE test_name = ${testName}";
        String injectionPayload = "' OR '1'='1";
        Map<String, String[]> params = new HashMap<>();
        params.put("additionalParams", new String[]{"{\"tests\":\"" + injectionPayload + "\"}"});

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        sqlQueryHelper.constructPreparedStatement(queryString, params, mockConn);

        verify(mockConn).prepareStatement(eq("SELECT * FROM obs WHERE test_name = ?"));
        verify(mockPreparedStatement).setObject(eq(1), eq(injectionPayload));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowWhenAdditionalParamsJsonIsMalformed() throws Exception {
        String queryString = "SELECT * FROM obs WHERE test_name = ${testName}";
        Map<String, String[]> params = new HashMap<>();
        params.put("additionalParams", new String[]{"not-valid-json"});

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        sqlQueryHelper.constructPreparedStatement(queryString, params, mockConn);
    }
}
