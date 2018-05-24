---
layout: global
displayTitle: Sort
title: Sort
description: Sort
usesMathJax: true
includeOperationsMenu: true
---

Sorts input [DataFrame](../classes/dataframe.html) according to the given columns.

User selects columns by name or index. He may also specify the ascending/descending order for each column.

The sort is performed as in the SQL's ORDER BY clause - the first selected column is given the highest priority, subsequent columns are taken into account only when the comparison on the earlier ones is not enough to determine the rows' ordering.

**Since**: Seahorse 1.4.0

## Input

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Input <code>DataFrame</code>.</td>
</tr>

</tbody>
</table>

## Output

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td><code>DataFrame</code> sorted by the given columns.</td>
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
<td><code>sort columns</code></td><td><code><a href="../parameter_types.html#parameters-sequence">Parameters Sequence</a></code></td><td>Sequence of <code><a href="../parameter_types.html#single-column-selector">single column selector</a></code>s each augmented with <code><a href="../parameter_types.html#boolean">boolean</a></code> <code>descending</code> flag.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/SortTransformation.md %}
