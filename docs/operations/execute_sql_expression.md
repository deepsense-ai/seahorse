---
layout: documentation
displayTitle: SQL Expression
title: SQL Expression
description: SQL Expression
usesMathJax: true
includeOperationsMenu: true
---

Executes an Spark SQL expression provided by the user on a DataFrame connected to its input port.
Returns the results of the execution as a DataFrame.

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
<td>A DataFrame that the SQL expression will be executed on.</td>
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
<td>The results of the SQL expression.</td>
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
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>An identifier that can be used in the SQL expression to refer to the input DataFrame. The value has to be unique in the workflow.</td>
</tr>
<tr>
<td><code>expression</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>The SQL expression to be executed. The expression must be a valid Spark SQL expression.</td>
</tr>
</tbody>
</table>
