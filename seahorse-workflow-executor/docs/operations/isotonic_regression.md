---
layout: global
displayTitle: Isotonic Regression
title: Isotonic Regression
description: Isotonic Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates an isotonic regression model.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/mllib-isotonic-regression.html">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.regression.IsotonicRegression">org.apache.spark.ml.regression.IsotonicRegression documentation</a>.

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
<td><code>isotonic</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Whether the output sequence should be isotonic/increasing (true)
or antitonic/decreasing (false).</td>
</tr>

<tr>
<td><code>use custom weights</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Whether to over-/under-sample training instances according to the given weights in
the `weight column`. If the `weight column` is not specified,
all instances are treated equally with a weight 1.0. Possible values: <code>["no", "yes"]</code></td>
</tr>

<tr>
<td><code>feature index</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The index of the feature if features column is a vector column, no effect otherwise.</td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The label column for model fitting.</td>
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

