---
layout: global
displayTitle: ALS
title: ALS
description: ALS
usesMathJax: true
includeOperationsMenu: true
---
Creates an ALS recommendation model.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/mllib-collaborative-filtering.html#collaborative-filtering">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.recommendation.ALS">org.apache.spark.ml.recommendation.ALS documentation</a>.

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
<td><code>alpha</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The alpha parameter in the implicit preference formulation.</td>
</tr>

<tr>
<td><code>checkpoint interval</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The checkpoint interval. E.g. 10 means that the cache will get checkpointed
every 10 iterations.</td>
</tr>

<tr>
<td><code>implicit prefs</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Whether to use implicit preference.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of iterations.</td>
</tr>

<tr>
<td><code>nonnegative</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Whether to apply nonnegativity constraints.</td>
</tr>

<tr>
<td><code>num item blocks</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of item blocks.</td>
</tr>

<tr>
<td><code>num user blocks</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The number of user blocks.</td>
</tr>

<tr>
<td><code>rank</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The rank of the matrix factorization.</td>
</tr>

<tr>
<td><code>rating column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The column for ratings.</td>
</tr>

<tr>
<td><code>regularization param</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The regularization parameter.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The random seed.</td>
</tr>

<tr>
<td><code>item column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The column for item ids.</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The prediction column created during model scoring.</td>
</tr>

<tr>
<td><code>user column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The column for user ids.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

