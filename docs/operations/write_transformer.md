---
layout: documentation
displayTitle: Write Transformer
title: Write Transformer
description: Write Transformer
usesMathJax: true
includeOperationsMenu: true
---

A `Write Transformer` operation saves a [Transformer](../classes/transformer.html) to a directory at the specified location.

It supports writing `Transformers` to the local file system and HDFS.

**Since**: Seahorse 1.1.0

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
        <a href="../classes/transformer.html">Transformer</a>
        </code>
      </td>
      <td>The <code>Transformer</code> to save.</td>
    </tr>
  </tbody>
</table>

## Output

The `Write Transformer` operation does not produce any output.

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
        <code id="output-file">output path</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>The path where the output directory will be saved.
      </td>
    </tr>
  </tbody>
</table>
