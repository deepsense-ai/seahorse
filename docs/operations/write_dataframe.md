---
layout: global
displayTitle: Write DataFrame
title: Write DataFrame
description: Write DataFrame
usesMathJax: true
includeOperationsMenu: true
---

Saves a `DataFrame` to a specified [data source](../reference/data_sources.html).

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
        <code>data source</code>
      </td>
      <td>
        <code>Visual Selector</code>
      </td>
      <td>The definition that Seahorse uses to write the data.</td>
    </tr>
    <tr>
      <td>
        <code>overwrite</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>
        Value determining whether saving a file or a table should overwrite an existing one with the same name.
        Valid only for files and relational databases.
      </td>
    </tr>
  </tbody>
</table>
