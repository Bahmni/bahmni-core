package org.bahmni.datamigration;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorrectedTehsils {
    private static Logger logger = LogManager.getLogger(CorrectedTehsils.class);
    private Map<String, String> oldNewTehsils;

    public CorrectedTehsils(String csvLocation, String fileName) throws IOException {
        File file = new File(csvLocation, fileName);
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), ',');
            reader.readNext(); //ignore header
            List<String[]> rows = reader.readAll();
            oldNewTehsils = new HashMap<String, String>(rows.size());
            logger.info(String.format("Found %d tehsil mapping", rows.size()));
            for (String[] row : rows) {
                oldNewTehsils.put(row[0].trim(), row[1].trim());
            }
        } finally {
            if (reader != null) reader.close();
        }
    }

    public String correctedTehsil(String tehsil) {
        return oldNewTehsils.get(tehsil);
    }
}