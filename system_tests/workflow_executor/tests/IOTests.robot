# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Read Write Local Filesystem
    ${dir} =    Set Variable    resources/ioTests/readWriteLocal/
    Remove Directory    readWriteLocalOutput    recursive=yes
    Create Output Directory    readWriteLocalOutput
    Run Workflow Local    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory

Read Write HDFS
    ${dir} =    Set Variable    resources/ioTests/readWriteHdfs/
    Remove Hdfs Path    /system_tests/readWriteHdfs
    Upload to Hdfs    /system_tests/readWriteHdfs/input.csv    ${dir}input.csv
    Remove Directory    readWriteHdfsOutput    recursive=yes
    Create Output Directory    readWriteHdfsOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/readWriteHdfs

Read Write S3
    ${dir} =    Set Variable    resources/ioTests/readWriteS3/
    Remove From S3    system_tests/ioTests
    Upload to S3    system_tests/ioTests/input.csv    ${dir}input.csv
    Remove Directory    readWriteS3Output    recursive=yes
    Create Output Directory    readWriteS3Output
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove From S3    system_tests/ioTests

Missing HDFS File
    ${dir} =    Set Variable    resources/ioTests/readWriteHdfs/
    Remove Hdfs Path    /system_tests/readWriteHdfs
    Remove Directory    missingHdfsFileOutput    recursive=yes
    Create Output Directory    missingHdfsFileOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}/errorPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/readWriteHdfs

No Write Permissions
    ${dir} =    Set Variable    resources/ioTests/noWritePermissions/
    Remove Directory    noWritePermissionsOutput    recursive=yes
    Create Output Directory    noWritePermissionsOutput
    Run Workflow Local    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}/errorPattern.json
    Clean Output Directory

Read Write Local Filesystem Json Format
    ${dir} =    Set Variable    resources/ioTests/readWriteLocalJson/
    Remove Directory    readWriteLocalOutputJson    recursive=yes
    Create Output Directory    readWriteLocalOutputJson
    Run Workflow Local    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory

Read Write Local Filesystem Parquet Format
    ${dir} =    Set Variable    resources/ioTests/readWriteLocalParquet/
    Remove Directory    readWriteLocalOutputParquet    recursive=yes
    Create Output Directory    readWriteLocalOutputParquet
    Run Workflow Local    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
