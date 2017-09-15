# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =    pythonTransformationTests


*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Test Setup      Standard Test Setup
Test Teardown   Standard Test Teardown

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Syntax Error
    Run Workflow Local
    Check Report

No Transform Function Error
    Run Workflow Local
    Check Report

Bad Transform Function Signature Error
    Run Workflow Local
    Check Report

Execution Error
    Run Workflow Local
    Check Report

Success
    Run Workflow Local
    Check Report

Success Automatic Conversion To DataFrame
    Run Workflow Local
    Check Report
