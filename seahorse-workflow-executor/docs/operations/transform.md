---
layout: global
displayTitle: Transform
title: Transform
description: Transform
usesMathJax: true
includeOperationsMenu: true
---

The `Transform` operation is used to convert one [DataFrame](../classes/dataframe.html) into another
using a [Transformer](../classes/transformer.html). For example, a trained model produced by a
[Fit](../operations/fit.html) operation can be supplied to `Transform` with a
[DataFrame](../classes/dataframe.html) to be converted.

**Usage example**:
![Transform example](../img/transformer_example.png){: .img-responsive .spacer .centered-image }

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
<td><code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>Transformer</code> to be applied on the input <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The <code>DataFrame</code> to transform.</td>
</tr>
</tbody>
</table>

## Output

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
<td>The <code>DataFrame</code> which is a result of the <code>Transformer</code> applied on the input <code>DataFrame</code>.</td>
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
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>