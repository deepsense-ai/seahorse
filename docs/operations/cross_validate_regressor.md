---
layout: documentation
displayTitle: Cross Validate Regressor
title: Cross Validate Regressor
description: Cross Validate Regressor
usesMathJax: true
includeOperationsMenu: true
---

Produces
[Scorable](../traits/scorable.html)
[Regressor](../traits/regressor.html)
trained on entire [DataFrame](../classes/dataframe.html)
and a cross-validation [report](../classes/report.html).
Report contains the following metrics for each fold:
<a target="_blank" href="https://en.wikipedia.org/wiki/Explained_variation">Explained Variance</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Mean_absolute_error">Mean Absolute Error</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Mean_squared_error">Mean Square Error</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Coefficient_of_determination">r2</a>,
<a target="_blank" href="https://en.wikipedia.org/wiki/Root-mean-square_deviation">Root Mean Squared Error</a>.

More details about cross-validation can be found
<a target="_blank" href="https://en.wikipedia.org/wiki/Cross-validation_%28statistics%29>here</a>.

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
      <td><code>0</code></td>
      <td><code>
      [<a href="../traits/regressor.html">Regressor</a> +
      <a href="../traits/trainable.html">Trainable</a>]</code></td>
      <td>Classifier to train and cross-validate</td>
    </tr>
    <tr>
      <td><code>1</code></td>
      <td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
      <td>DataFrame to train and cross-validate on</td>
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
      <td><code>0</code></td>
      <td><code>
      [<a href="../traits/regressor.html">Regressor</a> +
      <a href="../traits/scorable.html">Scorable</a>]</code></td>
      <td>Regressor trained on entire DateFrame</td>
    </tr>
    <tr>
      <td><code>1</code></td>
      <td>
        <code>
        <a href="../classes/report.html">Report</a>
        </code>
      </td>
      <td>
      Report of cross-validation with metrics for each fold.
      </td>
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
      <td><code>number of folds</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Number of folds (default: 10).
        If number of folds is greater than number of rows in training DataFrame,
        greatest applicable number of folds is used.
        Number of rows equal 0 results in omitting generation of cross-validation report
        (empty report is returned).
      </td>
    </tr>
    <tr>
      <td><code>shuffle</code></td>
      <td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
      <td>Indicates if shuffle on DataFrame should be performed before cross-validation.
        Possible values:
        <code>Yes</code>,
        <code>No</code>
      </td>
    </tr>
    <tr>
      <td><code>seed</code></td>
      <td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
      <td>Available only if <code>shuffle</code> is set to <code>Yes</code>. Seed value for shuffle (default: 0)</td>
    </tr>
    <tr>
      <td><code>feature columns</code></td>
      <td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
      <td>Subset of the DataFrame columns, used to train model.
        If columns selected by user have type different then <code>Numeric</code>,
        <code>WrongColumnTypeException</code> will be thrown.
        If some of selected columns do not exist,
        <code>ColumnDoesNotExistException</code> will be thrown.
      </td>
    </tr>
    <tr>
      <td><code>target column</code></td>
      <td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
      <td>Column containing target of training (i. e. label to predict).
        If selected column has type different then <code>Numeric</code>,
        <code>WrongColumnTypeException</code> will be thrown.
        If selected column does not exist, <code>ColumnDoesNotExistException</code> will be thrown.
      </td>
    </tr>
  </tbody>
</table>
