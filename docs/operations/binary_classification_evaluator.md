---
layout: documentation
displayTitle: Binary Classification Evaluator
title: Binary Classification Evaluator
description: Binary Classification Evaluator
usesMathJax: true
includeOperationsMenu: true
---
Creates a binary classification evaluator.


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

</tbody>
</table>

