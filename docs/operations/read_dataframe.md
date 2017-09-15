---
layout: documentation
displayTitle: Read DataFrame
title: Read DataFrame
description: Read DataFrame
includeOperationsMenu: true
---

Reads a DataFrame from a file in specified format. Supports reading files from
a local file system, Amazon S3 and from HDFS.


## Available file formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

In this mode, the operation infers column types.
When a column contains values of different types, the narrowest possible type will be chosen,
so that all the values can be represented in that type.
Empty cells are treated as ``null``, unless column type is inferred as ``String`` - in this
case, they are treated as empty strings.

If `convert to boolean` mode is enabled, columns that contain only zeros, ones and empty values will be
inferred as `Boolean`.
In particular, column consisting of empty cells will be inferred as ``Boolean`` containing ``null`` values only.

Operation assumes that each row in file has the same number of fields.
In other case, behavior of operation is undefined.

If the file defines column names they will be used in the output DataFrame.
If a name is missing (or is empty) for some column then the column will
be named ``unnamed_X`` (where ``X`` is the smallest positive number so that
column names are unique). In case names are not included in the input file
or are all empty then the columns will be named ``unnamed_X`` where ``X`` are
consecutive integers beginning from 0.

Escaping of separator sign is done by double quotes sign.
Moreover, all not escaped values will be trimmed before parsing.
For example, assuming comma as separator, following line

``1,abc,"a,b,c",""x"",, z  ," z  "``

will be parsed as:

``1``  ``abc``  ``a,b,c``  ``"x"`` ``_``  ``z`` ``_z__``

where ``_`` denotes space.

### `PARQUET`
<a target="_blank" href="http://spark.apache.org/docs/latest/sql-programming-guide.html#parquet-files">Parquet Files</a>

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve column order.

Timestamp columns are converted to string columns
(values of that columns are converted to its string representations by Apache Spark).

It is possible to select columns to be Categorical
(by index, by name or by inferred type) using ``column selector``.
When categorizing a non-string column all values will be cast to strings and trimmed first.



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
        <code id="ratio">source</code>
      </td>
      <td>
        <code><a href="../parameters.html#string">String</a></code>
      </td>
      <td>A path to the input file.</td>

    <tr>
      <td>
        <code id="seed">format</code>
      </td>
      <td>
        <code><a href="../parameters.html#single_choice">Choice</a></code>
      </td>
      <td>The input file format. Possible values are:
        <code>CSV</code>, <code>PARQUET</code>, <code>JSON</code>.
      </td>
    </tr>

    </tr>
    <tr>
      <td>
        <code id="seed">separator</code>
      </td>
      <td>
        <code><a href="../parameters.html#single_choice">Choice</a></code>
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
        <code id="seed">custom column separator</code>
      </td>
      <td>
        <code><a href="../parameters.html#string">String</a></code>
      </td>
      <td>Valid only if <code>separator = Custom column separator</code>.
        A custom column separator.
      </td>
    </tr>

    <tr>
      <td>
        <code id="seed">names included</code>
      </td>
      <td>
        <code><a href="../parameters.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then values in the first row will be used as columns'
        names in the output DataFrame.
      </td>
    </tr>

    <tr>
      <td>
        <code id="convert_to_boolean">convert to boolean</code>
      </td>
      <td>
        <code><a href="../parameters.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then columns containing only zeros, ones and empty cells will be inferred as <code>Boolean</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code id="seed">categorical columns</code>
      </td>
      <td>
        <code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code> or <code>format = JSON</code>.
        Selects the columns that should be converted to <code>Categorical</code> type.
      </td>
    </tr>

    <tr>
      <td>
        <code id="seed">line separator</code>
      </td>
      <td>
        <code><a href="../parameters.html#single_choice">Choice</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        A line separator used in the input file. Possible values:
        <code>UNIX</code>,
        <code>WINDOWS</code>,
        <code>CUSTOM</code>. The last one allows to set a custom line separator.
      </td>
    </tr>

    </tr>
    <tr>
      <td>
        <code id="seed">custom line separator</code>
      </td>
      <td>
        <code><a href="../parameters.html#string">String</a></code>
      </td>
      <td>Valid only if <code>line separator = CUSTOM</code>.
        A line separator used in the input file.
      </td>
    </tr>

  </tbody>
</table>
