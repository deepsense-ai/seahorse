# Copyright (c) 2015, CodiLime Inc.

*** Keywords ***
Connect To Docker Mysql Database
    Build Docker Image    system_tests/mysql    resources/mySQLTests
    Kill Docker Container    mysql
    Remove Docker Container    mysql
    Run Docker Container    system_tests/mysql    mysql    -e    MYSQL_USER=system_tests    -e    MYSQL_PASS=pass    -p    3306:3306
    Delay    30.0
    Connect To Database    pymysql    mysql    system_tests    pass    localhost    3306

Disconnect From Docker Mysql Database
    Disconnect From Database
    Kill Docker Container    mysql


*** Settings ***
Suite Setup       Connect To Docker Mysql Database
Suite Teardown    Disconnect From Docker Mysql Database

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/DockerClient.py
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py


*** Test Cases ***
Read Write MySQL
    ${dir} =    Set Variable    resources/mySQLTests/readWriteMySQL/
    Execute Sql Script    ${dir}fixture.sql

    Remove Directory    readWriteMySQLOutput    recursive=yes
    Create Output Directory    readWriteMySQLOutput
    Run Workflow Local    ${dir}workflow.json    --jars ${dir}../mysql-connector-java-5.1.36.jar

    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    @{queryResultsIn} =    Query   SELECT * FROM read_write_mysql_in ORDER BY string_col;
    @{queryResultsOut} =    Query   SELECT * FROM read_write_mysql_out ORDER BY string_col;
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}

    Clean Output Directory
    Execute Sql Script    ${dir}cleanup.sql
