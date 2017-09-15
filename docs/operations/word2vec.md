---
layout: documentation
displayTitle: Word2Vec
title: Word2Vec
description: Word2Vec
usesMathJax: true
includeOperationsMenu: true
---
Transforms a word into a code for further natural language processing or machine learning process.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.Word2Vec">org.apache.spark.ml.feature.Word2Vec documentation</a>

A comprehensive introduction is available at <a target="_blank" href="http://spark.apache.org/docs/1.6.0/mllib-feature-extraction.html#word2vec">MLlib Word2VVec Guide</a>.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Input DataFrame</td></tr>
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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Output DataFrame</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>Transformer that allows to apply the operation on other DataFrames using <a href="transform.html">Transform</a></td></tr>
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
<td>Input column name.</td>
</tr>

<tr>
<td><code>output column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>Output column name.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Maximum number of iterations.</td>
</tr>

<tr>
<td><code>step size</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Step size to be used for each iteration of optimization.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Random seed.</td>
</tr>

<tr>
<td><code>vector size</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The dimension of codes after transforming from words.</td>
</tr>

<tr>
<td><code>num partitions</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Number of partitions for sentences of words.</td>
</tr>

<tr>
<td><code>min count</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The minimum number of times a token must appear to be included in the model's vocabulary.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/Word2Vec.md %}
