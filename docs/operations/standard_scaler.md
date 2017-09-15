---
layout: documentation
displayTitle: Standard Scaler
title: Standard Scaler
description: Standard Scaler
usesMathJax: true
includeOperationsMenu: true
---
Standardizes features by removing the mean and scaling to unit variance using column summary statistics on the samples in the training set.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.StandardScaler">org.apache.spark.ml.feature.StandardScaler documentation</a>.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The input <code>DataFrame</code>.</td></tr>
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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The output <code>DataFrame</code>.</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>A <code>Transformer</code> that allows to apply the operation on other<code>DataFrames</code> using a <a href="transform.html">Transform</a>.</td></tr>
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
<td><code>with mean</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Centers the data with mean before scaling.</td>
</tr>

<tr>
<td><code>with std</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Scales the data to unit standard deviation.</td>
</tr>

<tr>
<td><code>input column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The input column name.</td>
</tr>

<tr>
<td><code>output column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The output column name.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/StandardScaler.md %}
