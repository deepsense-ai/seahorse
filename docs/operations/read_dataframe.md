---
layout: global
displayTitle: Read DataFrame
title: Read DataFrame
description: Read DataFrame
includeOperationsMenu: true
---

Reads a `DataFrame` from a specified data storage.

* Reading files (in CSV, JSON or PARQUET formats) from HDFS is supported (reading Hadoop-compatible partitioned files is supported).
Path to the file needs to be prefixed with ``hdfs://``.

* CSV and JSON files can be also loaded from local computer using **Files Library**.
File uploaded to the library can be selected with the widget in the ``source`` parameter.

* HTTP/HTTPS and FTP URLs are supported. E.g. specifying
``https://s3.amazonaws.com/workflowexecutor/examples/data/transactions.csv`` as the ``source`` parameter will result
in downloading the example file from HTTP server.

* Reading data from JDBC-compatible databases is supported.
This functionality requires placing adequate JDBC driver JAR file to Seahorse shared folder `jars`.
That file placement has to be performed before starting editing workflow that uses JDBC connection
(otherwise, it will be required to stop running session and start it again).

For detailed information on using custom JDBC drivers in the Batch Workflow Executor, visit
[Custom JDBC drivers](../productionizing.html#custom-jdbc-drivers) section.


## Available File Formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

In this mode, the operation infers column types.
When a column contains values of multiple types, the narrowest possible type will be chosen,
so that all the values can be represented in that type.
Empty cells are treated as ``null``, unless column type is inferred as a ``String`` - in this
case, they are treated as empty strings.

If the `convert to boolean` mode is enabled, the columns that contain only zeros, ones or empty values will be
inferred as `Boolean`.
In particular, a column consisting of empty cells will be inferred as ``Boolean`` with ``null`` values only.

The operation assumes that each row in the file has the same number of fields.
When this condition is not met, the behavior of the operation is undefined.

If the file defines column names, they will be used in the output `DataFrame`.
If a name is empty for some column, the column will
be named ``unnamed_X`` (where ``X`` is the smallest non-negative number so that
column names are unique). If names are not included in the input file
or are all empty, the columns will be named ``unnamed_X`` where ``X`` are
consecutive integers beginning from 0.

Escaping of a column separator can be achieved with double quotes sign.
For example, assuming a comma as separator, the following line

<code>1,abc,"a,b,c","""x""",, z ," z&nbsp;&nbsp;"</code>

will be parsed as:

``1.0``  ``abc``  ``a,b,c``  ``"x"`` <code>&nbsp;</code> ``_z_``  ``_z__``

where ``_`` denotes a space and the fifth value is an empty string. Note, that ``"""x"""`` is being
parsed as ``"x"``, since ``""`` inside an already quoted value translates to ``"``. In other words,
escaping a ``"`` sign inside a quoted value is done by prefixing it with another ``"``.

### `PARQUET`
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#parquet-files">Parquet Files</a>

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve the order of columns.

`Timestamp` columns are converted to `String` columns
(values of that columns are converted to its string representations by Apache Spark).


**Since**: Seahorse 0.4.0

## Input

The `Read DataFrame` operation does not take any input.

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
      <td>The data read from the source as a <code>DataFrame</code>.</td>
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
        <code>data storage type</code>
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
        <code>source</code>
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
        <code>format</code>
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
        <code>separator</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#single-choice">Single Choice</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        The character separating fields in a row. Possible values are:
        <code>Comma</code>, <code>Semicolon</code>, <code>Colon</code>,
        <code>Space</code>, <code>Tab</code>, <code>Custom</code>.
        Default value: <code>Comma</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code>custom separator</code>
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
        <code>names included</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then values in the first row will be used as columns'
        names in the output <code>DataFrame</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code>convert to boolean</code>
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
        <code>url</code>
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
        <code>driver</code>
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
        <code>table</code>
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
