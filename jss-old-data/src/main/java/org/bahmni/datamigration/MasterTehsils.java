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

public class MasterTehsils {
    private static Logger logger = LogManager.getLogger(CorrectedTehsils.class);
    private Map<String, FullyQualifiedTehsil> fullyQualifiedTehsils = new HashMap<String, FullyQualifiedTehsil>();

    public MasterTehsils(String csvLocation, String fileName) throws IOException {
        File file = new File(csvLocation, fileName);
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());

        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(file), ',');
            reader.readNext(); //ignore header
            List<String[]> rows = reader.readAll();
            logger.info(String.format("Found %d master fullyQualifiedTehsils", rows.size()));
            for (String[] row : rows) {
                fullyQualifiedTehsils.put(row[2].trim(), new FullyQualifiedTehsil(row[2].trim(), row[1].trim(), row[0].trim()));
            }
        } finally {
            if (reader != null) reader.close();
        }
    }

    public FullyQualifiedTehsil getFullyQualifiedTehsil(String tehsil) {
        return fullyQualifiedTehsils.get(tehsil);
    }
}