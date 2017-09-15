---
layout: documentation
displayTitle: Write DataFrame
title: Write DataFrame
description: Write DataFrame
usesMathJax: true
includeOperationsMenu: true
---

Write DataFrame saves a DataFrame on a file system.
It is possible to customize the file format (e.g. the values separator in CSV format)
by setting appropriate parameters.

## Available file formats

### `CSV`
<a target="_blank" href="https://en.wikipedia.org/wiki/Comma-separated_values">Comma-separated values</a>

### `PARQUET`
<a target="_blank" href="http://spark.apache.org/docs/latest/sql-programming-guide.html#parquet-files">Parquet Files</a>
do not allow using characters "` ,;{}()\n\t=`" in column names.

### `JSON`
<a target="_blank" href="https://en.wikipedia.org/wiki/JSON">JSON</a>
file format does not preserve column order.

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
        <code id="ratio">format</code>
      </td>
      <td>
        <code><a href="../parameters.html#single_choice">Choice</a></code>
      </td>
      <td>
        A format of the output file. Possible values:
        <code>CSV</code>, <code>PARQUET</code>, <code>JSON</code>.
      </td>

    </tr>
    <tr>
      <td>
        <code id="seed">column separator</code>
      </td>
      <td>
        <code><a href="../parameters.html#string">String</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        Character separating fields in a row.
        Default value: <code>,</code>.
      </td>
    </tr>

    <tr>
      <td>
        <code id="seed">write header</code>
      </td>
      <td>
        <code><a href="../parameters.html#boolean">Boolean</a></code>
      </td>
      <td>Valid only if <code>format = CSV</code>.
        If <code>true</code> then columns' names will be written at the
        beginning of the output file.
      </td>
    </tr>

    <tr>
      <td>
        <code id="seed">output file</code>
      </td>
      <td>
        <code><a href="../parameters.html#string">String</a></code>
      </td>
      <td>A path where the output file will be saved.</td>
    </tr>
  </tbody>
</table>
