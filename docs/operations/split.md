---
layout: documentation
displayTitle: Split
title: Split
description: Split
usesMathJax: true
includeOperationsMenu: true
---

Splits a [DataFrame](../classes/dataframe.html) into two separate `DataFrames`. Each row from the
input `DataFrame` will always end up in one of the result `DataFrames`, but never in both.

The `Split` operation does not preserve row order.

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
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The <code>DataFrame</code> to split.</td>
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
<td>The first part of the input <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The second part of the input <code>DataFrame</code>.</td>
</tr>
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
<td><code id="ratio">ratio</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>
  A number between 0 and 1 describing how much of the input <code>DataFrame</code> will end up in
  the first part of the split. Example: <code>ratio = 0.3</code> means that the first output
  <code>DataFrame</code> will contain 30% of the rows of the input <code>DataFrame</code>, and the
  second output <code>DataFrame</code> will contain 70% of the rows of the input
  <code>DataFrame</code>.
</td>
</tr>
<tr>
<td><code id="seed">seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>
  An integer between -1073741824 and 1073741823 that is used as a seed for the random number
  generator. A fixed value of this parameter allows to produce repeatable results.
</td>
</tr>
</tbody>
</table>

{% markdown operations/examples/Split.md %}
