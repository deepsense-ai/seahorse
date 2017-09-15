# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =    customTransformerTests


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
Incorrect Workflow Error
    Run Workflow Local
    Check Report

Execution Error
    Run Workflow Local
    Check Report

Success
    Run Workflow Local
    Check Report
