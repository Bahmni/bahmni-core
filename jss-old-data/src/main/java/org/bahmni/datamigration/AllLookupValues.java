package org.bahmni.datamigration;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllLookupValues implements LookupValueProvider {
    private static Logger logger = LogManager.getLogger(AllLookupValues.class);
    private Map<Integer, Object[]> map = new HashMap<Integer, Object[]>();

    protected AllLookupValues() {
    }

    public AllLookupValues(String csvLocation, String fileName) throws IOException {
        File file = new File(csvLocation, fileName);
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), ',');
            reader.readNext(); //ignore header
            List<String[]> rows = reader.readAll();
            logger.info(String.format("Found %d lookupValues", rows.size()));
            for (String[] row : rows) {
                Object[] allExceptFirstIndex = ArrayUtils.remove(row, 0);
                map.put(Integer.parseInt(row[0].trim()), allExceptFirstIndex);
            }
        } finally {
            if (reader != null) reader.close();
        }
    }

    public String getLookUpValue(String key) {
        return getLookUpValue(key, 0);
    }

    @Override
    public String getLookUpValue(String key, int valueIndexExcludesFirstColumn) {
        if (StringUtils.equals("0", key)) return null;

        int keyAsNumber = Integer.parseInt(key.trim());
        Object[] values = map.get(keyAsNumber);
        if (values == null) return null;
        return values[valueIndexExcludesFirstColumn].toString();
    }
}