# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Empty Workflow
    ${dir} =    Set Variable    resources/simpleTests/emptyWorkflow/
    Remove Directory    emptyWorkflowOutput    recursive=True
    Create Output Directory    emptyWorkflowOutput
    Run Workflow Local    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory

