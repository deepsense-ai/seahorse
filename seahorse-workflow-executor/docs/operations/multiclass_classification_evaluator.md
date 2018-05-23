---
layout: global
displayTitle: Multiclass Classification Evaluator
title: Multiclass Classification Evaluator
description: Multiclass Classification Evaluator
usesMathJax: true
includeOperationsMenu: true
---
Creates a multiclass classification evaluator. Multiclass classification evaluator does not assume any label class is special, thus it cannot be used for calculation of metrics specific for binary classification (where this assumption is taken into account).

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/mllib-evaluation-metrics.html#multiclass-classification">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator">org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator documentation</a>.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/evaluator.html">Evaluator</a></code></td><td>An <code>Evaluator</code> that can be used in an <a href="evaluate.html">Evaluate</a> operation.</td></tr>
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
<td><code>multiclass metric</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>The metric used in evaluation. Possible values: <code>["f1", "precision", "recall", "weightedPrecision", "weightedRecall"]</code></td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The prediction column.</td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The label column for model fitting.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

