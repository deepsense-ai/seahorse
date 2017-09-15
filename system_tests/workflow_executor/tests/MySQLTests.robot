# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  mySQLTests

*** Keywords ***
Connect To Docker Mysql Database
    Build Docker Image    system_tests/mysql    ${SUITE RESOURCE PATH}
    Kill Docker Container    mysql
    Remove Docker Container    mysql
    Run Docker Container    system_tests/mysql    mysql    -e    MYSQL_USER=system_tests    -e    MYSQL_PASS=pass    -p    3306:3306
    Delay    30.0
    Connect To Database    pymysql    mysql    system_tests    pass    localhost    3306

Disconnect From Docker Mysql Database
    Disconnect From Database
    Kill Docker Container    mysql

Custom Suite Setup
    Standard Suite Setup
    Connect To Docker Mysql Database

Custom Suite Teardown
    Disconnect From Docker Mysql Database
    Standard Suite Teardown


*** Settings ***
Suite Setup     Custom Suite Setup
Suite Teardown  Custom Suite Teardown

Test Setup      Standard Test Setup
Test Teardown   Standard Test Teardown

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/DockerClient.py
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Read Write MySQL
    Execute Sql Script    ${TEST RESOURCE PATH}fixture.sql
    Run Workflow Local    ${WORKFLOW PATH}   --jars ${SUITE RESOURCE PATH}mysql-connector-java-5.1.36.jar
    Check Report
    @{queryResultsIn} =    Query   SELECT * FROM read_write_mysql_in ORDER BY string_col;
    @{queryResultsOut} =    Query   SELECT * FROM read_write_mysql_out ORDER BY string_col;
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}
    Execute Sql Script    ${TEST RESOURCE PATH}cleanup.sql
