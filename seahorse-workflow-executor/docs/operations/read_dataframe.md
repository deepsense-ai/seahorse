---
layout: global
displayTitle: Read DataFrame
title: Read DataFrame
description: Read DataFrame
includeOperationsMenu: true
---

Reads a `DataFrame` from a specified [data source](../reference/data_sources.html).

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
    <code>data source</code>
  </td>
  <td>
    <code>Visual Selector</code>
  </td>
  <td>The definition that Seahorse uses to read the data.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>
