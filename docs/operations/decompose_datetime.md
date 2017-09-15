---
layout: documentation
displayTitle: Decompose Datetime
title: Decompose Datetime
description: Decompose Datetime
usesMathJax: true
includeOperationsMenu: true
---

Extracts parts of a timestamp from a specified timestamp column to newly created columns.
For example for timestamp column: ``'Birthdate'`` when extracting year part new
column will be created with name: ``year`` and Numeric type.
If the value for parameter ``prefix`` is provided, it will be prepended to
generated columns' names, according to the pattern: ``prefix + timestampPartName``.

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
<td>DataFrame with timestamp column to decompose.</td>
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
<td>DataFrame containing new columns with parts of timestamp extracted from the original
timestamp.</td>
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
<td><code>timestamp column</code></td>
<td><code><a href="../parameter_types.html#single_column_selector">SingleColumnSelector</a></code></td>
<td>One of the DataFrame columns.
   If column selected by user has type different then Timestamp, <code>WrongColumnTypeException</code>
   will be thrown.
   If selected column does not exist, <code>ColumnDoesNotExistException</code> will be thrown.</td>
</tr>
<tr>
<td><code>parts</code></td>
<td><code><a href="../parameter_types.html#multiple_choice">MultipleChoice</a></code></td>
<td>Parts of timestamp to extract to separate columns.
   Possible values are: <code>[year, month, day, hour, minutes, seconds]</code>.</td>
</tr>
<tr>
<td><code>prefix</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>Optional prefix for created columns.
   If provided, names of generated columns match pattern: <code>prefix + timestampPartName</code>,
   otherwise names will match pattern: <code>timestampPartName</code>.</td>
</tr>
</tbody>
</table>

## Example

**Input data**

| Name         | Birthdate           |
|:-------------|:--------------------|
| Jim Morrison | 1943-12-08 13:47:16 |
| Jimmy Page   | 1944-01-09 08:35:10 |

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
<td><code>timestamp column</code></td>
<td><code>Name</code></td>
</tr>
<tr>
<td><code>parts</code></td>
<td><code>[year, month, day]</code></td>
</tr>
</tbody>
</table>

**Output data**

| Name         | Birthdate           | year | month | day |
|:-------------|:--------------------|:-----|:------|:----|
| Jim Morrison | 1943-12-08 13:47:16 | 1943 | 12    | 8   |
| Jimmi Page   | 1944-01-09 08:35:10 | 1944 | 1     | 9   |

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
<td><code>timestamp column</code></td>
<td><code>Name</code></td>
</tr>
<tr>
<td><code>parts</code></td>
<td><code>[year, month, day]</code></td>
</tr>
<tr>
<td><code>prefix</code></td>
<td><code>"Birthdate_"</code></td>
</tr>
</tbody>
</table>

**Output data**

| Name         | Birthdate           | Birthdate_year | Birthdate_month | Birthdate_day |
|:-------------|:--------------------|:---------------|:----------------|:--------------|
| Jim Morrison | 1943-12-08 13:47:16 | 1943           | 12              | 8             |
| Jimmi Page   | 1944-01-09 08:35:10 | 1944           | 1               | 9             |
