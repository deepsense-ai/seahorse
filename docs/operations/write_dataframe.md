---
layout: documentation
displayTitle: Write DataFrame
title: Write DataFrame
description: Write DataFrame
usesMathJax: true
includeOperationsMenu: true
---

Write DataFrame saves a DataFrame to specified data storage type.

Supports writing files (CSV, JSON or PARQUET) to local file system, Amazon S3 and HDFS.
Output will be Hadoop-compatible partitioned file.
It is possible to customize the file format (e.g. the values separator in CSV format)
by setting appropriate parameters.

It also supports writing data to JDBC compatible databases and Cassandra. <BR/>
For more detailed information on using JDBC driver, visit
[Custom JDBC drivers](../workflowexecutor.html#custom-jdbc-drivers) section. <BR/>
For more detailed information on using Cassandra, visit
[Cassandra configuration](../workflowexecutor.html#cassandra-configuration) section.


## Available file formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

### `PARQUET`
<a target="_blank" href="http://spark.apache.org/docs/latest/sql-programming-guide.html#parquet-files">Parquet Files</a>
do not allow using characters "` ,;{}()\n\t=`" in column names.

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve column order.

`Null` values in JSON are omitted. This might result in schema mismatch if all values in particular
column are `null` (that column will be omitted in output JSON file).

Timestamp columns are converted to string columns
(values of that columns are converted to its string representations by Apache Spark).



**Since**: Seahorse 0.4.0

## Input

<table>
  <thead>
    <tr>
      <th style="width:20%">Port</th>
      <th style="width:25%">Type Qualifier</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>0</code>
      </td>
      <td>
        <code>
        <a href="../classes/dataframe.html">DataFrame</a>
        </code>
      </td>
      <td>A DataFrame to save</td>
    </tr>
  </tbody>
</table>

## Output

Write DataFrame operation does not produce any output.

## Parameters

<table class="table">
  <thead>
    <tr>
      <th style="width:20%">Name</th>
      <th style="width:25%">Type</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code id="data-storage-type">data storage type</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single_choice">Choice</a></code>
      </td>
      <td>The input data storage type. Possible values are:
        <code>FILE</code>, <code>JDBC</code>, <code>CASSANDRA</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code id="output-file">output file</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        A path where the output file will be saved.
      </td>
    </tr>
    <tr>
      <td>
        <code id="format">format</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single_choice">Choice</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        A format of the output file. Possible values:
        <code>CSV</code>, <code>PARQUET</code>, <code>JSON</code>.
      </td>

    </tr>
    <tr>
      <td>
        <code id="column-separator">column separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single_choice">Choice</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        Character separating fields in a row. Possible values are:
        <code>Comma</code>, <code>Semicolon</code>, <code>Colon</code>,
        <code>Space</code>, <code>Tab</code>, <code>Custom column separator</code>.
        Default value: <code>Comma</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code id="custom-column-separator">custom column separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>column separator = Custom column separator</code>.
        A custom column separator.
      </td>
    </tr>

    <tr>
      <td>
        <code id="write-header">write header</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then columns' names will be written at the
        beginning of the output file.
      </td>
    </tr>

    <tr>
      <td>
        <code id="url">url</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code>.
        JDBC connection URL.
        Sensitive data (e.g. user, password) could be replaced on the fly during workflow execution,
        see: <a href="../parameter_types.html#string">String parameter documentation</a> for more details.
      </td>
    </tr>
    <tr>
      <td>
        <code id="driver">driver</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code>.
        JDBC driver ClassName.
      </td>
    </tr>
    <tr>
      <td>
        <code id="table">table</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code> or <code>data storage type = CASSANDRA</code>.
        JDBC/Cassandra table name.
        In case of JDBC, table with appropriate schema will be created; if table already exists, exception will be thrown.
        In case of Cassandra, table must already exist, match dataframe schema and be empty.
      </td>
    </tr>
    <tr>
      <td>
        <code id="keyspace">keyspace</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = CASSANDRA</code>.
        Cassandra keyspace.
      </td>
    </tr>
  </tbody>
</table>
