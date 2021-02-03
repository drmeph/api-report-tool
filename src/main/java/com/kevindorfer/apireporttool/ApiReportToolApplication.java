package com.kevindorfer.apireporttool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.kevindorfer.apireporttool.SupportedApi.FIND;
import static com.kevindorfer.apireporttool.SupportedApi.TEXT;

@Slf4j
@SpringBootApplication
public class ApiReportToolApplication implements CommandLineRunner {
    private ReportGenerator reportGenerator;

    public ApiReportToolApplication(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext appContext = SpringApplication.run(ApiReportToolApplication.class, args);
        appContext.close();
    }
    @Override
    public void run(String... args) throws Exception {
        log.info("Args : {}", args.length);

        if (args.length <= 0 || args.length < 3) {
            log.error("Please enter the input, the output files, and the api (find|text)");
        } else {
            List<String> params = new ArrayList<>();

            for (String filename : args) {
                params.add(filename);
            }

            if (!(params.get(2).equals("text") || params.get(2).equals("find"))) {
                log.error("please make sure that api name is \"find\" or \"text\"");
                System.exit(1);
            }

            log.info("Parameters: {}, {}, and {}", params.get(0), params.get(1), params.get(2));

            File input = new File(params.get(0));
            File output = new File(params.get(1));
            boolean isTest = (params.size() >= 4 && params.get(3).equals("test")) ? true : false;

            switch (params.get(2)) {
                case "find": {
                    reportGenerator.generateReport(input, output, isTest, FIND);
                    break;
                }
                case "text": {
                    reportGenerator.generateReport(input, output, isTest, TEXT);
                    break;
                }
                default:
            }
        }
    }
}
