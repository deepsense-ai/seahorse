# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =    ioTests


*** Keywords ***
Upload Resource File To Hdfs
    [Arguments]  ${RESOURCE FILE NAME}
    Upload to Hdfs  ${HDFS PATH}${RESOURCE FILE NAME}    ${TEST RESOURCE PATH}${RESOURCE FILE NAME}

Upload Resource File To S3
    [Arguments]  ${RESOURCE FILE NAME}
    Upload to S3  ${S3 PATH}${RESOURCE FILE NAME}    ${TEST RESOURCE PATH}${RESOURCE FILE NAME}


*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Library    Collections
Library    OperatingSystem
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Read Write Local Filesystem
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

No Write Permissions
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

Read Write Local Filesystem Json Format
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

Read Write Local Filesystem Parquet Format
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

Read Write HDFS
    [Setup]  Standard Hdfs Test Setup
    Upload Resource File To Hdfs  input.csv
    Run Workflow
    Check Report
    [Teardown]  Standard Hdfs Test Teardown

Missing HDFS File
    [Setup]  Standard Hdfs Test Setup
    Run Workflow
    Check Report
    [Teardown]  Standard Hdfs Test Teardown

Read Write S3
    [Setup]  Standard S3 Test Setup
    Upload Resource File To S3  input.csv
    Run Workflow
    Check Report
    [Teardown]  Standard S3 Test Teardown
