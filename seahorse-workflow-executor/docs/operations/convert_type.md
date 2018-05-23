---
layout: global
displayTitle: Convert Type
title: Convert Type
description: Convert Type
usesMathJax: true
includeOperationsMenu: true
---

Converts types of columns in a [DataFrame](../classes/dataframe.html). One or more columns
can be converted at the same time. Every selected column is converted to the same (selected) type.

Supports conversions from all types to String, Boolean, Timestamp, Double, Float, Long,
and Integer type.

Every `null` value stays a `null` value in the result `DataFrame` (despite the column type change).
When a Timestamp column is converted to Numeric, then each value is represented
by the number of milliseconds since 1 January 1970.

Boolean converted to String generates a column of `true` and `false` strings.

String column can be converted to Numeric only if all values in the column represent a numeric value.

A column converted to its type is not modified.
If one or more column can not be converted,
the operation will fail at runtime with a `TypeConversionException`.

This operation also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) with [Transform](transform.html) operation.

**Since**: Seahorse 0.4.0

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
<td>The <code>DataFrame</code> to select columns from.</td>
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
<td>The <code>DataFrame</code> with the converted columns.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>A <code>Transformer</code> that allows to apply the operation to other <code>DataFrames</code>
using a <a href="transform.html">Transform</a>.</td>
</tr>
</tbody>
</table>

## Parameters

<table class="table">
<thead>
<tr>
<th style="width:15%">Name</th>
<th style="width:15%">Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>target type</code></td>
<td><code><a href="../parameter_types.html#single-choice">Single Choice</a></code></td>
<td>Target type of the conversion. Possible values are: <code>[String, Boolean, Timestamp, Double, Float, Long,
Integer]</code>.</td>
</tr>

<tr>
<td><code>operate on</code></td>
<td><code><a href="../parameter_types.html#input-output-column-selector">InputOutputColumnSelector</a></code></td>
<td>Input and output columns for the operation.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/ConvertType.md %}
