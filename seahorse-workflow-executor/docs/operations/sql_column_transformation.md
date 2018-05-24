---
layout: global
displayTitle: SQL Column Transformation
title: SQL Column Transformation
description: SQL Column Transformation
usesMathJax: true
includeOperationsMenu: true
---

Executes a
<a target="_blank" href="{{ site.SPARK_DOCS }}/sql-programming-guide.html#sql">Spark SQL</a>
(enriched with some [User Defined Functions](../spark_sql_udf.html))
formula (as used in `SELECT` statement) provided by the user on a column (columns)
of [DataFrame](../classes/dataframe.html) connected to its input port.
Returns modified `DataFrame`.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

**Since**: Seahorse 1.1.0

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
<td>The input <code>DataFrame</code>.</td>
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
<td><code>0</code></td><td>
<code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The results of the transformation.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>transformer</code> that allows to apply the operation on another <code>DataFrame</code> using
<code><a href="transform.html">Transform</a></code>.</td>
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
<td><code>input column alias</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The identifier that can be used in the <code>Spark SQL</code> formula
    (as used in <code>SELECT</code> statement) to refer the input column.</td>
</tr>

<tr>
<td><code>formula</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The <code>Spark SQL</code> formula (as used in <code>SELECT</code> statement).</td>
</tr>

<tr>
<td><code>operate on</code></td>
<td><code><a href="../parameter_types.html#input-output-column-selector">InputOutputColumnSelector</a></code></td>
<td>The input and output columns for the operation.</td>
</tr>
  

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/SqlColumnTransformation.md %}
