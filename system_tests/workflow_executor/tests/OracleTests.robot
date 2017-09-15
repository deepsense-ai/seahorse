# Copyright (c) 2015, CodiLime Inc.

*** Keywords ***
Connect To Docker Oracle Database
    Pull Docker Image    sath89/oracle-xe-11g
    Kill Docker Container    oracle
    Remove Docker Container    oracle
    Run Docker Container    sath89/oracle-xe-11g    oracle    -p    8080:8080    -p    1521:1521
    Delay    100.0
    Connect To Database Using Custom Params    cx_Oracle    user='system',password='oracle',dsn='localhost'

Disconnect From Docker Oracle Database
    Disconnect From Database
    Kill Docker Container    oracle


*** Settings ***
Suite Setup       Connect To Docker Oracle Database
Suite Teardown    Disconnect From Docker Oracle Database

Library    Collections
Library    DatabaseLibrary
Library    OperatingSystem
Library    ../lib/DockerClient.py
Library    ../lib/HdfsClient.py
Library    ../lib/S3Client.py
Library    ../lib/WorkflowExecutorClient.py

*** Test Cases ***
Read Write Oracle
    ${dir} =    Set Variable    resources/oracleTests/readWriteOracle/
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_in
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_out
    Execute Sql String    CREATE TABLE read_write_in (timestamp_col TIMESTAMP)
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:01', 'RRRR-MM-DD HH24-MI-SS'))
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:02', 'RRRR-MM-DD HH24-MI-SS'))
    Execute Sql String    INSERT INTO read_write_in(timestamp_col) VALUES (TO_TIMESTAMP('2013-08-05 18:19:03', 'RRRR-MM-DD HH24-MI-SS'))

    Remove Directory    readWriteOracleOutput    recursive=yes
    Create Output Directory    readWriteOracleOutput
    Run Workflow Local    ${dir}workflow.json    --jars ${dir}../ojdbc6.jar

    Check Execution Status
    @{queryResultsIn} =    Query   SELECT * FROM read_write_in ORDER BY timestamp_col
    @{queryResultsOut} =    Query   SELECT * FROM read_write_out ORDER BY timestamp_col
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}
    Check Report    ${dir}expectedReportPattern.json

    Clean Output Directory
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_in;
    Run Keyword And Ignore Error    Execute Sql String    DROP TABLE read_write_out;
