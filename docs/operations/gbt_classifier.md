---
layout: documentation
displayTitle: GBT Classifier
title: GBT Classifier
description: GBT Classifier
usesMathJax: true
includeOperationsMenu: true
---
Gradient-Boosted Trees (GBTs) learning algorithm for classification. It supports binary labels, as well as both continuous and categorical features. Note: Multiclass labels are not currently supported.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.5.2/api/scala/index.html#org.apache.spark.ml.classification.GBTClassifier">org.apache.spark.ml.classification.GBTClassifier documentation</a>.

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
<td><code>features column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Features column for model fitting.</td>
</tr>
    
<tr>
<td><code>impurity</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Criterion used for information gain calculation. Possible values: <code>["entropy", "gini"]</code></td>
</tr>
    
<tr>
<td><code>label column</code></td>
<td><code><a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>Label column for model fitting.</td>
</tr>
    
<tr>
<td><code>loss function</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Loss function which GBT tries to minimize. Possible values: <code>["logistic"]</code></td>
</tr>
    
<tr>
<td><code>max bins</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of bins used for discretizing continuous features and for choosing how to split on features at each node. More bins give higher granularity. Must be >= 2 and >= number of categories in any categorical feature.</td>
</tr>
    
<tr>
<td><code>max depth</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum depth of the tree (>= 0). E.g., depth 0 means 1 leaf node; depth 1 means 1 internal node + 2 leaf nodes.</td>
</tr>
    
<tr>
<td><code>max iterations</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Maximum number of iterations (>= 0).</td>
</tr>
    
<tr>
<td><code>min information gain</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Minimum information gain for a split to be considered at a tree node.</td>
</tr>
    
<tr>
<td><code>min instances per node</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Minimum number of instances each child must have after split. If a split causes the left or right child to have fewer than minInstancesPerNode, the split will be discarded as invalid. Should be >= 1.</td>
</tr>
    
<tr>
<td><code>prediction column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Prediction column created during model scoring.</td>
</tr>
    
<tr>
<td><code>seed</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Random seed.</td>
</tr>
    
<tr>
<td><code>step size</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Step size to be used for each iteration of optimization.</td>
</tr>
    
<tr>
<td><code>subsampling rate</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Fraction of the training data used for learning each decision tree, in range (0, 1].</td>
</tr>
    
</tbody>
</table>
    
