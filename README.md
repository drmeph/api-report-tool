# API Report tool

This project is meant to be a tool to create customize report using data pulled from apis.
Although The current version isn't generic yet, the goal is to allow users to be able to customize it to use different apis.
But as per now, this tool only create reports for the [Google Places API](https://developers.google.com/places/web-service/overview?hl=en_US).

## Prerequisite
* Java 8 (or later)
* Jdk must be configured to allow secure communication (cacert, etc)
* maven 3

## Installation
* Clone [the project](https://gitlab.ethoca.com/kevin.dorfer/api-report-tool)
```
git clone git@gitlab.ethoca.com:kevin.dorfer/api-report-tool.git
```
* cd into the directory
```
cd places-api-report
```

## Configure
* In [application.yml](src/main/resources/application.yml)
    * Change the Google places api key from "changeme" to your own api key
    * (optional) update `fields` or `locationBias` depending on your needs
    * testVolume: the amount of records processed while running in `test mode`
    * batchSize: Number of descriptors processed at once (preferably < 1000)

## Generate Report
### Command line options
* input: path to the input csv file
* output: path to the output csv file
* api:
    * find: uses the ['Find Place' request](https://developers.google.com/places/web-service/search?hl=en_US)
    * text: uses the ['Text Search' request](https://developers.google.com/places/web-service/search?hl=en_US)
* (optional) test: if added run in `test mode`

### Run report
* run maven to package the project (necessary to pick up the latest configuration)
```
maven clean package
```
* run the report
```
java -jar target/api-report-tool[-version].jar input output api [test]
```