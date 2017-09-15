# Copyright (c) 2015, CodiLime Inc.

*** Variables ***
${SUITE} =  oracleTests

*** Keywords ***
Connect To Docker Oracle Database
    Pull Docker Image    wnameless/oracle-xe-11g
    Kill Docker Container    oracle
    Remove Docker Container    oracle
    Run Docker Container    wnameless/oracle-xe-11g    oracle    -p    49160:22    -p    1521:1521
    Delay    100.0
    Connect To Database Using Custom Params    cx_Oracle    user='system',password='oracle',dsn='localhost'
Disconnect From Docker Oracle Database
    Disconnect From Database
    Kill Docker Container    oracle

Custom Suite Setup
    Standard Suite Setup
    Connect To Docker Oracle Database
Custom Suite Teardown
    Standard Suite Teardown
    Disconnect From Docker Oracle Database

Drop Test Tables
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_in
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_out

Custom Test Setup
    Standard Test Setup
    Drop Test Tables
Custom Test Teardown
    Standard Suite Teardown
    Drop Test Tables

*** Settings ***
Suite Setup     Custom Suite Setup
Suite Teardown  Custom Suite Teardown

Test Setup      Custom Test Setup
Test Teardown   Custom Test Teardown

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/DockerClient.py
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py
Library    ../lib/CommonSetupsAndTeardowns.py


*** Test Cases ***
Read Write Oracle
    Execute Sql String    CREATE TABLE read_write_in (timestamp_col TIMESTAMP)
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:01', 'RRRR-MM-DD HH24-MI-SS'))
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:02', 'RRRR-MM-DD HH24-MI-SS'))
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:03', 'RRRR-MM-DD HH24-MI-SS'))
    Run Workflow Local    ${WORKFLOW PATH}    --jars ${SUITE RESOURCE PATH}ojdbc6.jar
    @{queryResultsIn} =    Query   SELECT * FROM read_write_in ORDER BY timestamp_col
    @{queryResultsOut} =    Query   SELECT * FROM read_write_out ORDER BY timestamp_col
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}
    Check Report
