# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
K Means
    ${dir} =    Set Variable    resources/clusteringTests/kMeans/
    Remove Hdfs Path    /system_tests/kMeans
    Upload to Hdfs    /system_tests/kMeans/input.csv    ${dir}input.csv
    Remove Directory    kMeansOutput    recursive=yes
    Create Output Directory    kMeansOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/kMeans
