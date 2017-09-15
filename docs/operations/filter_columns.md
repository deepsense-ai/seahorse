---
layout: documentation
displayTitle: Filter Columns
title: Filter Columns
description: Filter Columns
usesMathJax: true
includeOperationsMenu: true
---

Creates a new DataFrame that contains only the selected columns. Order of the columns is preserved.
Each column can be selected only once. Selecting a column more than once will not duplicate the
column in the output. Thus, in the resulting DataFrame no column will be duplicated.

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
<td>DataFrame containing the selected columns (and only them).</td>
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
<td>Columns to be included in the output DataFrame.
Even if one of the columns is selected more than once (eg. by name and by type)
it will be included only once. Empty selection is supported, but when a column selected by name
or by index does not exist the operation will fail at runtime with <code>ColumnsDoNotExistException</code>.
</td>
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
<td><code>columns</code></td>
<td>Selected columns: "city" and "price"</td>
</tr>
</tbody>
</table>

### Input

<table class="table">
    <thead>
        <tr>
            <th>city</th>
            <th>beds</th>
            <th>price</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>CityA</td>
            <td>4.0</td>
            <td>695611.0</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>2.0</td>
            <td>294691.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>3.0</td>
            <td>430784.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>2.0</td>
            <td>336677.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>3.0</td>
            <td>584639.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>4.0</td>
            <td>579560.0</td>
       </tr>
    </tbody>
</table>

### Output

<table class="table">
    <thead>
        <tr>
            <th>city</th>
            <th>price</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>CityA</td>
            <td>695611.0</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>294691.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>430784.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>336677.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>584639.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>579560.0</td>
       </tr>
    </tbody>
</table>

