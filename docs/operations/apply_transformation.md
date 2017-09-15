---
layout: documentation
displayTitle: Apply Transformation
title: Apply Transformation
description: Apply Transformation
usesMathJax: true
includeOperationsMenu: true
---

Apply Transformation is a parameterless operation used to transform a [DataFrame](../classes/dataframe.html)
 using a [Transformation](../traits/transformation.html).

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
<td><code><a href="../classes/transformation.html">[Transformation]</a></code></td>
<td>A Transformation to apply.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>A DataFrame to transform.</td>
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
<td>A DataFrame which is a result of the transformation appliance on the input DataFrame.</td>
</tr>
</tbody>
</table>
