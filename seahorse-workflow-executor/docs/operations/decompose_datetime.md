---
layout: global
displayTitle: Decompose Datetime
title: Decompose Datetime
description: Decompose Datetime
usesMathJax: true
includeOperationsMenu: true
---

Extracts parts of a timestamp from a specified timestamp column to new columns.
For example: given the timestamp column ``'Birthdate'`` when extracting the year part, a new
column named ``year`` of type `Numeric` will be created.

If the value for parameter ``prefix`` is provided it will be prepended to the
generated columns' names, according to the pattern: ``prefix + timestampPartName``.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) with a [Transform](transform.html) operation.

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
<td>The <code>DataFrame</code> with the <code>timestamp</code> column to decompose.</td>
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
<td>The <code>DataFrame</code> with new columns with parts of timestamp extracted from the original
<code>timestamp</code> column.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>transformer</code> that allows to apply the operation on another DataFrame using a
<a href="transform.html">Transform</a>.</td>
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
<td><code><a href="../parameter_types.html#single-column-selector">SingleColumnSelector</a></code></td>
<td>One of the <code>DataFrame</code> columns.
   If the column selected by the user has a type other than <code>Timestamp</code>,
   <code>WrongColumnTypeException</code> will be thrown.
   If the selected column does not exist, a <code>ColumnDoesNotExistException</code> will be thrown.</td>
</tr>
<tr>
<td><code>parts</code></td>
<td><code><a href="../parameter_types.html#multiple-choice">MultipleChoice</a></code></td>
<td>Parts of <code>timestamp</code> to extract to new columns.
   Possible values are: <code>[year, month, day, hour, minutes, seconds]</code>.</td>
</tr>
<tr>
<td><code>prefix</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>An optional prefix for the created columns.
   If provided, names of the generated columns will match the <code>prefix + timestampPartName</code> pattern,
   otherwise names will match the <code>timestampPartName</code> pattern.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
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
