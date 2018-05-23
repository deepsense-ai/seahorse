---
layout: global
displayTitle: Word2Vec
title: Word2Vec
description: Word2Vec
usesMathJax: true
includeOperationsMenu: true
---
Transforms vectors of words into vectors of numeric codes for the purpose of further
processing by NLP or machine learning algorithms.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-features.html#word2vec">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.feature.Word2Vec">org.apache.spark.ml.feature.Word2Vec documentation</a>.

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
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of iterations.</td>
</tr>

<tr>
<td><code>step size</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The step size to be used for each iteration of optimization.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The random seed.</td>
</tr>

<tr>
<td><code>vector size</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The dimension of codes after transforming from words.</td>
</tr>

<tr>
<td><code>num partitions</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of partitions for sentences of words.</td>
</tr>

<tr>
<td><code>min count</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The minimum number of occurences of a token to be included in the model's vocabulary.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/Word2Vec.md %}
