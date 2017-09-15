---
layout: documentation
displayTitle: One Hot Encode
title: One Hot Encode
description: One Hot Encode
usesMathJax: true
includeOperationsMenu: true
---
One Hot Encode maps a column of category indices to a column of binary vectors.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.OneHotEncoder">org.apache.spark.ml.feature.OneHotEncoder documentation</a>.

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
<td><code>drop last</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>Whether to drop the last category in the encoded vector.</td>
</tr>

<tr>
<td><code>operate on</code></td>
<td><code><a href="../parameters.html#input_output_column_selector">InputOutputColumnSelector</a></code></td>
<td>Input and output columns for the operation.</td>
</tr>

</tbody>
</table>

