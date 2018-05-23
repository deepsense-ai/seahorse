---
layout: global
displayTitle: Naive Bayes
title: Naive Bayes
description: Naive Bayes
usesMathJax: true
includeOperationsMenu: true
---
Creates a naive Bayes model.
It supports Multinomial NB which can handle finitely supported discrete data.
For example, by converting documents into TF-IDF vectors,
it can be used for document classification.
By making every vector a binary (0/1) data, it can also be used as Bernoulli NB.
The input feature values must be nonnegative.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/mllib-naive-bayes.html">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.classification.NaiveBayes">org.apache.spark.ml.classification.NaiveBayes documentation</a>.

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
<td><code>smoothing</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The smoothing parameter.</td>
</tr>

<tr>
<td><code>modelType</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>The model type. Possible values: <code>["multinomial", "bernoulli"]</code></td>
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
<td><code>probability column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The column for predicted class conditional probabilities.</td>
</tr>

<tr>
<td><code>raw prediction column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The raw prediction (confidence) column.</td>
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

