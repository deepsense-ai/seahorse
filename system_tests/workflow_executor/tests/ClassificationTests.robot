# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Point Cloud
    ${dir} =    Set Variable    resources/classificationTests/pointCloud/
    Remove Hdfs Path    /system_tests/pointCloud
    Upload to Hdfs    /system_tests/pointCloud/input.csv    ${dir}input.csv
    Remove Directory    pointCloudOutput    recursive=yes
    Create Output Directory    pointCloudOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Clean Output Directory
    Remove Hdfs Path    /system_tests/pointCloud

Point Cloud Using Random Forest Classification
    ${dir} =    Set Variable    resources/classificationTests/pointCloud2/
    Remove Hdfs Path    /system_tests/pointCloud2
    Upload to Hdfs    /system_tests/pointCloud2/input.csv    ${dir}input.csv
    Remove Directory    pointCloud2Output    recursive=yes
    Create Output Directory    pointCloud2Output
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Clean Output Directory
    Remove Hdfs Path    /system_tests/pointCloud2
