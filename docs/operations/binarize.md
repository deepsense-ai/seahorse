---
layout: documentation
displayTitle: Binarize
title: Binarize
description: Binarize
usesMathJax: true
includeOperationsMenu: true
---
Binarizes continuous features.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.Binarizer">org.apache.spark.ml.feature.Binarizer documentation</a>.

**Since**: Seahorse 1.0.0

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Input DataFrame</td></tr>
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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Output DataFrame</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>Transformer that allows to apply the operation on other DataFrames using <a href="transform.html">Transform</a></td></tr>
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
<td><code>threshold</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Threshold used to binarize continuous features. Feature values greater than the threshold will be binarized to 1.0. Remaining values will be binarized to 0.0.</td>
</tr>

<tr>
<td><code>operate on</code></td>
<td><code><a href="../parameters.html#input_output_column_selector">InputOutputColumnSelector</a></code></td>
<td>Input and output columns for the operation.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/Binarize.md %}
