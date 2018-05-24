---
layout: global
displayTitle: Decision Tree Regression
title: Decision Tree Regression
description: Decision Tree Regression
usesMathJax: true
includeOperationsMenu: true
---
Creates a decision tree regression model.
It supports both continuous and categorical features.

This operation is ported from Spark ML.


For a comprehensive introduction, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/ml-classification-regression.html#decision-tree-regression">Spark documentation</a>.


For scala docs details, see
<a target="_blank" href="https://spark.apache.org/docs/2.0.0/api/scala/index.html#org.apache.spark.ml.regression.DecisionTreeRegressor">org.apache.spark.ml.regression.DecisionTreeRegressor documentation</a>.

**Since**: Seahorse 1.1.0

## Input

This operation does not take any input.

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
    <tr><td><code>0</code></td><td><code><a href="../classes/estimator.html">Estimator</a></code></td><td>An <code>Estimator</code> that can be used in a <a href="fit.html">Fit</a> operation.</td></tr>
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
<td><code>max depth</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum depth of the tree. E.g., depth 0 means 1 leaf node; depth 1 means 1 internal node + 2 leaf nodes.</td>
</tr>

<tr>
<td><code>max bins</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The maximum number of bins used for discretizing continuous features and for choosing how to split on features at each node. More bins give higher granularity. Must be >= 2 and >= number of categories in any categorical feature.</td>
</tr>

<tr>
<td><code>min instances per node</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The minimum number of instances each child must have after split. If a split causes the left or right child to have fewer instances than the parameter's value, the split will be discarded as invalid.</td>
</tr>

<tr>
<td><code>min information gain</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The minimum information gain for a split to be considered at a tree node.</td>
</tr>

<tr>
<td><code>max memory</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>Maximum memory in MB allocated to histogram aggregation.</td>
</tr>

<tr>
<td><code>cache node ids</code></td>
<td><code><a href="../parameter_types.html#boolean">Boolean</a></code></td>
<td>The caching nodes IDs. Can speed up training of deeper trees.</td>
</tr>

<tr>
<td><code>checkpoint interval</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The checkpoint interval. E.g. 10 means that the cache will get checkpointed
every 10 iterations.</td>
</tr>

<tr>
<td><code>seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>The random seed.</td>
</tr>

<tr>
<td><code>regression impurity</code></td>
<td><code><a href="../parameter_types.html#single-choice">SingleChoice</a></code></td>
<td>The criterion used for information gain calculation. Possible values: <code>["variance"]</code></td>
</tr>

<tr>
<td><code>label column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The label column for model fitting.</td>
</tr>

<tr>
<td><code>features column</code></td>
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>The features column for model fitting.</td>
</tr>

<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>The prediction column created during model scoring.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

