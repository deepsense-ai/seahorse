---
layout: documentation
displayTitle: One Hot Encoder
title: One Hot Encoder
description: One Hot Encoder
usesMathJax: true
includeOperationsMenu: true
---

Encodes selected Categorical columns so that they are decomposed to Numeric columns
containing only 0's and 1's.
Encoding is performed for each selected column.

For each category level $$x$$ of Categorical column's categories, additional column is appended.
This additional column has value 1 in particular row if and only if this row has category level
$$x$$.
Note that column corresponding to last category level can be dropped without information loss.
For example, if column ``Kind`` has three category levels: ``Mammal``, ``Bird`` and ``Fish``,
then if some row has ``0`` both in columns ``Kind_Bird`` and ``Kind_Fish``, it implies that it
is ``Mammal``.
This redundant column can be either dropped or preserved
- this can be set through ``with redundant`` parameter.

Sets of columns will be appended in order of corresponding selected columns in original DataFrame.
Columns for each category will be appended in lexicographical order of this category levels.
For rows that have ``null`` in Categorical column that is being decomposed, it will have ``null``'s
in corresponding columns.

Result columns will have name of column category they represent with name of corresponding
level appended. If the value for parameter ``prefix`` is provided, it will be prepended to
generated columns' names, according to the pattern: ``prefix + columnName + "_" + value``.

Encoded column will not be removed from DataFrame.

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
<td>DataFrame with columns to encode.</td>
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
<td>DataFrame with columns containing encoding appended.x</td>
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
<td><code>columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Subset of the DataFrame columns to encode.
   If columns selected by the user have type different than Categorical,
   <code>WrongColumnTypeException</code> will be thrown.
   If some of selected columns do not exist,
   <code>ColumnDoesNotExistException</code> will be thrown.</td>
</tr>
<tr>
<td><code>with redundant</code></td>
<td><code><a href="../parameters.html#boolean">Boolean</a></code></td>
<td>If <code>true</code>, column that represents last (lexicographically) level of category will be present in result - otherwise it will be dropped.</td>
</tr>
<tr>
<td><code>prefix</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Optional prefix for created columns. If provided, names of generated columns match pattern: <code>prefix + columnName + "_" + value</code>.</td>
</tr>
</tbody>
</table>

## Example 1

**Input data**

| Animal  | Kind   | Size  |
|:--------|:-------|:------|
| Cow     | Mammal | Big   |
| Ostrich | Bird   | Big   |
| Trout   | Fish   | null  |
| Sparrow | Bird   | Small |
| Thing   | null   | Small |

**Operation params**

<table>
<thead>
<tr>
<th style="width:15%">Parameter</th>
<th style="width:85%">Value</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>columns</code></td>
<td><code>["Kind", "Size"]</code></td>
</tr>
<tr>
<td><code>with redundant</code></td>
<td><code>false</code></td>
</tr>
<tr>
<td><code>prefix</code></td>
<td><code>"Animal_"</code></td>
</tr>
</tbody>
</table>

**Output data**

| Animal  | Kind   | Size  | Animal_Kind_Bird | Animal_Kind_Fish | Animal_Size_Big |
|:--------|:-------|:------|:-----------------|:-----------------|:----------------|
| Cow     | Mammal | Big   | 0                | 0                | 1               |
| Ostrich | Bird   | Big   | 1                | 0                | 1               |
| Trout   | Fish   | null  | 0                | 1                | null            |
| Sparrow | Bird   | Small | 1                | 0                | 0               |
| Thing   | null   | Small | null             | null             | 0               |

## Example 2

**Input data**

| Animal  | Kind   |
|:--------|:-------|
| Cow     | Mammal |
| Ostrich | Bird   |
| Trout   | Fish   |
| Sparrow | Bird   |
| Thing   | null   |

**Operation params**

<table>
<thead>
<tr>
<th style="width:15%">Parameter</th>
<th style="width:85%">Value</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>columns</code></td>
<td><code>["Kind"]</code></td>
</tr>
<tr>
<td><code>with redundant</code></td>
<td><code>true</code></td>
</tr>
</tbody>
</table>

**Output data**

| Animal  | Kind   | Kind_Bird | Kind_Fish | Kind_Mammal |
|:--------|:-------|:----------|:----------|:------------|
| Cow     | Mammal | 0         | 0         | 0           |
| Ostrich | Bird   | 1         | 0         | 0           |
| Trout   | Fish   | 0         | 0         | 1           |
| Sparrow | Bird   | 1         | 0         | 0           |
| Thing   | null   | null      | null      | null        |
