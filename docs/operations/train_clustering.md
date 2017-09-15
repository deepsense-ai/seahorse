---
layout: documentation
displayTitle: Train Clustering
title: Train Clustering
description: Train Clustering
includeOperationsMenu: true
---

Trains a [Clustering](../traits/clustering.html) on a
[DataFrame](../classes/dataframe.html),
returning a trained model than can be [scored](../traits/scorable.html) and
a [DataFrame](../classes/dataFrame.html) with assignment to clusters.

**Since**: Seahorse 0.5.0

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
          [<a href="../traits/clustering.html">Clustering</a> +
          <a href="../traits/unsupervised_trainable.html">UnsupervisedTrainable</a>]
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
        <code>
          [<a href="../traits/clustering.html">Clustering</a> +
          <a href="../traits/scorable.html">Scorable</a>]
        </code>
      </td>
      <td>A trained model</td>
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
      <td>A DataFrame with assignment to clusters</td>
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
      <td><code>prediction column</code></td>
      <td><code><a href="../parameters.html#string">String</a></code></td>
      <td>
        A name of the newly created column, which contains generated assignment to clusters in
        the output DataFrame.
      </td>
    </tr>
  </tbody>
</table>
