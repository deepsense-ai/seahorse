---
layout: documentation
displayTitle: ALS
title: ALS
description: ALS
usesMathJax: true
includeOperationsMenu: true
---
Creates an ALS recommendation model.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.recommendation.ALS">org.apache.spark.ml.recommendation.ALS documentation</a>.

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
<td><code>alpha</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Param for the alpha parameter in the implicit preference formulation (>= 0).</td>
</tr>
    
<tr>
<td><code>checkpoint interval</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Checkpoint interval (>= 1).</td>
</tr>
    
<tr>
<td><code>implicit prefs</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether to use implicit preference.</td>
</tr>
    
<tr>
<td><code>item column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Column for item ids.</td>
</tr>
    
<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of iterations (>= 0).</td>
</tr>
    
<tr>
<td><code>nonnegative</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether to apply nonnegativity constraints.</td>
</tr>
    
<tr>
<td><code>num item blocks</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of item blocks (>= 1).</td>
</tr>
    
<tr>
<td><code>num user blocks</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of user blocks (>= 1).</td>
</tr>
    
<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>
    
<tr>
<td><code>rank</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Rank of the matrix factorization (>= 1).</td>
</tr>
    
<tr>
<td><code>rating column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Column for ratings.</td>
</tr>
    
<tr>
<td><code>regularization param</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Regularization parameter (>= 0).</td>
</tr>
    
<tr>
<td><code>seed</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Random seed.</td>
</tr>
    
<tr>
<td><code>user column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Column for user ids.</td>
</tr>
    
</tbody>
</table>
    
