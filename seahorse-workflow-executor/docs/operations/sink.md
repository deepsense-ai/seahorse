---
layout: global
displayTitle: Sink
title: Sink
description: Sink
includeOperationsMenu: true
---

The `Sink` operation is a special node that represents the output of user-defined
[Transformer](../classes/transformer.html) created by
[Create Custom Transformer](create_custom_transformer.html).

This operation is automatically added to every inner workflow of `Create Custom Transformer`,
as a part of its creation process.

**Remark**: Only one instance of that operation is allowed in every inner workflow,
thus there is no possibility of manual placing, copying or removing this operation.

**Since**: Seahorse 1.0.0


## Input

<table>
  <thead>
    <tr>
      <th style="width:15%">Port</th>
      <th style="width:15%">Type Qualifier</th>
      <th style="width:70%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>0</code></td>
      <td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
      <td>The input <code>DataFrame</code> that will be passed as result of <code>CustomTransformer</code>.</td>
    </tr>
  </tbody>
</table>

## Output

The `Sink` operation does not produce any output.

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
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>