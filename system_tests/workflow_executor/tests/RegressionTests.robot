# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  regressionTests

*** Keywords ***
Upload Suite Resources To Hdfs
    Upload To Hdfs    ${HDFS PATH}training.csv    ${SUITE RESOURCE PATH}training.csv
    Upload To Hdfs    ${HDFS PATH}test.csv    ${SUITE RESOURCE PATH}test.csv

Upload File To HDFS
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
Ridge Regression
    Upload Suite Resources To Hdfs
    Run Workflow
    Check Report

Lasso Regression
    Upload Suite Resources To Hdfs
    Run Workflow
    Check Report

Random Forest
    Upload Suite Resources To Hdfs
    Run Workflow
    Check Report

Gradient Boosted Trees
    Upload Suite Resources To Hdfs
    Run Workflow
    Check Report