---
layout: global
displayTitle: Filter Rows
title: Filter Rows
description: Filter Rows
usesMathJax: true
includeOperationsMenu: true
---

Creates a [DataFrame](../classes/dataframe.html) containing only rows satisfying given condition.
The condition should be
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#sql">Spark SQL</a>
(enriched with some [User Defined Functions](../spark_sql_udf.html))
condition (as used in `WHERE` condition).
The order of the columns is preserved.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) using a [Transform](transform.html) operation.

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
<td>A <code>DataFrame</code> to filter rows on.</td>
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
<td>The <code>DataFrame</code> containing only rows satisfying given condition.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>A <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code> using
a <a href="transform.html">Transform</a>.</td>
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
<td><code>condition</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>
The filtering condition. Rows not satisfying given condition will be excluded from output <code>DataFrame</code>.
It should be <code>Spark SQL</code> condition (as used in <code>WHERE</code> condition).</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/FilterRows.md %}
