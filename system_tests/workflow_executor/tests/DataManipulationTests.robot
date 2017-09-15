# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Decompose Datetime
    ${dir} =    Set Variable    resources/dataManipulationTests/decomposeDatetime/
    Remove Hdfs Path    /system_tests/decomposeDatetime
    Upload to Hdfs    /system_tests/decomposeDatetime/input.csv    ${dir}input.csv
    Remove Directory    decomposeDatetimeOutput    recursive=yes
    Create Output Directory    decomposeDatetimeOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/decomposeDatetime

One Hot Encoder
    ${dir} =    Set Variable    resources/dataManipulationTests/oneHotEncoder/
    Remove Hdfs Path    /system_tests/oneHotEncoder
    Upload to Hdfs    /system_tests/oneHotEncoder/input.csv    ${dir}input.csv
    Remove Directory    oneHotEncoderOutput    recursive=yes
    Create Output Directory    oneHotEncoderOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/oneHotEncoder

SQL Expression
    ${dir} =    Set Variable    resources/dataManipulationTests/sqlExpression/
    Remove Hdfs Path    /system_tests/sqlExpression
    Upload to Hdfs    /system_tests/sqlExpression/input.csv    ${dir}input.csv
    Remove Directory    sqlExpressionOutput    recursive=yes
    Create Output Directory    sqlExpressionOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/sqlExpression

Invalid SQL Expression
    ${dir} =    Set Variable    resources/dataManipulationTests/invalidSqlExpression/
    Remove Hdfs Path    /system_tests/invalidSqlExpression
    Upload to Hdfs    /system_tests/invalidSqlExpression/input.csv    resources/dataManipulationTests/sqlExpression/input.csv
    Remove Directory    invalidSqlExpressionOutput    recursive=yes
    Create Output Directory    invalidSqlExpressionOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status    FAILED
    Check Error    ${dir}errorPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/invalidSqlExpression

Train Normalizer
    ${dir} =    Set Variable    resources/dataManipulationTests/trainNormalizer/
    Remove Hdfs Path    /system_tests/trainNormalizer
    Upload to Hdfs    /system_tests/trainNormalizer/frame1.csv    ${dir}frame1.csv
    Upload to Hdfs    /system_tests/trainNormalizer/frame2.csv    ${dir}frame2.csv
    Remove Directory    trainNormalizerOutput    recursive=yes
    Create Output Directory    trainNormalizerOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/trainNormalizer

Join and Split
    ${dir} =    Set Variable    resources/dataManipulationTests/joinAndSplit/
    Remove Hdfs Path    /system_tests/joinAndSplit
    Upload to Hdfs    /system_tests/joinAndSplit/frame1.csv    ${dir}frame1.csv
    Upload to Hdfs    /system_tests/joinAndSplit/frame2.csv    ${dir}frame2.csv
    Remove Directory    joinAndSplitOutput    recursive=yes
    Create Output Directory    joinAndSplitOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/joinAndSplit

Missing Values Handler
    ${dir} =    Set Variable    resources/dataManipulationTests/handleMissingValues/
    Remove Hdfs Path    /system_tests/handleMissingValues
    Upload to Hdfs    /system_tests/handleMissingValues/input.csv    ${dir}input.csv
    Remove Directory    handleMissingValuesOutput    recursive=yes
    Create Output Directory    handleMissingValuesOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/handleMissingValues
