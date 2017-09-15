---
layout: documentation
displayTitle: Convert Type
title: Convert Type
description: Convert Type
usesMathJax: true
includeOperationsMenu: true
---

Converts types of columns in a DataFrame. It can convert one or more columns at once.
All of the selected columns are converted to the same (selected) type. The conversion is done in
place.

Supports conversions from all types to String type, Numeric type and Categorical type.
When a non-string column is converted to Categorical it is first converted to String.
Empty strings in columns converted to Categorical are converted to null values.
Every null value stays a null value in the result DataFrame (despite the column type change).
When a Timestamp column is converted to Numeric then each value is represented
by the number of milliseconds since 1 January 1970.

Boolean converted to String generates a column of 'true' and 'false' strings.
Categorical can be converted to Numeric as long as all categories' names are String representation
of a numeric value.

String column can be converted to Numeric as long as all values in the column represent a numeric value.

A column converted to its type is not modified.
If one or more column can not be converted,
the operation will fail at runtime with TypeConversionException.

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
<td>DataFrame to select columns from.</td>
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
<td>DataFrame with the converted columns.</td>
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
<td><code>selected columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Columns to convert.
If one of the columns is selected more than once (eg. by name and by type)
it will be included only once. When a column selected by name
or by index does not exist the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.</td>
</tr>
<tr>
<td><code>target type</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>Target type of the conversion. Possible values are: <code>[Numeric,String,Categorical]</code>.</td>
</tr>
</tbody>
</table>
