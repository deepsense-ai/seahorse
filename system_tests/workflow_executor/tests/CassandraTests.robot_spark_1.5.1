# Copyright (c) 2015, CodiLime Inc.

*** Settings ***
Library    OperatingSystem
Library    Collections
Library    ../lib/CassandraClient.py
Library    ../lib/CassandraClient.py
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
    Run Workflow Local    ${dir}workflow.json    --conf spark.cassandra.connection.host=spark-cluster-1 --conf spark.cassandra.auth.username=cassandra --conf spark.cassandra.auth.password=cassandra

    Check Execution Status
    Check Report    ${dir}expectedReportPattern.json
    @{queryResultsIn} =    Execute Cql String    SELECT * FROM system_tests.test_in;
    @{queryResultsOut} =    Execute Cql String    SELECT * FROM system_tests.test_out;
    Should Be Equal As Strings    ${queryResultsIn}    ${queryResultsOut}

    Clean Output Directory
    Execute Cql String    DROP KEYSPACE IF EXISTS system_tests;
