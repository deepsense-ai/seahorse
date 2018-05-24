---
layout: global
displayTitle: AFT Survival Regression
title: AFT Survival Regression
description: AFT Survival Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates an AFT survival regression model.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-classification-regression.html#survival-regression">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.regression.AFTSurvivalRegression">org.apache.spark.ml.regression.AFTSurvivalRegression documentation</a>.

**Since**: Seahorse 1.1.0

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
<td><code>tolerance</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The convergence tolerance for iterative algorithms.</td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The label column for model fitting.</td>
</tr>

<tr>
<td><code>censor column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>Param for censor column name.
The value of this column could be 0 or 1.
If the value is 1, it means the event has occurred i.e. uncensored;
otherwise censored.</td>
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
<td><code>quantile probabilities</code></td>
<td><code><a href="../parameter_types.html#multiple-numeric">MultipleNumeric</a></code></td>
<td>Param for quantile probabilities array.
Values of the quantile probabilities array should be in the range (0, 1)
and the array should be non-empty.</td>
</tr>

<tr>
<td><code>use custom quantiles</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>Param for quantiles column name.
This column will output quantiles of corresponding
quantileProbabilities if it is set. Possible values: <code>["no", "yes"]</code></td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

