---
layout: documentation
displayTitle: Evaluate Classification
title: Evaluate Classification
description: Evaluate Classification
includeOperationsMenu: true
---

Provides a [report](../classes/report.html)
containing an evaluation of classification quality in the given DataFrame.

The report contains the following metrics:
[Area under ROC](https://en.wikipedia.org/wiki/Receiver_operating_characteristic#Area_under_the_curve),
[Log Loss](https://en.wikipedia.org/wiki/Cross_entropy#Cross-entropy_error_function_and_logistic_regression),
[Accuracy](https://en.wikipedia.org/wiki/Accuracy_and_precision#In_binary_classification) by threshold,
[F-Measure (F1 score)](https://en.wikipedia.org/wiki/F1_score) by threshold,
[ROC curve](https://en.wikipedia.org/wiki/Receiver_operating_characteristic).

**Since**: Seahorse 0.4.0

## Input

<table>
  <thead>
    <tr>
      <th style="width:20%">Port</th>
      <th style="width:25%">Type Qualifier</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>0</code>
      </td>
      <td>
        <code>
          <a href="../classes/dataframe.html">DataFrame</a>
        </code>
      </td>
      <td>DataFrame containing predictions which are to be evaluated</td>
    </tr>
  </tbody>
</table>

## Output

<table>
  <thead>
    <tr>
      <th style="width:20%">Port</th>
      <th style="width:25%">Type Qualifier</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>0</code>
      </td>
      <td>
        <code>
          <a href="../classes/report.html">Report</a>
        </code>
      </td>
      <td>Evaluation of the classifier's predictions</td>
    </tr>
  </tbody>
</table>

## Parameters

<table class="table">
  <thead>
    <tr>
      <th style="width:20%">Name</th>
      <th style="width:25%">Type</th>
      <th style="width:55%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>target column</code></td>
      <td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
      <td>Column with expected values. Has to be of <code>Numeric</code>type</td>
    </tr>
    <tr>
      <td><code>prediction column</code></td>
      <td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
      <td>Column with classification prediction. Has to be of <code>Numeric</code>type</td>
    </tr>
  </tbody>
</table>
