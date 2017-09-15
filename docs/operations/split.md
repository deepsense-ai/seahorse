---
layout: documentation
displayTitle: Split
title: Split
description: Split
usesMathJax: true
includeOperationsMenu: true
---

Splits [DataFrame](../classes/dataframe.html) $$ D $$ into two
separate DataFrames $$ A $$ and $$ B $$, such that $$ D=A \cup B $$ and $$ A \cap B = \emptyset $$.
This means that each row from the input DataFrame will always be in one of the result DataFrames,
but never in both.

Split operation does not preserve rows order.

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
<td>DataFrame to split</td>
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
<td>First part of the input DataFrame</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Second part of the input DataFrame</td>
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
  Number between 0 and 1 describing how much of the input DataFrame will end up in the first part
  of the split. Example: ratio = 0.3 means that the input DataFrame will be split in 30% and 70%
  proportions.
</td>
</tr>
<tr>
<td><code id="seed">seed</code></td>
<td><code><a href="../parameter_types.html#numeric">Numeric</a></code></td>
<td>
  Integer between -1073741824 and 1073741823 that is used as a seed for random number generator.
  Fixed value of this parameter allows to produce repeatable results.
</td>
</tr>
</tbody>
</table>

{% markdown operations/examples/Split.md %}
