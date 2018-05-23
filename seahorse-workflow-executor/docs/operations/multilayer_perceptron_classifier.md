---
layout: global
displayTitle: Multilayer Perceptron Classifier
title: Multilayer Perceptron Classifier
description: Multilayer Perceptron Classifier
usesMathJax: true
includeOperationsMenu: true
---
Creates a Multilayer Perceptron classification model.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-classification-regression.html#multilayer-perceptron-classifier">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.classification.MultilayerPerceptronClassifier">org.apache.spark.ml.classification.MultilayerPerceptronClassifier documentation</a>.

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
<td><code>layers</code></td>
<td><code><a href="../parameter_types.html#multiple-numeric">MultipleNumeric</a></code></td>
<td>The list of layer sizes that includes the input layer size as the first number and the
output layer size as the last number. The input layer and hidden layers have sigmoid
activation functions, while the output layer has a softmax. The input layer size has to be
equal to the length of the feature vector. The output layer size has to be equal to the
total number of labels.</td>
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

