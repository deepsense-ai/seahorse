---
layout: documentation
displayTitle: Fit
title: Fit
description: Fit
usesMathJax: true
includeOperationsMenu: true
---

Fit is an operation used to fit an [Estimator](../classes/estimator.html) on a
[DataFrame](../classes/dataframe.html). It produces a [Transformer](../classes/transformer.html).

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
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>A DataFrame to fit the Estimator on.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/estimator.html">Estimator</a></code></td>
<td>An Estimator to fit.</td>
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
<td><code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>Result of fitting the Estimator on input DataFrame.</td>
</tr>
</tbody>
</table>
