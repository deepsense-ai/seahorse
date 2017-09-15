# Copyright (c) 2016, CodiLime Inc.

*** Variables ***
${SUITE} =    ioTransformerTests

*** Keywords ***

*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Library    Collections
Library    OperatingSystem
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


# Write and read transformer tests are split into two seperate workflows,
# as there is no way to test it (in the same workflow) if loaded model works.

# Existing tools and frameworks support one workflow execution per test in suite,
# so to keep it simple, only output of read and transform is validated.

*** Test Cases ***
Custom Transformer
    [Setup]  Standard Test Setup
    Run Workflow Local  ${TEST RESOURCE PATH}writeTransformerWorkflow.json
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

GBT Classifier Model
    [Setup]  Standard Test Setup
    Run Workflow Local  ${TEST RESOURCE PATH}writeTransformerWorkflow.json
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

String Indexer Single Column
    [Setup]  Standard Test Setup
    Run Workflow Local  ${TEST RESOURCE PATH}writeTransformerWorkflow.json
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

String Indexer Multi Column
    [Setup]  Standard Test Setup
    Run Workflow Local  ${TEST RESOURCE PATH}writeTransformerWorkflow.json
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

Logistic Regression Model
    [Setup]  Standard Test Setup
    Run Workflow Local  ${TEST RESOURCE PATH}writeTransformerWorkflow.json
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown

Missing Transformer
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown
