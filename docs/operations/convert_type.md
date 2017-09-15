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

Supports conversions from all types to String, Boolean, Timestamp, Double, Float, Long,
and Integer type.

Every null value stays a null value in the result DataFrame (despite the column type change).
When a Timestamp column is converted to Numeric then each value is represented
by the number of milliseconds since 1 January 1970.

Boolean converted to String generates a column of 'true' and 'false' strings.

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
<td><code>target type</code></td>
<td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
<td>Target type of the conversion. Possible values are: <code>[String, Boolean, Timestamp, Double, Float, Long,
Integer]</code>.</td>
</tr>

<tr>
<td><code>one or many</code></td>
<td><code><a href="../parameters.html#single_choice">SingleChoice</a></code></td>
<td>Transform one or many columns. Possible values: <code>["Transform one column", "Transform multiple columns"]</code></td>
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
<td><code>one or many</code></td>
<td>transform one column</td>
</tr>
<tr>
<td><code>input column</code></td>
<td>beds</td>
</tr>
<tr>
<td><code>transform in place</code></td>
<td>create a new column</td>
</tr>
<tr>
<td><code>output column</code></td>
<td>beds_int</td>
</tr>
<tr>
<td><code>target type</code></td>
<td>Integer</td>
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
            <th>beds</th>
            <th>price</th>
            <th>beds_int</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>CityA</td>
            <td>4.0</td>
            <td>695611.0</td>
            <td>4</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>2.0</td>
            <td>294691.0</td>
            <td>2</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>3.0</td>
            <td>430784.0</td>
            <td>3</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>2.0</td>
            <td>336677.0</td>
            <td>2</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>3.0</td>
            <td>584639.0</td>
            <td>3</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>4.0</td>
            <td>579560.0</td>
            <td>4</td>
       </tr>
    </tbody>
</table>
