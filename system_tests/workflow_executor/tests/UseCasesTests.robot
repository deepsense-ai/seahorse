# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  useCasesTests

*** Keywords ***

*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Test Setup      Standard Test Setup
Test Teardown   Standard Test Teardown

Library    OperatingSystem
Library    Collections
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Income Prediction
    Run Workflow Local
    Check Report

Text Message Spam Detection
    Run Workflow Local
    Check Report

Wine Properties
    Run Workflow Local
    Check Report

Mushrooms
    Run Workflow Local
    Check Report

US Baby Names
    Run Workflow Local
    Check Report
