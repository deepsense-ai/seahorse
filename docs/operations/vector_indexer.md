---
layout: documentation
displayTitle: Vector Indexer
title: Vector Indexer
description: Vector Indexer
usesMathJax: true
includeOperationsMenu: true
---
Indexes categorical feature columns in a dataset of Vector.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.feature.VectorIndexer">org.apache.spark.ml.feature.VectorIndexer documentation</a>.

**Since**: Seahorse 1.0.0

## Input

This operation does not take any input.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/estimator.html">Estimator</a></code></td><td>Estimator that can be used in <a href="fit.html">Fit</a> operation</td></tr>
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
<td><code>max categories</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Threshold for the number of values a categorical feature can take. If a feature is found to have > maxCategories values, then it is declared continuous. Must be >= 2.</td>
</tr>
    
<tr>
<td><code>input column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Input column name.</td>
</tr>
    
<tr>
<td><code>output column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Output column name.</td>
</tr>
    
</tbody>
</table>
    
