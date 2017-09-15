---
layout: documentation
displayTitle: Train Regressor
title: Train Regressor
description: Train Regressor
usesMathJax: true
includeOperationsMenu: true
---

Trains a [Regressor](../traits/regressor.html) on a
[DataFrame](../classes/dataframe.html),
returning a trained model that can be [scored](../operations/score_regressor.html).

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
          [<a href="../traits/regressor.html">Regressor</a> +
          <a href="../traits/trainable.html">Trainable</a>]
        </code>
      </td>
      <td>An untrained model</td>
    </tr>
    <tr>
      <td>
        <code>1</code>
      </td>
      <td>
        <code>
          <a href="../classes/dataframe.html">DataFrame</a>
        </code>
      </td>
      <td>A DataFrame on which the model is to be trained</td>
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
        <code>[<a href="../traits/classifier.html">Regressor</a> +
          <a href="../traits/scorable.html">Scorable</a>]
        </code>
      </td>
      <td>A trained model</td>
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
      <td><code>feature columns</code></td>
      <td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
      <td>A subset of the DataFrame columns, used to train the model.
        If columns selected by user have type different than <code>Numeric</code>,
        <code>WrongColumnTypeException</code> will be thrown.
        If some of the selected columns do not exist,
        <code>ColumnDoesNotExistException</code> will be thrown
      </td>
    </tr>
    <tr>
      <td><code>target column</code></td>
      <td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
      <td>A column containing a target of the training (original label).
        If selected column's type is different than <code>Numeric</code>,
        <code>WrongColumnTypeException</code> will be thrown.
        If selected column does not exist, <code>ColumnDoesNotExistException</code> will be thrown
      </td>
    </tr>
  </tbody>
</table>
