package com.kevindorfer.apireporttool.service;

import com.kevindorfer.apireporttool.SupportedApi;
import com.opencsv.CSVWriter;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ApiReportService {
    ResponseEntity<String> sendRequest(String descriptor, SupportedApi api);
    List<String[]> transformRecord(String descriptor, ResponseEntity<String> responseEntity, SupportedApi api);
    String getParameterizedUri(SupportedApi api, String input);
    String[] getHeader();
    void persistRecords(List<String[]> batchRecords, int index, CSVWriter csvWriter);
}
