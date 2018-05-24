---
layout: global
displayTitle: Projection
title: Projection
description: Projection
usesMathJax: true
includeOperationsMenu: true
---

Creates a new [DataFrame](../classes/dataframe.html) that contains only the selected columns.
The order of the columns is specified by user.
Each column can be optionally renamed.
Each column can be selected many times,
but in the resulting `DataFrame` column names cannot be duplicated.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

**Since**: Seahorse 1.2.0

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
<td>The <code>DataFrame</code> containing the selected columns (and only them).</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code>
using the <a href="transform.html">Transform</a>.</td>
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
<td><code>projection columns</code></td>
<td><code><a href="../parameter_types.html#parameters-sequence">Parameters Sequence</a></code></td>
<td>The sequence of column projection descriptions
   (<code>original column: <a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code>,
   <code>rename column: <a href="../parameter_types.html#single-choice">Single Choice</a></code> - possible values: <code>["No", "Yes"]</code>,
   <code>column name: <a href="../parameter_types.html#string">String</a></code>
    - valid only if <code>rename column</code> is set to <code>"Yes"</code>)
   defining the selection, order and optionally new names of columns.
   When a column selected by name or by index does not exist, <code>ColumnDoesNotExistException</code> is thrown.
</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/Projection.md %}
