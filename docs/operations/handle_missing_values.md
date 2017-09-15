---
layout: documentation
displayTitle: Handle Missing Values
title: Handle Missing Values
description: Handle Missing Values
usesMathJax: true
includeOperationsMenu: true
---

Handle Missing Values finds rows containing empty values and handles them according to the
chosen strategy.

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
<td>DataFrame to process</td>
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
<td>Output DataFrame with processed missing values</td>
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
<td><code id="columns">columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Columns to process.
If one of the columns is selected more than once (eg. by name and by type)
it will be included only once. When a column selected by name
or by index does not exist the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.</td>
</tr>
<tr>
<td><code id="strategy">strategy</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>
  Strategy of handling missing values in data.<br />
  Possible values: <code>["remove row", "remove column", "replace with custom value", "replace with mode"]</code>
</td>
</tr>
<tr>
<td><code id="missing-value-indicator">missing value indicator</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>
  When set to <code>"Yes"</code>, a missing value indicator is added for each column in the
  selected column range. Newly generated columns contain <code>true</code> if the value was
  missing and <code>false</code> otherwise. The names of generated columns are constructed by
  prepending the given <code><a href="#indicator-column-prefix">indicator column prefix</a></code>
  to original column name.<br />
  Possible values: <code>["Yes", "No"]</code>
</td>
</tr>
<tr>
<td><code id="value">value</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>
  Available only if <code><a href="#strategy">strategy</a></code> is set to
  <code>"replace with custom value"</code>. It contains a replacement for missing values.
  The replacement value should match selected columns' type. Boolean values are represented
  as <code>true</code> or <code>false</code>. Timestamps are represented in
  <code>yyyy-[m]m-[d]d hh:mm:ss[.f...]</code> format.
  Example timestamp: <code>2015-03-30 15:25:00.0</code>.
</td>
</tr>
<tr>
<td><code id="empty-column-strategy">empty column strategy</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>
  Available only if <code><a href="#strategy">strategy</a></code> is set to <code>"replace with mode"</code>.
  It defines whether to remove or retain columns, which contain only empty values.
  Possible values: <code>["remove", "retain"]</code>
</td>
</tr>
<tr>
<td><code id="indicator-column-prefix">indicator column prefix</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>
  Available only if <code><a href="#missing-value-indicator">missing value indicator</a></code>
  is set to <code>"Yes"</code>. It defines the prefix for generated missing value indicator columns.
</td>
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
<td>Removes all rows containing at least one missing value in selected column range.</td>
</tr>
<tr>
<td><code>remove column</code></td>
<td>Removes columns with at least one missing value. Only the columns from selected columns range are affected.</td>
</tr>
<tr>
<td><code>replace with custom value</code></td>
<td>Replaces empty values with custom value (within selected column range).</td>
</tr>
<tr>
<td><code>replace with mode</code></td>
<td>Replaces empty values within selected column range with the mode (most frequently occuring value in a column).</td>
</tr>
</tbody>
</table>

## Example

### Parameters

<table class="table">
<thead>
<tr>
<th style="width:20%">Name</th>
<th style="width:80%">Value</th>
</tr>
</thead>
<tbody>
<tr>
<td><code id="columns">columns</code></td>
<td>Selected columns: "baths" and "price"</td>
</tr>
<tr>
<td><code id="strategy">strategy</code></td>
<td>Remove row</td>
</tr>
<tr>
<td><code id="missing-value-indicator">missing value indicator</code></td>
<td>No</td>
</tr>
</tbody>
</table>

### Input

<table class="table">
    <thead>
        <tr>
            <th>city</th>
            <th>beds</th>
            <th>baths</th>
            <th>sq_ft</th>
            <th>price</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>""</td>
            <td>2.0</td>
            <td>1.0</td>
            <td>820.0</td>
            <td>449178.0</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>null</td>
            <td>1.0</td>
            <td>656.0</td>
            <td>267975.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>2.0</td>
            <td>null</td>
            <td>636.0</td>
            <td>348946.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>2.0</td>
            <td>1.0</td>
            <td>736.0</td>
            <td>null</td>
       </tr>
    </tbody>
</table>

### Output

<table class="table">
    <thead>
        <tr>
            <th>city</th>
            <th>beds</th>
            <th>baths</th>
            <th>sq_ft</th>
            <th>price</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>""</td>
            <td>2.0</td>
            <td>1.0</td>
            <td>820.0</td>
            <td>449178.0</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>null</td>
            <td>1.0</td>
            <td>656.0</td>
            <td>267975.0</td>
       </tr>
    </tbody>
</table>
