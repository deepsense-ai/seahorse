---
layout: documentation
displayTitle: HashingTF
title: HashingTF
description: HashingTF
usesMathJax: true
includeOperationsMenu: true
---
Maps a sequence of terms to their term frequencies using the hashing trick.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.HashingTF">org.apache.spark.ml.feature.HashingTF documentation</a>.

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
<td><code>numFeatures</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Number of features. Should be > 0.</td>
</tr>

<tr>
<td><code>one or many</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Transform one or many columns. Possible values: <code>["Transform one column", "Transform multiple columns"]</code></td>
</tr>

</tbody>
</table>

