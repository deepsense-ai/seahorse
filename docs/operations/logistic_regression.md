---
layout: documentation
displayTitle: Logistic Regression
title: Logistic Regression
description: Logistic Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates a logistic regression model.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.classification.LogisticRegression">org.apache.spark.ml.classification.LogisticRegression documentation</a>.

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
<td><code>elastic net param</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>The ElasticNet mixing parameter, in range [0, 1]. For alpha = 0, the penalty is an L2 penalty. For alpha = 1, it is an L1 penalty.</td>
</tr>

<tr>
<td><code>fit intercept</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether to fit an intercept term.</td>
</tr>

<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of iterations (>= 0).</td>
</tr>

<tr>
<td><code>regularization param</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Regularization parameter (>= 0).</td>
</tr>

<tr>
<td><code>tolerance</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>The convergence tolerance for iterative algorithms.</td>
</tr>

<tr>
<td><code>standardization</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether to standardize the training features before fitting the model.</td>
</tr>

<tr>
<td><code>features column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Features column for model fitting.</td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Label column for model fitting.</td>
</tr>

<tr>
<td><code>probability column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Column for predicted class conditional probabilities.</td>
</tr>

<tr>
<td><code>raw prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Raw prediction (confidence) column.</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>

<tr>
<td><code>threshold</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Threshold in binary classification prediction, in range [0, 1].</td>
</tr>

</tbody>
</table>

