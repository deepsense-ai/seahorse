# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  realLifeCases

*** Keywords ***

Upload Resource File To Hdfs
    [Arguments]  ${RESOURCE FILE NAME}
    Upload to Hdfs  ${HDFS PATH}${RESOURCE FILE NAME}    ${TEST RESOURCE PATH}${RESOURCE FILE NAME}


*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Test Setup      Standard Hdfs Test Setup
Test Teardown   Standard Hdfs Test Teardown

Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Location Attractiveness
    Upload Resource File To Hdfs  LocationAttractiveness.csv
    Run Workflow
    Check Report

Demand Forecasting
    Upload Resource File To Hdfs  demand.csv
    Upload Resource File To Hdfs  weather.csv
    Run Workflow
    Check Report

Demand Forecasting Using Random Forest
    Upload Resource File To Hdfs  demand.csv
    Upload Resource File To Hdfs  weather.csv
    Run Workflow
    Check Report
