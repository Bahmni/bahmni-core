package org.bahmni.module.bahmnicore.service;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class RowMapper {

    public SimpleObject mapRow(ResultSet rs) throws SQLException {
        SimpleObject row = new SimpleObject();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            Object value = rs.getObject(column);
            if (value == null) {
                row.put(column, "");
            } else {
                row.put(column, value);
            }
        }
        return row;
    }

}
