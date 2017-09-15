---
layout: documentation
displayTitle: Create Logistic Regression
title: Create Logistic Regression
description: Create Logistic Regression
usesMathJax: true
includeOperationsMenu: true
---

Creates an [Untrained Logistic Regression](../classes/untrained_logistic_regression.html).

The output model eligible for
[training](train_classifier.html).

Regression is performed using <a target="_blank" href="https://en.wikipedia.org/wiki/Limited-memory_BFGS">LBFGS</a>
minimizing following loss function:

$$ log(1 + e^{-yw^Tx}) + \lambda\cdot\frac{1}{2}||w||^2_2 $$

where $$ x $$ is a vector of features, $$ y $$ is a label (-1 or 1),
$$ w $$ is the vector of weights of the model and $$ \lambda $$ is regularization parameter.


**Since**: Seahorse 0.4.0

## Input

Create Logistic Regression does not take any input.

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
        <a href="../classes/untrained_logistic_regression.html">
          Untrained Logistic Regression</a>
        </code>
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
      <td><code>regularization</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Regularization parameter used in loss function. Should be non-negative.</td>
    </tr>
    <tr>
      <td><code>iterations number</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Max number of iterations for the algorithm to perform</td>
    </tr>
    <tr>
      <td><code>tolerance</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>The convergence tolerance of iterations for LBFGS.
        Smaller value will lead to higher accuracy with the cost of more iterations.
      </td>
    </tr>
  </tbody>
</table>
