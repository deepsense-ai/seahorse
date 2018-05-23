---
layout: global
displayTitle: Binary Classification Evaluator
title: Binary Classification Evaluator
description: Binary Classification Evaluator
usesMathJax: true
includeOperationsMenu: true
---
Creates a binary classification evaluator.

## Available Metrics

### `Area under ROC`
Receiver operating characteristic (ROC), or ROC curve, is a graphical plot that illustrates
the performance of a binary classifier system as its discrimination threshold is varied.
For more details, view
<a target="_blank" href="https://en.wikipedia.org/wiki/Receiver_operating_characteristic">Wikipedia article</a>.

### `Area under PR`
Typically, Precision and Recall are inversely related,
but when a balance between these two metrics needs to be achieved,
the area under precision-recall curve could be used.
For more details, view
<a target="_blank" href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia article</a>.

### `Precision`
Precision (also called positive predictive value) is the fraction of retrieved instances that are relevant.
For more details, view
<a target="_blank" href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia article</a>.

### `Recall`
Recall (also known as sensitivity) is the fraction of relevant instances that are retrieved.
For more details, view
<a target="_blank" href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia article</a>.

### `F1 Score`
F1 score (also F-score or F-measure) is a measure of a test's accuracy.
It considers both precision and recall of the test to compute the score.
For more details, view
<a target="_blank" href="https://en.wikipedia.org/wiki/F1_score">Wikipedia article</a>.


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
<td><code>binary metric</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>The metric used in evaluation. Possible values: <code>["Area under ROC", "Area under PR", "Precision", "Recall", "F1 Score"]</code></td>
</tr>

<tr>
<td><code>raw prediction column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>Valid only if <code>binary metric = Area under ROC</code> or <code>binary metric = Area under PR</code>.
  The raw prediction (confidence) column.</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>Valid only if <code>binary metric = Precision</code>, <code>binary metric = Recall</code> or <code>binary metric = F1 Score</code>.
  The prediction column created during model scoring.</td>
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

