---
layout: global
displayTitle: Min-Max Scaler
title: Min-Max Scaler
description: Min-Max Scaler
usesMathJax: true
includeOperationsMenu: true
---
Linearly rescales each feature to a common range [min, max] using column summary statistics.
The operation is also known as Min-Max normalization or rescaling.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-features.html#minmaxscaler">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.feature.MinMaxScaler">org.apache.spark.ml.feature.MinMaxScaler documentation</a>.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The output <code>DataFrame</code>.</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>A <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code> using a <a href="transform.html">Transform</a>.</td></tr>
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
<td><code>input column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The input column name.</td>
</tr>

<tr>
<td><code>output</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Output generation mode. Possible values: <code>["replace input column", "append new column"]</code></td>
</tr>

<tr>
<td><code>min</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The lower bound after transformation, shared by all features.</td>
</tr>

<tr>
<td><code>max</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The upper bound after transformation, shared by all features.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/MinMaxScaler.md %}
