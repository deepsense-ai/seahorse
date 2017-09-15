# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  clusteringTests

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
K Means
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report
