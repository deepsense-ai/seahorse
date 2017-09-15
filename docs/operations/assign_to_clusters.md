---
layout: documentation
displayTitle: Assign To Clusters
title: Assign To Clusters
description: Assign To Clusters
includeOperationsMenu: true
---

Assigns data to clusters on a [DataFrame](../classes/dataframe.html)
using a previously trained [Clustering](../traits/clustering.html) Model.

It assumes that names of all columns that were used to train the model
can be found in input DataFrame, and that corresponding columns
have the same types.
If not, <code>ColumnsDoNotExistException</code> or
<code>WrongColumnTypeException</code> are thrown respectively.

The result column will be appended to the scored DataFrame.

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
          <a href="../traits/scorable.html">Scorable</a>]
        </code>
      </td>
      <td>A trained clustering model to use on the dataset</td>
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
      <td>A DataFrame to score</td>
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
          <a href="../classes/dataframe.html">DataFrame</a>
        </code>
      </td>
      <td>A DataFrame with predictions column appended</td>
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
      <td><code>prediction column</code></td>
      <td><code><a href="../parameters.html#string">String</a></code></td>
      <td>A name of the newly created column, which contains generated assignment to clusters</td>
    </tr>
  </tbody>
</table>
