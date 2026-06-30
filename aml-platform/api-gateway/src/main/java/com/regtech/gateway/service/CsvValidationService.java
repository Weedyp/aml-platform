package com.regtech.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;

@Service
public class CsvValidationService {

    private static final Logger log = LoggerFactory.getLogger(CsvValidationService.class);

    private final CsvMapper csvMapper = new CsvMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    /**
     * @param extractedCsvPath The raw CSV file we just extracted
     * @param schemaName The name of the schema to validate against (e.g., "bank_alpha_customer_v1.json")
     */
    public void processAndValidate(Path extractedCsvPath, String schemaName) {
        log.info("Starting Dynamic Schema Validation for file: {}", extractedCsvPath.getFileName());

        try {

            com.networknt.schema.SchemaValidatorsConfig config = new com.networknt.schema.SchemaValidatorsConfig();
            config.setTypeLoose(true);
            // 1. Load the dynamic schema from our resources folder
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schemas/" + schemaName);
            JsonSchema schema = schemaFactory.getSchema(schemaStream, config);

            // 2. Configure the CSV parser to read the first row as column headers
            CsvSchema bootstrapSchema = CsvSchema.emptySchema().withHeader();

            // 3. Open a memory-efficient stream (does not load the whole file into RAM)
            var iterator = csvMapper.readerFor(JsonNode.class)
                    .with(bootstrapSchema)
                    .readValues(extractedCsvPath.toFile());

            int rowCount = 0;
            int failedCount = 0;

            // 4. Stream through the file row by row
            while (iterator.hasNextValue()) {
                rowCount++;
                JsonNode rowAsJson = (JsonNode) iterator.nextValue();

                // 5. Run the JSON Schema Validation
                Set<ValidationMessage> errors = schema.validate(rowAsJson);

                if (errors.isEmpty()) {
                    // TODO: Valid Row! Add to our fast SQL Batch Insert list.
                } else {
                    // Invalid Row! Log the exact reason it failed.
                    failedCount++;
                    log.warn("Row {} failed validation. Errors: {}", rowCount, errors);
                    // TODO: Send to Dead Letter Queue (DLQ) table.
                }
            }

            log.info("Validation Complete. Total Rows: {}. Failed: {}", rowCount, failedCount);

        } catch (Exception e) {
            log.error("Failed to process CSV file", e);
            throw new RuntimeException("Data processing failure");
        }
    }
}
