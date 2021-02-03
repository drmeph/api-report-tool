package com.kevindorfer.apireporttool;

import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.kevindorfer.apireporttool.SupportedApi.FIND;
import static com.kevindorfer.apireporttool.SupportedApi.TEXT;

@Slf4j
@SpringBootTest(classes = ApiReportToolApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiReportToolIT {
    private static final String DESCRIPTOR_INPUT = "HKSP_MatchRateAnalysis_v1.0.csv";
    private static final String DESCRIPTOR_OUTPUT = "places-api-output.csv";
    private static final String CSV_EXTENSION = ".csv";

    @Autowired
    ReportGenerator reportGenerator;

    /**
     * Generate a TEST exact match report, it should contain no
     * @throws IOException
     * @throws CsvValidationException
     */
    @Test
    //@Disabled
    public void testExactMatchCSVFile() throws IOException, CsvValidationException {
        File input = new ClassPathResource(DESCRIPTOR_INPUT).getFile();
        File output = new File(DESCRIPTOR_OUTPUT + "-exact-match" + CSV_EXTENSION);

        reportGenerator.generateReport(input, output, true, FIND);
    }

    @Test
    //@Disabled
    public void testFuzzyMatchCSVFile() throws IOException, CsvValidationException {
        File input = new ClassPathResource(DESCRIPTOR_INPUT).getFile();
        File output = new File(DESCRIPTOR_OUTPUT + "-fuzzy-match" + CSV_EXTENSION);

        reportGenerator.generateReport(input, output, true, TEXT);
    }
}
