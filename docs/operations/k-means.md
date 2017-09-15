---
layout: documentation
displayTitle: K-Means
title: K-Means
description: K-Means
usesMathJax: true
includeOperationsMenu: true
---
Creates a k-means model.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.clustering.KMeans">org.apache.spark.ml.clustering.KMeans documentation</a>.

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
<td><code>features column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Features column for model fitting.</td>
</tr>

<tr>
<td><code>k</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of clusters to create. Must be > 1.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of iterations (>= 0).</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Random seed.</td>
</tr>

<tr>
<td><code>tolerance</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>The convergence tolerance for iterative algorithms.</td>
</tr>

<tr>
<td><code>init mode</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Param for the initialization algorithm. This can be either "random" to choose random points as initial cluster centers, or "k-means||" to use a parallel variant of k-means++. Possible values: <code>["random", "k-means||"]</code></td>
</tr>

<tr>
<td><code>init steps</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of steps for the k-means|| initialization mode. It will be ignored when other initialization modes are chosen.</td>
</tr>

</tbody>
</table>

