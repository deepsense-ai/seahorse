---
layout: global
displayTitle: Union
title: Union
description: Union
usesMathJax: true
includeOperationsMenu: true
---

Sums two input [DataFrames](../classes/dataframe.html) together into a single `DataFrame`.
This means that each row from each of the input `DataFrames` will end up in the resulting `DataFrame`.

There will be no deduplication effort, meaning that if there are two identical rows in the input
`DataFrames`, both of them will be added to the output `DataFrame`.

The schemas of both input `DataFrames` have to be the same.

The order of rows from input `DataFrames` is not guaranteed to be preserved in the output `DataFrame`.

If schemas of the input `DataFrames` do not match, a ``SchemaMismatchException`` will be thrown.

**Since**: Seahorse 0.4.0

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
<td>The first <code>DataFrame</code>.</td>
</tr>

<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The second <code>DataFrame</code>.</td>
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
<td>Union of the input <code>DataFrames</code>.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/Union.md %}
