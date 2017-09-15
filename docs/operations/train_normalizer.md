---
layout: documentation
displayTitle: Train Normalizer
title: Train Normalizer
description: Train Normalizer
usesMathJax: true
includeOperationsMenu: true
---

Train Normalizer takes an input [DataFrame](../classes/dataframe.html) and
uses data from columns specified as the operation parameter to calculate mean and variance for these
columns. Train Normalizer creates a [Normalizer](../classes/normalizer.html)
which uses calculated mean and variance to normalize
[DataFrame](../classes/dataframe.html)
on which [Normalizer](../classes/normalizer.html) will be applied.
Under the hood Train Normalizer uses
<a target="_blank" href="http://spark.apache.org/docs/latest/api/java/org/apache/spark/ml/feature/StandardScaler.html#fit(org.apache.spark.sql.DataFrame)">StandardScaler.fit</a>
to produce
<a target="_blank" href="http://spark.apache.org/docs/latest/api/java/org/apache/spark/ml/feature/StandardScalerModel.html">StandardScalerModel</a>

**Since**: Seahorse 0.4.0

## Input

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>DataFrame that we want to train <code><a href="../classes/normalizer.html">Normalizer</a></code> on.</td>
</tr>
</tbody>
</table>

## Output

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Type Qualifier</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Input DataFrame normalized using produced <code><a href="../classes/normalizer.html">Normalizer</a></code></td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/normalizer.html">Normalizer</a></code></td>
<td>Normalizer trained on the input <code><a href="../classes/dataframe.html">DataFrame</a></code></td>
</tr>
</tbody>
</table>

## Parameters

<table class="table">
<thead>
<tr>
<th style="width:15%">Name</th>
<th style="width:15%">Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>selected columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Columns that the operation will use for training. Selected columns have to have Numeric type.</td>
</tr>
</tbody>
</table>
