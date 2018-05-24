---
layout: global
displayTitle: Join
title: Join
description: Join
usesMathJax: true
includeOperationsMenu: true
---

Joins two [DataFrames](../classes/dataframe.html) by performing an SQL join operation.
Depending on set parameters inner, outer, left outer or right outer join operation will be performed.

Creates a new `DataFrame` that consists of the values from all the columns of the left `DataFrame`
and the columns not used in the join conditions from the right `DataFrame`.
Two rows match when all of the equality conditions created by ``join columns`` are satisfied.
That is, the values in the rows are equal. The order of the columns is preserved.

The operation joins two `DataFrames` by the column pairs given in ``join columns`` parameter.
For each given pair, both columns must be of the same type. If the column pairs in ``join columns``
are not present in their `DataFrames` (left `DataFrame` and right `DataFrame`, respectively),
a ``ColumnDoesNotExistException`` is thrown. If columns from one pair have different types,
a ``WrongColumnTypeException`` is thrown.

If values of ``left prefix`` and/or ``right prefix`` are provided,
the columns in the output `DataFrame` are renamed by prepending the prefix
proper for the table they come from.

If the columns' names in the resulting `DataFrame` are not to be unique,
a ``DuplicatedColumnsException`` is thrown.

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
<td>The left-side <code>DataFrame</code>.</td>
</tr>
<tr>
<td><code>1</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The right-side <code>DataFrame</code>.</td>
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
<td>The <code>DataFrame</code> containing all the columns of the left <code>DataFrame</code> and the
   columns of the right <code>DataFrame</code> not used in join condition
   (in <code>join columns</code> parameter).</td>
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
<td><code>join type</code></td>
<td><code><a href="../parameter_types.html#single_choice">Single Choice</a></code></td>
<td>The type of the join to perform. Possible values are:
   <code>Inner</code>, <code>Outer</code>, <code>Left outer</code>, <code>Right outer</code>.
   Default value: <code>Inner</code>.</td>
</tr>

<tr>
<td><code>join columns</code></td>
<td><code><a href="../parameter_types.html#parameters-sequence">Parameters Sequence</a></code></td>
<td>The sequence of column pairs (<code>left column: <a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code>,
   <code>right column: <a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code>) defining the condition for the JOIN operation.
   Empty join condition is not supported and <code>ColumnDoesNotExistException</code> is thrown.
   When a column selected by name or by index does not exist, <code>ColumnDoesNotExistException</code> is thrown.
   When the type of columns to JOIN upon in the two <code>DataFrames</code> do not match,
   <code>WrongColumnTypeException</code> is thrown.</td>
</tr>
<tr>
<td><code>left prefix</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>An optional prefix, which can be prepended
   to these columns in the output table, which come from the left input table.</td>
</tr>
<tr>
<td><code>right prefix</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>An optional prefix, which can be prepended
   to these columns in the output table, which come from the right input table.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/Join.md %}
