---
layout: documentation
displayTitle: Create Gradient Boosted Trees Regression
title: Create Gradient Boosted Trees Regression
description: Create Gradient Boosted Trees Regression
usesMathJax: true
includeOperationsMenu: true
---

Creates an [UntrainedGradientBoostedTreesRegression](../classes/untrained_gradient_boosted_trees_regression.html) model.

The training algorithm iteratively trains decision trees in order to minimize a loss function.
On each iteration, the algorithm uses the current ensemble to predict the value of each training
instance and then compares the prediction with the true value. The DataFrame is re-labeled to put
more emphasis on training instances with poor predictions. Thus, in the next iteration, the decision
tree will help correct for previous mistakes.

The output model is eligible for [training](train_regressor.html).

**Since**: Seahorse 0.5.0

## Input

Create Gradient Boosted Trees Regression does not take any input.

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
        <code><a href="../classes/untrained_gradient_boosted_trees_regression.html">UntrainedGradientBoostedTreesRegression</a></code>
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
      <td><code>num iterations</code></td>
      <td><code>Numeric</code></td>
      <td>Number of iterations.</td>
    </tr>
    <tr>
      <td><code>loss</code></td>
      <td><code>Choice</code></td>
      <td>Loss function to use in iterations.
      Possible values: <code>["squared", "absolute"]</code>.</td>
    </tr>
    <tr>
      <td><code>impurity</code></td>
      <td><code>Choice</code></td>
      <td>Criterion used for information gain calculation.
      Possible values: <code>["variance"]</code>.</td>
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
