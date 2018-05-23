---
layout: global
displayTitle: Linear Regression
title: Linear Regression
description: Linear Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates a linear regression model.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-classification-regression.html#linear-regression">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.regression.LinearRegression">org.apache.spark.ml.regression.LinearRegression documentation</a>.

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
<td><code>elastic net param</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The ElasticNet mixing parameter. For alpha = 0, the penalty is an L2 penalty. For alpha = 1, it is an L1 penalty.</td>
</tr>

<tr>
<td><code>fit intercept</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Whether to fit an intercept term.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of iterations.</td>
</tr>

<tr>
<td><code>regularization param</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The regularization parameter.</td>
</tr>

<tr>
<td><code>tolerance</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The convergence tolerance for iterative algorithms.</td>
</tr>

<tr>
<td><code>standardization</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>Whether to standardize the training features before fitting the model.</td>
</tr>

<tr>
<td><code>use custom weights</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Whether to over-/under-sample training instances according to the given weights in
the `weight column`. If the `weight column` is not specified,
all instances are treated equally with a weight 1.0. Possible values: <code>["no", "yes"]</code></td>
</tr>

<tr>
<td><code>solver</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Sets the solver algorithm used for optimization.
Can be set to "l-bfgs", "normal" or "auto".
"l-bfgs" denotes Limited-memory BFGS which is a limited-memory quasi-Newton
optimization method. "normal" denotes Normal Equation. It is an analytical
solution to the linear regression problem.
The default value is "auto" which means that the solver algorithm is
selected automatically. Possible values: <code>["auto", "normal", "l-bfgs"]</code></td>
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

