---
layout: documentation
displayTitle: Union
title: Union
description: Union
usesMathJax: true
includeOperationsMenu: true
---

Combines input [DataFrames](../classes/dataframe.html) $$ A $$ and $$ B $$
into a DataFrame $$ D $$ such that
$$ \forall_{x \in A \cup B} \; x \in D $$ and $$ |D| = |A| + |B| $$.

This means that each row from each of the input DataFrames will end up in the resulting DataFrame
with no deduplication effort.

The schemas of $$ A $$ and $$ B $$ have to be the same.

The order of rows in $$ A $$ and $$ B $$ is not guaranteed to be preserved in the $$ D $$.

**Since**: Seahorse 0.4.0

## Input

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>

<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Frist DataFrame</td>
</tr>

<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Second DataFrame</td>
</tr>

</tbody>
</table>

## Output

<table>
<thead>
<tr>
<th style="width:15%">Port</th>
<th style="width:15%">Data Type</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Union of the input DataFrames</td>
</tr>
</tbody>
</table>

## Parameters

Union does not take any parameters.
