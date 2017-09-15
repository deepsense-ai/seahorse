# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  interactiveTestsTemplate

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
Library    ../lib/InteractiveTestsClient.py


*** Test Cases ***
Use Example
  Publish Message From File  to_executor  ${TEST RESOURCE PATH}publish.json
  Unexpected Message From File  to_executor  ${TEST RESOURCE PATH}unexpected.json
  Expect Message From File  to_executor   ${TEST RESOURCE PATH}expected.json
  Validate Received Messages  5  # in 5s
