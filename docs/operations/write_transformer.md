---
layout: global
displayTitle: Write Transformer
title: Write Transformer
description: Write Transformer
usesMathJax: true
includeOperationsMenu: true
---

A `Write Transformer` operation saves a [Transformer](../classes/transformer.html) to a directory at the specified location.

It can be subsequently read using the [Read Transformer](read_transformer.html) operation.

A `Transformer` can be written to HDFS.
Additionally, in local cluster mode `Transformer` can be written to local filesystem.

**NOTE:** Only crucial attributes of the `Transformer` are written.
Attributes that are not used during `Transformer` execution might be lost after `Transformer` write&read flow.

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
        <code>output path</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#string">String</a></code>
      </td>
      <td>The path where the output directory will be saved.
      </td>
    </tr>
    <tr>
      <td>
        <code>overwrite</code>
      </td>
      <td>
        <code><a href="../parameter_types.html#boolean">Boolean</a></code>
      </td>
      <td>Whether saving the transformer should overwrite an existing one with the same name.
      </td>
    </tr>
  </tbody>
</table>
