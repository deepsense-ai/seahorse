# Copyright (c) 2015, CodiLime Inc.

*** Keywords ***
Start Docker With Cassandra Database
    Build Docker Image    system_tests/cassandra    resources/cassandraTests
    Kill Docker Container    cassandra
    Remove Docker Container    cassandra
    Run Docker Container    system_tests/cassandra    cassandra    -p    7000:7000    -p    7001:7001    -p    7199:7199    -p    9042:9042    -p    9160:9160
    Delay    30.0

Stop Docker With Cassandra Database
    Kill Docker Container    cassandra


*** Settings ***
Suite Setup       Start Docker With Cassandra Database
Suite Teardown    Stop Docker With Cassandra Database

Library    Collections
Library    OperatingSystem
Library    ../lib/CassandraClient.py
Library    ../lib/DockerClient.py
Library    ../lib/WorkflowExecutorClient.py


*** Test Cases ***
Read Write Cassandra
    ${dir} =    Set Variable    resources/cassandraTests/readWriteCassandra/
    Execute Cql String    DROP KEYSPACE IF EXISTS system_tests;
    Execute Cql String    CREATE KEYSPACE IF NOT EXISTS system_tests WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};
    Execute Cql String    CREATE TABLE system_tests.test_in ( string_col text, numeric_col double, categorical_col text, timestamp_col timestamp, boolean_col boolean, primary key (string_col, numeric_col));
    Execute Cql String    CREATE TABLE system_tests.test_out (string_col text, numeric_col double, categorical_col text, timestamp_col timestamp, boolean_col boolean, primary key (string_col, numeric_col));
    Execute Cql String    INSERT INTO system_tests.test_in (string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_one', 2.3461, 'A', '2013-08-05 18:19:01', true);
    Execute Cql String    INSERT INTO system_tests.test_in (string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_two', 4.1334, 'A', '2013-08-05 18:19:02', false);
    Execute Cql String    INSERT INTO system_tests.test_in (string_col, numeric_col, categorical_col, timestamp_col, boolean_col) VALUES ('str_three', 7.41, 'B', '2013-08-05 18:19:03', true);

    Remove Directory    readWriteCassandraOutput    recursive=yes
    Create Output Directory    readWriteCassandraOutput
    Run Workflow Local    ${dir}workflow.json    --conf spark.cassandra.connection.host=localhost --conf spark.cassandra.auth.username=cassandra --conf spark.cassandra.auth.password=cassandra

    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    @{queryResultsIn} =    Execute Cql String    SELECT * FROM system_tests.test_in;
    @{queryResultsOut} =    Execute Cql String    SELECT * FROM system_tests.test_out;
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}

    Clean Output Directory
    Execute Cql String    DROP KEYSPACE IF EXISTS system_tests;
