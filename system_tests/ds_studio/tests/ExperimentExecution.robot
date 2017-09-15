# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/HdfsClient.py
Library    ../lib/ExperimentManagerClient.py
Library    ../lib/ReportChecker.py
Library    ../lib/DeployModelServiceClient.py

*** Test Cases ***
Location Attractiveness
   ${dir} =    Set Variable    resources/locationAttractiveness/
   Upload To Hdfs    /system_tests/file.csv    ${dir}files/hotels_model_matrix_head_10.csv
   ${id} =    Create Experiment From File    ${dir}experiment.json
   Launch Experiment    ${id}
   Wait For Experiment End    ${id}    timeout=600
   ${exp} =    Get Experiment    ${id}
   Experiment Status Should Be    ${exp}    COMPLETED
   Check Experiment Reports    ${exp}    ${dir}reports_pattern.json
   Delete Experiment    ${id}
   Remove Hdfs Path    /system_tests/

Deploy Model Service (Ridge Regression + Train Regressor)
    ${regressionNode} =    Set Variable    0777f6e1-9cbb-be45-cac4-ec903da8af45
    ${dir} =    Set Variable    resources/deployModel/
    Upload To Hdfs    /system_tests/file.csv    ${dir}files/almost_linear_function.csv
    ${id} =    Create Experiment From File    ${dir}experiment.json
    Launch Experiment    ${id}
    Wait For Experiment End    ${id}    timeout=600
    ${exp} =    Get Experiment    ${id}
    Experiment Status Should Be    ${exp}    COMPLETED
    ${trainedModelId} =    Result Of Node    ${exp}    ${regressionNode}
    ${deployedModelId} =    Deploy Model    ${trainedModelId}
    Assert Scoring Correct   ${deployedModelId}    ${dir}expected_scoring.json
