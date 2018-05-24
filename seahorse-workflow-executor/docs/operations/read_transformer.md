---
layout: global
displayTitle: Read Transformer
title: Read Transformer
description: Read Transformer
includeOperationsMenu: true
---

Reads a [Transformer](../classes/transformer.html) from a directory at the specified location.

It supports reading `Transformers` that were written using the [Write Transformer](write_transformer.html) operation.

A `Transformer` can be read from HDFS.
Additionally, in local cluster mode a `Transformer` can be read from local filesystem.

**Since**: Seahorse 1.1.0

## Input

The `Read Transformer` operation does not take any input.

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
          <a href="../classes/transformer.html">Transformer</a>
        </code>
      </td>
      <td>The <code>Transformer</code> read from the source.</td>
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
    <code>source</code>
  </td>
  <td>
    <code><a href="../parameter_types.html#string">String</a></code>
  </td>
  <td>A path to the input directory.
  </td>
</tr>
    

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>
    
</tbody>
</table>
