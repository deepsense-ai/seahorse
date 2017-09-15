# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Ridge Regression
    ${dir} =    Set Variable    resources/regressionTests/ridgeRegression/
    Remove Hdfs Path    /system_tests/ridgeRegression
    Upload To Hdfs    /system_tests/ridgeRegression/training.csv    resources/regressionTests/training.csv
    Upload To Hdfs    /system_tests/ridgeRegression/test.csv    resources/regressionTests/test.csv
    Remove Directory    ridgeRegressionOutput    recursive=yes
    Create Output Directory    ridgeRegressionOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    resources/regressionTests/expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/ridgeRegression

Random Forest
    ${dir} =    Set Variable    resources/regressionTests/randomForest/
    Remove Hdfs Path    /system_tests/randomForest
    Upload To Hdfs    /system_tests/randomForest/training.csv    resources/regressionTests/training.csv
    Upload To Hdfs    /system_tests/randomForest/test.csv    resources/regressionTests/test.csv
    Remove Directory    randomForestOutput    recursive=yes
    Create Output Directory    randomForestOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    resources/regressionTests/expectedReportPattern.json
    Clean Output Directory
    Remove Hdfs Path    /system_tests/randomForest

Gradient Boosted Trees
    ${dir} =    Set Variable    resources/regressionTests/gradientBoostedTrees/
    Remove Hdfs Path    /system_tests/gradientBoostedTrees
    Upload To Hdfs    /system_tests/gradientBoostedTrees/training.csv    resources/regressionTests/training.csv
    Upload To Hdfs    /system_tests/gradientBoostedTrees/test.csv    resources/regressionTests/test.csv
    Remove Directory    gradientBoostedTreesOutput    recursive=yes
    Create Output Directory    gradientBoostedTreesOutput
    Run Workflow    ${dir}workflow.json
    Check Execution Status
    Check Report    resources/regressionTests/expectedReportPattern.json
    Remove Hdfs Path    /system_tests/gradientBoostedTrees
