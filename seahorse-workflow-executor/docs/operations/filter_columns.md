---
layout: global
displayTitle: Filter Columns
title: Filter Columns
description: Filter Columns
usesMathJax: true
includeOperationsMenu: true
---

Creates a new `DataFrame` that contains only the selected columns. The order of the columns is preserved.
Each column can be selected only once. Selecting a column more than once will not duplicate the
column in the output. Thus, in the resulting `DataFrame` no column will be duplicated.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) with a [Transform](transform.html) operation.

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
<td><code>columns</code></td>
<td><code><a href="../parameter_types.html#multiple-column-selector">MultipleColumnSelector</a></code></td>
<td>The columns to be included in the output <code>DataFrame</code>.
Even if one of the columns is selected more than once (e.g. by name and by type)
it will be included only once. An empty selection is supported, but when a column selected by name
or by index does not exist, the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.
</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/FilterColumns.md %}
