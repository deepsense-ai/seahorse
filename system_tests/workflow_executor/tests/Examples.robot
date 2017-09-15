# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  examples

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
Example1
    Run Workflow Local
    Check Report

Example2
    Run Workflow Local
    Check Report

Example3
    Run Workflow Local
    Check Report

Example4
    Run Workflow Local
    Check Report
