package org.acme.agent;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;


@ApplicationScoped
public class CsvDataTool {

    @Tool("get the sales data for the year")
    public List<SaleRecord> getSalesData() {
        try {
            Reader in = new InputStreamReader(
                CsvDataTool.class.getResourceAsStream("/data/sales.csv"));
            CSVFormat format = CSVFormat.Builder.create()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();
            CSVParser parser = format.parse(in);
            List<CSVRecord> records = parser.getRecords();

            List<SaleRecord> result = new ArrayList<>();
            for (CSVRecord record : records) {
                String month = record.get("Month");
                int revenue = Integer.parseInt(record.get("Revenue"));
                result.add(new SaleRecord(month, revenue));
            }

            return result;

        } catch (Exception e) {
            return List.of(new SaleRecord("Error", 0));
        }
    }
}
