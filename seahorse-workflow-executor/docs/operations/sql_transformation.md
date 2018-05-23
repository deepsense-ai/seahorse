---
layout: global
displayTitle: SQL Transformation
title: SQL Transformation
description: SQL Transformation
usesMathJax: true
includeOperationsMenu: true
---

Executes a
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#sql">Spark SQL</a>
(enriched with some [User Defined Functions](../spark_sql_udf.html))
expression provided by the user on a [DataFrame](../classes/dataframe.html) connected to its input port.
Returns the results of the execution as a `DataFrame`.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

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
<td>The <code>DataFrame</code> that the <code>Spark SQL</code> expression will be executed on.</td>
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
<td>The results of the <code>Spark SQL</code> expression.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>A <code>Transformer</code> that allows to apply the operation to another <code>DataFrames</code>
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
<td><code>dataframe id</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The identifier that can be used in the <code>Spark SQL</code> expression to refer
the input <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>expression</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>The <code>Spark SQL</code> expression to be executed.
The expression must be a valid <code>Spark SQL</code> expression.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/SqlTransformation.md %}
