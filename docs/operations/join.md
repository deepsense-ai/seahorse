---
layout: documentation
displayTitle: Join
title: Join
description: Join
usesMathJax: true
includeOperationsMenu: true
---

Performs a left join. Creates a new DataFrame that consists of the values in
the columns of the left and right DataFrames, without the columns used to perform the left join.
Rows match when the values in ``join columns`` do. The values must be equal to match.
Order of the columns is preserved.

The operation joins two DataFrames by the column pairs given in ``join columns`` parameter.
For each given pair, both columns must be of the same type. If column pairs in ``join columns``
are not present in their DataFrames (left DataFrame and right DataFrame, respectively),
``ColumnDoesNotExistException`` is thrown. If columns from one pair are of different types,
``WrongColumnTypeException`` is thrown.

The join operation skips ``null`` values in left join, i.e. ``null`` s do not match and yield rows.

If values of ``left prefix`` and/or ``right prefix`` are provided, columns in the output table
are renamed by prepending prefix proper for the table, which they come from.

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
<td>Left-side DataFrame.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>Right-side DataFrame.</td>
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
<td>DataFrame containing the columns of the left DataFrame
       and the columns of the right DataFrame but the columns
       used to LEFT JOIN (in <code>join columns</code> parameter).</td>
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
<td><code>join columns</code></td>
<td><code><a href="../parameters.html#parameters_sequence">Parameters Sequence</a></code></td>
<td>Sequence of pairs (<code>left column: <a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code>,
   <code>right column: <a href="../parameters.html#single_column_selector">SingleColumnSelector</a></code>) defining condition for the JOIN operation.
   Empty join condition is not supported and exception <code>ColumnDoesNotExistException</code> is thrown.
   When a column selected by name or by index does not exist, <code>ColumnDoesNotExistException</code> is thrown.
   When the type of columns to LEFT JOIN upon in two DataFrames do not match,
   <code>WrongColumnTypeException</code> is thrown.</td>
</tr>
<tr>
<td><code>left prefix</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Optional prefix, which can be prepended
   to these columns in the output table, which come from the left input table.</td>
</tr>
<tr>
<td><code>right prefix</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Optional prefix, which can be prepended
   to these columns in the output table, which come from the right input table.</td>
</tr>
</tbody>
</table>

{% markdown operations/examples/Join.md %}
