---
layout: documentation
displayTitle: Random Forest Regression
title: Random Forest Regression
description: Random Forest Regression
usesMathJax: true
includeOperationsMenu: true
---
Random forest regression (RFR), learning algorithm for regression. It supports both continuous and categorical features.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.regression.RandomForestRegressor">org.apache.spark.ml.regression.RandomForestRegressor documentation</a>.

**Since**: Seahorse 1.0.0

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
    <tr><td><code>0</code></td><td><code><a href="../classes/estimator.html">Estimator</a></code></td><td>Estimator that can be used in <a href="fit.html">Fit</a> operation</td></tr>
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
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum depth of each tree in the forest (>= 0).</td>
</tr>
    
<tr>
<td><code>max bins</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of bins discretizing continuous features (>= 2 and >= number of categories for any categorical feature).</td>
</tr>
    
<tr>
<td><code>min instances per node</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Minimum number of instances each child must have after split (>= 1).</td>
</tr>
    
<tr>
<td><code>min info gain</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Minimum information gain for a split to be considered at a tree node (>= 0).</td>
</tr>
    
<tr>
<td><code>max memory</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum memory in MB allocated to histogram aggregation (>= 0).</td>
</tr>
    
<tr>
<td><code>cache node ids</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Caching nodes IDs. Can speed up training of deeper trees.</td>
</tr>
    
<tr>
<td><code>checkpoint interval</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Specifies how often to checkpoint the cached node IDs in intervals (>= 1).</td>
</tr>
    
<tr>
<td><code>impurity</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Criterion used for information gain calculation. Possible values: <code>["variance"]</code></td>
</tr>
    
<tr>
<td><code>subsampling rate</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Fraction of the training data used for learning each decision tree, in range (0, 1].</td>
</tr>
    
<tr>
<td><code>seed</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Random seed.</td>
</tr>
    
<tr>
<td><code>num trees</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of trees to train (>= 1).</td>
</tr>
    
<tr>
<td><code>feature subset strategy</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>The number of features to consider for splits at each tree node. Possible values: <code>["auto", "onethird", "sqrt", "log2"]</code></td>
</tr>
    
<tr>
<td><code>features column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Features column for model fitting.</td>
</tr>
    
<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>
    
<tr>
<td><code>label column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Label column for model fitting.</td>
</tr>
    
</tbody>
</table>
    
