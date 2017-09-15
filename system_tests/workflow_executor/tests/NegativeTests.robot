# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =    negativeTests

*** Keywords ***
Upload File To HDFS
    [Arguments]  ${RESOURCE FILE NAME}
    Upload to Hdfs  ${HDFS PATH}${RESOURCE FILE NAME}    ${TEST RESOURCE PATH}${RESOURCE FILE NAME}

*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Test Setup      Standard Test Setup
Test Teardown   Standard Test Teardown

Library    Collections
Library    OperatingSystem
Library    ../lib/CommonSetupsAndTeardowns.py
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py


*** Test Cases ***
Missing Input Data
    Run Workflow
    Check Report

Missing Parameter Value
    Run Workflow
    Check Report

Parameter Out Of Range
    Run Workflow
    Check Report

Type Mismatch
    Run Workflow
    Check Report

#Trivial Cycle
#    Run Workflow
#    Check Report
#
#Cycle
#    Run Workflow
#    Check Report

Column Name Duplication
    [Setup]  Standard Hdfs Test Setup
    Upload File To HDFS    input.csv
    Run Workflow
    Check Report
    [Teardown]  Standard Hdfs Test Teardown
