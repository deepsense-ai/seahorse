---
layout: documentation
displayTitle: Evaluate
title: Evaluate
description: Evaluate
usesMathJax: true
includeOperationsMenu: true
---

Evaluate is an operation used to calculate a metric value from given
[DataFrame](../classes/dataframe.html).

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
<td>Input DataFrame.</td>
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
<td><code><a href="../classes/metric_value.html">MetricValue</a></code></td>
<td>Metric value calculated for input DataFrame.</td>
</tr>
</tbody>
</table>
