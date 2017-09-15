# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Missing Input Data
    ${dir} =    Set Variable    resources/negativeTests/missingInputData/
    Remove Directory    missingInputDataOutput    recursive=True
    Create Output Directory    missingInputDataOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Missing Parameter Value
    ${dir} =    Set Variable    resources/negativeTests/missingParamValue/
    Remove Directory    missingParamValueOutput    recursive=True
    Create Output Directory    missingParamValueOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Parameter Out Of Range
    ${dir} =    Set Variable    resources/negativeTests/paramOutOfRange/
    Remove Directory    paramOutOfRangeOutput    recursive=True
    Create Output Directory    paramOutOfRangeOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Type Mismatch
    ${dir} =    Set Variable    resources/negativeTests/typeMismatch/
    Remove Directory    typeMismatchOutput    recursive=True
    Create Output Directory    typeMismatchOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Empty Column Selector
    ${dir} =    Set Variable    resources/negativeTests/emptyColumnSelector/
    Remove Directory    emptyColumnSelectorOutput    recursive=True
    Create Output Directory    emptyColumnSelectorOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Column Name Duplication
    [Timeout]    1m
    ${dir} =    Set Variable    resources/negativeTests/columnNameDuplication/
    Remove Hdfs Path    /system_tests/columnNameDuplication
    Upload to Hdfs    /system_tests/columnNameDuplication/input.csv    ${dir}input.csv
    Remove Directory    columnNameDuplicationOutput    recursive=True
    Create Output Directory    columnNameDuplicationOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/columnNameDuplication

One Category Feature
    ${dir} =    Set Variable    resources/negativeTests/oneCategoryFeature/
    Remove Hdfs Path    /system_tests/oneCategoryFeature
    Upload to Hdfs    /system_tests/oneCategoryFeature/input.csv    ${dir}input.csv
    Remove Directory    oneCategoryFeatureOutput    recursive=True
    Create Output Directory    oneCategoryFeatureOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/oneCategoryFeature

Trivial Cycle
    [Timeout]    20s
    ${dir} =    Set Variable    resources/negativeTests/trivialCycle/
    Remove Directory    trivialCycleOutput    recursive=True
    Create Output Directory    trivialCycleOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory

Cycle
    [Timeout]    20s
    ${dir} =    Set Variable    resources/negativeTests/cycle/
    Remove Directory    cycleOutput    recursive=True
    Create Output Directory    cycleOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory
