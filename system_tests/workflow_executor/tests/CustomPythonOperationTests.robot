# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =    customPythonOperationTests


*** Settings ***
Suite Setup     Standard Suite Setup
Suite Teardown  Standard Suite Teardown

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Success
    [Setup]  Standard Test Setup
    Run Workflow Local
    Check Report
    [Teardown]  Standard Test Teardown
