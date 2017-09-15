---
layout: documentation
displayTitle: Read DataFrame
title: Read DataFrame
description: Read DataFrame
includeOperationsMenu: true
---

Reads a DataFrame from a specified data storage.

It supports reading files (CSV, JSON or PARQUET) from local file system, Amazon S3 and HDFS
(it supports reading Hadoop-compatible partitioned files).

Additionally, HTTP/HTTPS and FTP URLs are supported. E.g. specifying
``https://seahorse.deepsense.io/_static/transactions.csv`` as ``source`` parameter will download
the example file from HTTP server.

It also supports reading data from JDBC compatible databases.
For more detailed information on using JDBC drivers in the Batch Workflow Executor, visit
[Custom JDBC drivers](../batch_workflow_executor_overview.html#custom-jdbc-drivers) section.
If you are using the Bundled Image please read about
[JDBC drivers included in the Bundled Image](../bundled_image_overview.html#bundled-jdbc-drivers).

## Available File Formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

In this mode, the operation infers column types.
When a column contains values of different types, the narrowest possible type will be chosen,
so that all the values can be represented in that type.
Empty cells are treated as ``null``, unless column type is inferred as a ``String`` - in this
case, they are treated as empty strings.

If `convert to boolean` mode is enabled, columns that contain only zeros, ones and empty values will be
inferred as `Boolean`.
In particular, column consisting of empty cells will be inferred as ``Boolean`` containing ``null`` values only.

The operation assumes that each row in file has the same number of fields.
In other case, behavior of operation is undefined.

If the file defines column names, they will be used in the output DataFrame.
If a name is missing (or is empty) for some column then the column will
be named ``unnamed_X`` (where ``X`` is the smallest non-negative number so that
column names are unique). In case names are not included in the input file
or are all empty then the columns will be named ``unnamed_X`` where ``X`` are
consecutive integers beginning from 0.

Escaping of a separator can be achieved by double quotes sign.
For example, assuming a comma as separator, following line

<code>1,abc,"a,b,c","""x""",, z ," z&nbsp;&nbsp;"</code>

will be parsed as:

``1.0``  ``abc``  ``a,b,c``  ``"x"`` <code>&nbsp;</code> ``_z_``  ``_z__``

where ``_`` denotes a space and the fifth value is an empty string.

### `PARQUET`
<a target="_blank" href="http://spark.apache.org/docs/latest/sql-programming-guide.html#parquet-files">Parquet Files</a>

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve column order.

Timestamp columns are converted to string columns
(values of that columns are converted to its string representations by Apache Spark).


**Since**: Seahorse 0.4.0

## Input

Read DataFrame does not take any input.

## Output

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
      <td>Data from the loaded file as a DataFrame</td>
    </tr>
  </tbody>
</table>


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
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>The input data storage type. Possible values are:
        <code>FILE</code>, <code>JDBC</code>.
      </td>
    </tr>
    <tr>
      <td>
        <code id="source">source</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        A path to the input file.
      </td>
    </tr>
    <tr>
      <td>
        <code id="format">format</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>Valid only if <code>data storage type = FILE</code>.
        The input file format. Possible values are:
        <code>CSV</code>, <code>PARQUET</code>, <code>JSON</code>.
      </td>
    </tr>
    <tr>
      <td>
        <code id="separator">separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        Character separating fields in a row. Possible values are:
        <code>Comma</code>, <code>Semicolon</code>, <code>Colon</code>,
        <code>Space</code>, <code>Tab</code>, <code>Custom</code>.
        Default value: <code>Comma</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code id="custom-separator">custom separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>Valid only if <code>separator = Custom</code>.
        A custom column separator.
      </td>
    </tr>

    <tr>
      <td>
        <code id="names-included">names included</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then values in the first row will be used as columns'
        names in the output DataFrame.
      </td>
    </tr>

    <tr>
      <td>
        <code id="convert-to-boolean">convert to boolean</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then columns containing only zeros, ones and empty cells will be inferred as <code>Boolean</code>.
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
      <td>Valid only if <code>data storage type = JDBC</code>.
        JDBC table's name.
      </td>
    </tr>
  </tbody>
</table>
