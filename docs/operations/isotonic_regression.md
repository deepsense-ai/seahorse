---
layout: documentation
displayTitle: Isotonic Regression
title: Isotonic Regression
description: Isotonic Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates an isotonic regression model.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.regression.IsotonicRegression">org.apache.spark.ml.regression.IsotonicRegression documentation</a>.

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
<td><code>feature index</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Index of the feature if features column is a vector column, no effect otherwise.</td>
</tr>
    
<tr>
<td><code>features column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Features column for model fitting.</td>
</tr>
    
<tr>
<td><code>isotonic</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether the output sequence should be isotonic/increasing (true) or antitonic/decreasing (false).</td>
</tr>
    
<tr>
<td><code>label column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Label column for model fitting.</td>
</tr>
    
<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>
    
<tr>
<td><code>weight column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Weight column - if this is not set, we treat all instance weights as 1.0.</td>
</tr>
    
</tbody>
</table>
    
