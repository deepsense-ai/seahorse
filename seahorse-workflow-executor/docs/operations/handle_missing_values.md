---
layout: global
displayTitle: Handle Missing Values
title: Handle Missing Values
description: Handle Missing Values
usesMathJax: true
includeOperationsMenu: true
---

Finds rows containing empty values and handles them according to the chosen strategy.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another [DataFrame](../classes/dataframe.html) using a [Transform](transform.html) operation.

**Since**: Seahorse 0.4.0

## Input

<table>
<thead>
<tr>
<th style="width:30%">Port</th>
<th style="width:25%">Type Qualifier</th>
<th style="width:45%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The <code>DataFrame</code> to process.</td>
</tr>
</tbody>
</table>

## Output

<table>
<thead>
<tr>
<th style="width:30%">Port</th>
<th style="width:25%">Type Qualifier</th>
<th style="width:45%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>The output <code>DataFrame</code> with processed missing values.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td>The <code>Transformer</code> that allows to apply the operation on other <code>DataFrames</code> using
<a href="transform.html">Transform</a>.</td>
</tr>
</tbody>
</table>

## Parameters

<table class="table">
<thead>
<tr>
<th style="width:30%">Name</th>
<th style="width:25%">Type</th>
<th style="width:45%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>columns</code></td>
<td><code><a href="../parameter_types.html#multiple-column-selector">MultipleColumnSelector</a></code></td>
<td>Columns to process.
If one of the columns is selected more than once (e.g. by name and by type)
it will be included only once. When a column selected by name
or by index does not exist the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.</td>
</tr>
<tr>
<td><code>strategy</code></td>
<td><code><a href="../parameter_types.html#single-choice">Single Choice</a></code></td>
<td>
  Strategy for handling missing values in the data.<br />
  Possible values: <code>["remove row", "remove column", "replace with custom value", "replace with mode"]</code>
</td>
</tr>
<tr>
<td><code>missing value indicator</code></td>
<td><code><a href="../parameter_types.html#single-choice">Single Choice</a></code></td>
<td>
  When set to <code>"Yes"</code>, a missing value indicator is added for each column in the
  selected column range. Newly generated columns contain <code>true</code> if the value was
  missing and <code>false</code> otherwise. The names of generated columns are constructed by
  prepending the given <code><a href="#indicator-column-prefix">indicator column prefix</a></code>
  to the original column name.<br />
  Possible values: <code>["Yes", "No"]</code>
</td>
</tr>
<tr>
<td><code>value</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>
  Available only if <code><a href="#strategy">strategy</a></code> is set to
  <code>"replace with custom value"</code>. It contains a replacement for the missing values.
  The replacement value should match selected columns' type. Boolean values are represented
  as <code>true</code> or <code>false</code>. Timestamps are represented in
  <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code> format.
  Example timestamp: <code>2015-03-30 15:25:00.0</code>.
</td>
</tr>
<tr>
<td><code>empty column strategy</code></td>
<td><code><a href="../parameter_types.html#single-choice">Single Choice</a></code></td>
<td>
  Available only if <code><a href="#strategy">strategy</a></code> is set to <code>"replace with mode"</code>.
  It defines whether to remove or retain columns, which contain only empty values.
  Possible values: <code>["remove", "retain"]</code>
</td>
</tr>
<tr>
<td><code>indicator column prefix</code></td>
<td><code><a href="../parameter_types.html#string">String</a></code></td>
<td>
  Available only if <code><a href="#missing-value-indicator">missing value indicator</a></code>
  is set to <code>"Yes"</code>. It defines the prefix for generated missing value indicator columns.
</td>
</tr>
<tr>
<td><code>user-defined missing values</code></td>
<td><code><a href="../parameter_types.html#parameters-sequence">Parameters Sequence</a></code></td>
<td>The sequence of user-defined missing values. Provided value will be cast to all chosen column types if possible,
so for example a value <code>-1</code> might be applied to all numeric and string columns.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

## Available Strategies

<table class="table">
<thead>
<tr>
<th style="width:30%">Name</th>
<th style="width:70%">Description</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>remove row</code></td>
<td>Removes all rows containing at least one missing value in the selected column range.</td>
</tr>
<tr>
<td><code>remove column</code></td>
<td>Removes columns with at least one missing value. Only the columns from the selected column range are affected.</td>
</tr>
<tr>
<td><code>replace with custom value</code></td>
<td>Replaces empty values with custom value (within selected column range).</td>
</tr>
<tr>
<td><code>replace with mode</code></td>
<td>Replaces empty values within selected column range with the mode (most frequently occurring value in a column).</td>
</tr>
</tbody>
</table>


{% markdown operations/examples/HandleMissingValues.md %}
