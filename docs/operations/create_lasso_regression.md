---
layout: documentation
displayTitle: Create Lasso Regression
title: Create Lasso Regression
description: Create Lasso Regression
usesMathJax: true
includeOperationsMenu: true
---

Creates
[UntrainedLassoRegression](../classes/untrained_lasso_regression.html)
(i.e. uses L1 regularization). The output model is eligible for
[training](train_regressor.html).

Regression is performed using stochastic gradient descent
minimizing mean squared error:

$$ \frac{1}{2}(w^Tx-y)^2 + \lambda\cdot\frac{1}{2}||w||_1 $$

where $$ x $$
is vector of features, $$ y $$ is label, $$ w $$ is vector of weights of model
and $$ \lambda $$ is regularization parameter.

**Since**: Seahorse 0.5.0

## Input

Create Lasso Regression does not take any input.

## Output

<table>
  <thead>
    <tr>
      <th style="width:20%">Port</th>
      <th style="width:30%">Type Qualifier</th>
      <th style="width:50%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>
        <code>0</code>
      </td>
      <td>
        <code><a href="../classes/untrained_lasso_regression.html">Untrained Lasso Regression</a></code>
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
      <th style="width:30%">Type</th>
      <th style="width:50%">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>iterations number</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Max number of iterations for the algorithm to perform</td>
    </tr>
    <tr>
      <td><code>regularization</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Regularization parameter used in loss function. Should be non-negative.
      </td>
    </tr>
  </tbody>
</table>
