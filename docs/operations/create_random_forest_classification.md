---
layout: documentation
displayTitle: Create Random Forest Classification
title: Create Random Forest Classification
description: Create Random Forest Classification
usesMathJax: true
includeOperationsMenu: true
---

Creates an [UntrainedRandomForestClassification](../classes/untrained_random_forest_classification.html) model.

The training algorithm repeatedly selects a random sample with replacement
of the training set and fits trees to these samples. After training, predictions
can be made by averaging the predictions from individual regression trees.
At each candidate split in the learning process, a random subset of the features
is selected.

The output model eligible for [training](train_classifier.html)

**Since**: Seahorse 0.4.0

## Input

Create Random Forest Classification does not take any input.

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
        <code><a href="../classes/untrained_random_forest_classification.html">UntrainedRandomForestClassification</a></code>
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
      <td><code>num trees</code></td>
      <td><code>Numeric</code></td>
      <td>Number of trees in the random forest.</td>
    </tr>
    <tr>
      <td><code>feature subset strategy</code></td>
      <td><code>Choice</code></td>
      <td>Number of features to consider for splits at each node.
      Possible values: <code>["auto", "all", "sqrt", "log2", "onethird"]</code>.</td>
    </tr>
    <tr>
      <td><code>impurity</code></td>
      <td><code>Choice</code></td>
      <td>Criterion used for information gain calculation.
      Possible values: <code>["gini", "entropy"]</code>.</td>
    </tr>
    <tr>
      <td><code>max depth</code></td>
      <td><code>Numeric</code></td>
      <td>Maximum depth of the tree.</td>
    </tr>
    <tr>
      <td><code>max bins</code></td>
      <td><code>Numeric</code></td>
      <td>Maximum number of bins used for splitting features.</td>
    </tr>
  </tbody>
</table>
