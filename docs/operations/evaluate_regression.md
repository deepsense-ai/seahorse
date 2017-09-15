---
layout: documentation
displayTitle: Evaluate Regression
title: Evaluate Regression
description: Evaluate Regression
usesMathJax: true
includeOperationsMenu: true
---


Provides a [report](../classes/report.html)
containing an evaluation of predictions quality in the given DataFrame.
The report contains the following metrics:
<a target="_blank" href="https://en.wikipedia.org/wiki/Explained_variation">Explained Variance</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Mean_absolute_error">Mean Absolute Error</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Mean_squared_error">Mean Square Error</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Coefficient_of_determination">r2</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Root-mean-square_deviation">Root Mean Squared Error</a>

**Since**: Seahorse 0.4.0

## Input

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>
DataFrame containing predictions which are to be evaluated.
</td>
</tr>
</tbody>
</table>

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
<tr>
<td><code>0</code></td>
<td><code>
  <a href="../classes/report.html">Report</a></code></td>
<td>Evaluation of the regression's predictions</td>
</tr>
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
<td><code id="target_column">target column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>
Column with expected values. Has to be of <code>Numeric</code>type.
</td>
</tr>
<tr>
<td><code id="prediction_column">prediction column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>
Column with prediction. Has to be of <code>Numeric</code>type.
</td>
</tr>
</tbody>
</table>
