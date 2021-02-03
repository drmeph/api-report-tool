package com.kevindorfer.apireporttool.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevindorfer.apireporttool.SupportedApi;
import com.kevindorfer.apireporttool.service.ApiReportService;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PlacesApiReportServiceImpl implements ApiReportService {
    private static final String FIND_PLACE_ENDPOINT = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json";
    private static final String SEARCH_TEXT_ENDPOINT = "https://maps.googleapis.com/maps/api/place/textsearch/json";
    private static final String INPUT_TYPE = "textquery";

    @Value("${google.placesApi.key}")
    private String API_KEY;

    @Value("${google.placesApi.fields}")
    String SEARCH_FIELDS;

    @Value("${google.placesApi.locationBias}")
    String LOCATION_BIAS;

    private RestTemplate restTemplate;
    private HttpHeaders httpHeaders;

    public PlacesApiReportServiceImpl() {
        restTemplate = new RestTemplate();
        httpHeaders = new HttpHeaders();
    }

    @Override
    public ResponseEntity<String> sendRequest(String descriptor, SupportedApi api) {
        String uri = getParameterizedUri(api, descriptor);
        HttpEntity<?> entity = new HttpEntity<>(httpHeaders);

        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }

    @Override
    public List<String[]> transformRecord(String descriptor, ResponseEntity<String> responseEntity, SupportedApi api) {
        List<String[]> records = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(responseEntity.getBody());

            JsonNode status = root.path("status");

            if (status.textValue().equals("OK")) {
                JsonNode results = root.path(api == SupportedApi.FIND ? "candidates" : "results");

                if (results != null && results.isArray()) {
                    boolean isFirst = true;

                    for (JsonNode jsonNode : results) {
                        JsonNode address = jsonNode.path("formatted_address");
                        JsonNode name = jsonNode.path("name");

                        if (address != null && name != null) {
                            records.add(new String[] {
                                    isFirst ? descriptor : null, isFirst ? status.textValue() : null,
                                    name.textValue(), address.textValue()
                            });

                            isFirst = false;
                        }
                    }
                }
            } else {
                records.add(new String[] {
                        descriptor, status.textValue(), null, null
                });
            }
        } catch (JsonProcessingException e) {
            log.error("unable to parse JSON response: ", e);
        }

        return records;
    }

    @Override
    public String getParameterizedUri(SupportedApi api, String input) {
        UriComponentsBuilder uri = null;

        switch (api) {
            case FIND: {
                uri = UriComponentsBuilder.fromHttpUrl(FIND_PLACE_ENDPOINT)
                        .queryParam("input", input)
                        .queryParam("key", API_KEY)
                        .queryParam("inputtype", INPUT_TYPE)
                        .queryParam("rectangle", LOCATION_BIAS)
                        .queryParam("fields", SEARCH_FIELDS);
                break;
            }
            case TEXT: {
                uri = UriComponentsBuilder.fromHttpUrl(SEARCH_TEXT_ENDPOINT)
                        .queryParam("query", input)
                        .queryParam("key", API_KEY);
                break;
            }
            default:
        }

        return uri.build(false).toUriString();
    }

    @Override
    public String[] getHeader() {
        return new String[] {
                "card acceptor name",
                "status",
                "name",
                "formatted address"
        };
    }

    @Override
    public void persistRecords(List<String[]> batchRecords, int index, CSVWriter csvWriter) {
        long start = System.currentTimeMillis();
        log.info("Check size {} - index {}", batchRecords.size(), index);
        log.info("Saving to file");

        for (String[] rec : batchRecords) {
            csvWriter.writeNext(rec);
        }

        log.info("Done saving - Time: {}", System.currentTimeMillis() - start);
    }
}
