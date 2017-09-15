---
layout: documentation
displayTitle: Missing Values Handler
title: Missing Values Handler
description: Missing Values Handler
usesMathJax: true
includeOperationsMenu: true
---

Missing Values Handler finds rows containing empty values and handles them according to the
chosen strategy.

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
<td>DataFrame to process</td>
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
<td>Output DataFrame with processed missing values</td>
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
<td><code id="columns">columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Columns to process.
If one of the columns is selected more than once (eg. by name and by type)
it will be included only once. When a column selected by name
or by index does not exist the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.</td>
</tr>
<tr>
<td><code id="seed">strategy</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>
  Strategy of handling missing values in data.<br />
  Possible values: <code>["remove row"]</code>
</td>
</tr>
</tbody>
</table>

## Available Strategies

<table class="table">
<thead>
<tr>
<th style="width:20%">Name</th>
<th style="width:80%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>remove row</code></td>
<td>Remove all rows containing at least one missing value.</td>
</tr>
</tbody>
</table>
