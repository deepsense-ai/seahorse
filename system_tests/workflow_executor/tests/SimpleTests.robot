# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  simpleTests

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
Empty Workflow
    Run Workflow Local
    Check Report
