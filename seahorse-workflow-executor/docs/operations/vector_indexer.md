---
layout: global
displayTitle: Vector Indexer
title: Vector Indexer
description: Vector Indexer
usesMathJax: true
includeOperationsMenu: true
---
Vector Indexer indexes categorical features inside of a Vector. It decides which features
are categorical and converts them to category indices. The decision is based on the number of
distinct values of a feature.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-features.html#vectorindexer">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.feature.VectorIndexer">org.apache.spark.ml.feature.VectorIndexer documentation</a>.

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
<td><code>max categories</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The threshold for the number of values a categorical feature can take.
If a feature is found to have more values, then it is declared continuous.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/VectorIndexer.md %}
