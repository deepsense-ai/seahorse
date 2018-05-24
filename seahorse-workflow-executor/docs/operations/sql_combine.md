---
layout: global
displayTitle: SQL Combine
title: SQL Combine
description: SQL Combine
usesMathJax: true
includeOperationsMenu: true
---

Combines two [DataFrames](../classes/dataframe.html) using
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#sql">Spark SQL</a>
expression provided by the user on a [DataFrames](../classes/dataframe.html) connected to its input ports.
Returns the results of the execution as a `DataFrame`.

**Since**: Seahorse 1.4.0

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
<td>The left-hand side <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The right-hand side <code>DataFrame</code>.</td>
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
<td>The <code>DataFrame</code> containing a result of executing the expression.</td>
</tr>
</tbody>
</table>

## Parameters


<table class="table">
<thead>
<tr>
<th style="width:20%">Name</th>
<th style="width:15%">Type</th>
<th style="width:65%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>Left dataframe id</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The identifier that can be used in the <code>Spark SQL</code> expression to refer
the left-hand side <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>Right dataframe id</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The identifier that can be used in the <code>Spark SQL</code> expression to refer
the right-hand side <code>DataFrame</code>.</td>
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

{% markdown operations/examples/SqlCombine.md %}
