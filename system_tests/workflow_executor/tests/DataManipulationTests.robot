# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  dataManipulationTests

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
Decompose Datetime
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report

One Hot Encoder
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report

SQL Expression
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report

Invalid SQL Expression
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report

Train Normalizer
    Upload Resource File To Hdfs  frame1.csv
    Upload Resource File To Hdfs  frame2.csv
    Run Workflow
    Check Report

Join and Split
    Upload Resource File To Hdfs  frame1.csv
    Upload Resource File To Hdfs  frame2.csv
    Run Workflow
    Check Report

Missing Values Handler
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report
