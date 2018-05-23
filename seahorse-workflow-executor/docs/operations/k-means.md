---
layout: global
displayTitle: K-Means
title: K-Means
description: K-Means
usesMathJax: true
includeOperationsMenu: true
---
Creates a k-means model. Note: Trained k-means model does not have any parameters.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-clustering.html#k-means">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.clustering.KMeans">org.apache.spark.ml.clustering.KMeans documentation</a>.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/estimator.html">Estimator</a></code></td><td>An <code>Estimator</code> that can be used in a <a href="fit.html">Fit</a> operation.</td></tr>
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
<td><code>k</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of clusters to create.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of iterations.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The random seed.</td>
</tr>

<tr>
<td><code>tolerance</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The convergence tolerance for iterative algorithms.</td>
</tr>

<tr>
<td><code>init mode</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>The initialization algorithm mode. This can be either "random" to choose random points as initial cluster centers, or "k-means||" to use a parallel variant of k-means++. Possible values: <code>["random", "k-means||"]</code></td>
</tr>

<tr>
<td><code>init steps</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of steps for the k-means|| initialization mode. It will be ignored when other initialization modes are chosen.</td>
</tr>

<tr>
<td><code>features column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The features column for model fitting.</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The prediction column created during model scoring.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

