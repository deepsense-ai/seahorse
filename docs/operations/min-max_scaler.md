---
layout: documentation
displayTitle: Min-Max Scaler
title: Min-Max Scaler
description: Min-Max Scaler
usesMathJax: true
includeOperationsMenu: true
---
Rescales each feature individually to a common range [min, max] linearly using column summary statistics, which is also known as min-max normalization or rescaling.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.MinMaxScaler">org.apache.spark.ml.feature.MinMaxScaler documentation</a>.

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
<td><code>min</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Lower bound after transformation, shared by all features.</td>
</tr>
    
<tr>
<td><code>max</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Upper bound after transformation, shared by all features.</td>
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
    
