---
layout: global
displayTitle: Write DataFrame
title: Write DataFrame
description: Write DataFrame
usesMathJax: true
includeOperationsMenu: true
---

The `Write DataFrame` operation saves a [DataFrame](../classes/dataframe.html) to a specified data
storage.

It supports writing files (in CSV, JSON or PARQUET formats) to HDFS.
The output is a Hadoop-compatible partitioned file.
Path to the file needs to be prefixed with ``hdfs://``.

CSV and JSON files can be also saved to **Files Library** using widget in the ``output file`` parameter.
Files in the library can be downloaded to local computer.

It is possible to customize the file format (e.g. the values separator in CSV format)
by setting appropriate parameters.

It also supports writing data to JDBC-compatible databases.
This functionality requires placing adequate JDBC driver JAR file to Seahorse shared folder `jars`.
That file placement has to be performed before starting editing workflow that uses JDBC connection
(otherwise, it will be required to stop running session and start it again).

For detailed information on using custom JDBC drivers in the Batch Workflow Executor, visit
[Custom JDBC drivers](../productionizing.html#custom-jdbc-drivers) section.


## Available File Formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

### `PARQUET`
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#parquet-files">Parquet</a>
format does not allow using characters ``, ;{}()\n\t=`` in column names.

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve the order of columns.

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
      <td>The <code>DataFrame</code> to save.</td>
    </tr>
  </tbody>
</table>

## Output

The `Write DataFrame` operation does not produce any output.

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
        <code>data storage type</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>The output data storage type. Possible values are:
        <code>FILE</code>, <code>JDBC</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code>output file</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        The path where the output file will be saved.
      </td>
    </tr>
    <tr>
      <td>
        <code>format</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        The format of the output file. Possible values:
        <code>CSV</code>, <code>PARQUET</code>, <code>JSON</code>.
      </td>

    </tr>
    <tr>
      <td>
        <code>separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        A character separating fields in a row. Possible values are:
        <code>Comma</code>, <code>Semicolon</code>, <code>Colon</code>,
        <code>Space</code>, <code>Tab</code>, <code>Custom</code>.
        The default value: <code>Comma</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code>custom separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>column separator = Custom</code>.
        A custom column separator.
      </td>
    </tr>

    <tr>
      <td>
        <code>names included</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then the first row of the output file will contain columns' names.
      </td>
    </tr>

    <tr>
      <td>
        <code>url</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code>.
        The JDBC connection URL.
      </td>
    </tr>
    <tr>
      <td>
        <code>driver</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code>.
        The JDBC driver ClassName.
      </td>
    </tr>
    <tr>
      <td>
        <code>table</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = JDBC</code>.
        The JDBC table's name.
        A table with an appropriate schema will be created; if the table already exists, an exception will be thrown.
      </td>
    </tr>
  </tbody>
</table>
