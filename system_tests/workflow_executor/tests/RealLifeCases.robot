# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Location Attractiveness
    ${dir} =    Set Variable    resources/realLifeCases/locationAttractiveness/
    Remove Hdfs Path    /system_tests/locationAttractiveness
    Upload To Hdfs    /system_tests/locationAttractiveness/LocationAttractiveness.csv    ${dir}LocationAttractiveness.csv
    Remove Directory    locationAttractivenessOutput    recursive=yes
    Create Output Directory    locationAttractivenessOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/locationAttractiveness

Demand Forecasting
    Remove Hdfs Path    /system_tests/demandForecasting/
    ${dir} =    Set Variable    resources/demandForecasting/
    Upload To Hdfs    /system_tests/demandForecasting/demand.csv    ${dir}demand.csv
    Upload To Hdfs    /system_tests/demandForecasting/weather.csv    ${dir}weather.csv
    Remove Directory    demandForecastingOutput    recursive=yes
    Create Output Directory    demandForecastingOutput
    Run Workflow    ${dir}workflow_hdfs.json
    Check Execution Status
    Clean Output Directory
    Remove Hdfs Path    /system_tests/demandForecasting/

Demand Forecasting Using Random Forest
    Remove Hdfs Path    /system_tests/demandForecastingRF/
    ${dir} =    Set Variable    resources/demandForecastingRF/
    Upload To Hdfs    /system_tests/demandForecastingRF/demand.csv    ${dir}demand.csv
    Upload To Hdfs    /system_tests/demandForecastingRF/weather.csv    ${dir}weather.csv
    Remove Directory    demandForecastingRFOutput    recursive=yes
    Create Output Directory    demandForecastingRFOutput
    Run Workflow    ${dir}workflow_hdfs.json
    Check Execution Status
    Clean Output Directory
    Remove Hdfs Path    /system_tests/demandForecastingRF/
