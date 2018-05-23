---
layout: global
displayTitle: Chi-Squared Selector
title: Chi-Squared Selector
description: Chi-Squared Selector
usesMathJax: true
includeOperationsMenu: true
---
Selects categorical features to use for predicting a categorical label.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-features.html#chisqselector">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.feature.ChiSqSelector">org.apache.spark.ml.feature.ChiSqSelector documentation</a>.

**Since**: Seahorse 1.1.0

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The input <code>DataFrame</code>.</td></tr>
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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>The output <code>DataFrame</code>.</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>A <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code> using a <a href="transform.html">Transform</a>.</td></tr>
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
<td><code>num top features</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Number of features that selector will select, ordered by statistics value descending. If the real number of features is lower, then this will select all features.</td>
</tr>

<tr>
<td><code>features column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The features column for model fitting.</td>
</tr>

<tr>
<td><code>output column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The output column name.</td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The label column for model fitting.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>


{% markdown operations/examples/ChiSqSelector.md %}
