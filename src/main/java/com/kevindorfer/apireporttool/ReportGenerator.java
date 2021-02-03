package com.kevindorfer.apireporttool;

import com.kevindorfer.apireporttool.service.ApiReportService;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class ReportGenerator {
    private static final char SEPARATOR = ';';
    private static final long TIMEOUT = 7;

    @Value("${report.batchSize}")
    int batchSize = 2;

    @Value("${report.testVolume}")
    int testVolume = 13;

    Map<String, CompletableFuture<List<String[]>>> futureMap;

    @Autowired
    ApiReportService apiReportService;

    public void generateReport(File input, File output, boolean test, SupportedApi api) throws IOException,
            CsvValidationException {
        log.info("Start: {} Report Generation", api.title);
        log.info("Test Mode: {}", test);

        try(
                Reader reader = new FileReader(input);
                Writer writer = new FileWriter(output);

                CSVReader csvReader = new CSVReader(reader);
                CSVWriter csvWriter = new CSVWriter(writer, SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.NO_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END)
        ) {
            String[] nextRecord;
            csvWriter.writeNext(apiReportService.getHeader());
            List<String> batchDescriptors = new ArrayList<>();
            int index = 0;
            List<String[]> batchRecords;

            while ((nextRecord = csvReader.readNext()) != null && (index < testVolume || !test)) {
                if (nextRecord != null && index > 0) { //Skip the header row
                    String descriptor = nextRecord[1];

                    if (batchDescriptors.size() >= batchSize) { //process batch
                        batchRecords = getPlaceApiRecord(batchDescriptors, api);

                        if (batchRecords != null && !batchRecords.isEmpty()) {
                            apiReportService.persistRecords(batchRecords, index, csvWriter);
                        }

                        batchDescriptors = new ArrayList<>();
                    } else {
                        batchDescriptors.add(descriptor);
                    }
                }

                if (index%batchSize == 0 && index > 0) {
                    log.info("{} Descriptors done!: ", index);
                }

                index++;
            }

            if (!batchDescriptors.isEmpty()) {
                log.info("cleaning batch records of {} records", batchDescriptors.size());
                batchRecords = getPlaceApiRecord(batchDescriptors, api);

                if (batchRecords != null && !batchRecords.isEmpty()) {
                    apiReportService.persistRecords(batchRecords, index, csvWriter);
                }
            }
        }

        log.info("End: {} Report Generation", api.title);
    }

    private List<String[]> getPlaceApiRecord(List<String> descriptors, SupportedApi api) {
        long startTime = System.currentTimeMillis();
        log.info("Start: Place Api Fuzzy Record");
        List<String[]> result = new ArrayList<>();
        futureMap = new HashMap<>();

        for (String descriptor: descriptors) {

            CompletableFuture<List<String[]>> futureValue = CompletableFuture
                    .supplyAsync(() -> apiReportService.sendRequest(descriptor, api))
                    .thenApply(response -> apiReportService.transformRecord(descriptor, response, api));

            futureMap.put(descriptor, futureValue);
        }

        for (String descriptor: futureMap.keySet()) {

            try {
                List<String[]> records = futureMap.get(descriptor).get(TIMEOUT, TimeUnit.SECONDS);
                result.addAll(records);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error while requesting descriptor: {} - {}", descriptor, e.getMessage());
                result.add(new String[]{
                        descriptor, "ERROR: " + e.getMessage(), null, null
                });
                continue;
            } catch (TimeoutException e) {
                //log.error("Timeout Error while requesting descriptor: {}", descriptor);
                result.add(new String[]{
                        descriptor, "TIMEOUT ERROR", null, null
                });
                continue;
            }
        }

        long endTime = System.currentTimeMillis();
        log.info("End: Place Api Fuzzy Record - Time {} ms", endTime - startTime);
        return result;
    }
}
