---
layout: documentation
displayTitle: Create Support Vector Machine Classification
title: Create Support Vector Machine Classification
description: Create Support Vector Machine Classification
includeOperationsMenu: true
---

Creates an [UntrainedSupportVectorMachineClassifier](../classes/untrained_support_vector_machine_classification.html) model.

This operation creates a binary classifier that uses the
[Support Vector Machine](https://en.wikipedia.org/wiki/Support_vector_machine) algorithm.
This SVM implementation runs a `number of iterations` of
stochastic gradient descent algorithm using the provided `mini-batch fraction` of data in each step.

The output model is eligible for [training](train_classifier.html).

**Since**: Seahorse 0.5.0

## Input

Create Support Vector Machine Classification does not take any input.

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
        <code><a href="../classes/untrained_support_vector_machine_classification.html">UntrainedSupportVectorMachineClassifier</a></code>
      </td>
      <td>An untrained model</td>
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
      <td><code>number of iterations</code></td>
      <td><code>Numeric</code></td>
      <td>Number of iterations.</td>
    </tr>
    <tr>
      <td><code>regularization</code></td>
      <td><code>Choice</code></td>
      <td>Kind or regularization that should be used.
      Possible values: <code>["None", "L1", "L2"]</code>.</td>
    </tr>
    <tr>
      <td><code>regularization parameter</code></td>
      <td><code>Numeric</code></td>
      <td>Valid only if regularization is different from "None".
      Regularization parameter used in loss function.</td>
    </tr>
    <tr>
      <td><code>mini-batch fraction</code></td>
      <td><code>Numeric</code></td>
      <td>Fraction of data to be used for each SGD iteration.</td>
    </tr>
  </tbody>
</table>
