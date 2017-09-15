---
layout: documentation
displayTitle: Filter Rows
title: Filter Rows
description: Filter Rows
usesMathJax: true
includeOperationsMenu: true
---

Creates a [DataFrame](../classes/dataframe.html) containing only rows satisfying given condition.
Condition should be simple SQL condition (as used in `WHERE` condition).
Order of the columns is preserved.

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
<td>A <code>DataFrame</code> to filter rows on.</td>
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
<td>The <code>DataFrame</code> containing only rows satisfying given condition.</td>
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
<td><code>condition</code></td>
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>The filtering condition. Rows not satisfying given condition will be excluded from output
DataFrame. It should be simple SQL condition (as used in <code>WHERE</code> condition).</td>
</tr>
</tbody>
</table>

{% markdown operations/examples/FilterRows.md %}
