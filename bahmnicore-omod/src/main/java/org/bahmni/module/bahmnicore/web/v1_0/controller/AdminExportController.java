package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.fileexport.FileExporter;
import org.bahmni.module.admin.csv.exporter.ConceptSetExporter;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class AdminExportController extends BaseRestController {
    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/admin/export";
    private static Logger logger = LogManager.getLogger(AdminExportController.class);

    @Autowired
    private ConceptSetExporter conceptSetExporter;

    @RequestMapping(value = baseUrl + "/conceptset", method = RequestMethod.GET)
    @ResponseBody
    public void export(HttpServletResponse response, @RequestParam(value = "conceptName", required = true) String conceptName) {
        try {
            ConceptRows conceptRows = conceptSetExporter.exportConcepts(conceptName);
            createZipFile(response, conceptRows);
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + conceptName + ".zip" + "\"");
            response.flushBuffer();
        } catch (Exception e) {
            logger.error("Could not upload file", e);
        }
    }

    private void createZipFile(HttpServletResponse response, ConceptRows conceptRows) throws java.io.IOException {
        FileExporter<ConceptRow> conceptFileExporter = new FileExporter<>();
        FileExporter<ConceptSetRow> conceptSetFileExporter = new FileExporter<>();
        ByteArrayOutputStream conceptOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream conceptSetOutputStream = new ByteArrayOutputStream();
        conceptOutputStream = conceptFileExporter.exportCSV(conceptRows.getConceptRows(), conceptOutputStream);
        conceptSetOutputStream = conceptSetFileExporter.exportCSV(conceptRows.getConceptSetRows(), conceptSetOutputStream);
        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
        zipOutputStream.putNextEntry(new ZipEntry("concepts.csv"));
        zipOutputStream.write(conceptOutputStream.toByteArray());
        zipOutputStream.closeEntry();
        zipOutputStream.putNextEntry(new ZipEntry("concept_sets.csv"));
        zipOutputStream.write(conceptSetOutputStream.toByteArray());
        zipOutputStream.closeEntry();
        zipOutputStream.close();
    }
}
