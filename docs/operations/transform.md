---
layout: documentation
displayTitle: Transform
title: Transform
description: Transform
usesMathJax: true
includeOperationsMenu: true
---

Transform is an operation used to transform a [DataFrame](../classes/dataframe.html)
using a [Transformer](../traits/transformer.html).

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
<tr>
<td><code>0</code></td>
<td><code><a href="../traits/transformer.html">Transformer</a></code></td>
<td>A Transformer to apply.</td>
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
<td>A DataFrame which is a result of the Transformer executed on the input DataFrame.</td>
</tr>
</tbody>
</table>
