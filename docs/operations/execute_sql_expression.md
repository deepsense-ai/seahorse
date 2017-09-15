---
layout: documentation
displayTitle: SQL Expression
title: SQL Expression
description: SQL Expression
usesMathJax: true
includeOperationsMenu: true
---

Executes an Spark SQL expression provided by the user on a DataFrame connected to its input port.
Returns the results of the execution as a DataFrame.

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
<td>A DataFrame that the SQL expression will be executed on.</td>
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
<td>The results of the SQL expression.</td>
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
<td><code>dataframe id</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>An identifier that can be used in the SQL expression to refer to the input DataFrame. The value has to be unique in the workflow.</td>
</tr>
<tr>
<td><code>expression</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>The SQL expression to be executed. The expression must be a valid Spark SQL expression.</td>
</tr>
</tbody>
</table>

## Example

### Parameters

<table class="table">
<thead>
<tr>
<th style="width:15%">Name</th>
<th style="width:80%">Value</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>dataframe id</code></td>
<td>inputDF</td>
</tr>
<tr>
<td><code>expression</code></td>
<td>select avg(temp) as avg_temp, max(windspeed) as max_windspeed from inputDF</td>
</tr>
</tbody>
</table>

### Input

<table class="table">
    <thead>
        <tr>
            <th>datetime</th>
            <th>windspeed</th>
            <th>hum</th>
            <th>temp</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>2011-01-03T20:00:00.000Z</td>
            <td>0.1045</td>
            <td>0.47</td>
            <td>0.2</td>
       </tr>
        <tr>
            <td>2011-01-03T21:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.64</td>
            <td>0.18</td>
       </tr>
        <tr>
            <td>2011-01-03T22:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.69</td>
            <td>0.14</td>
       </tr>
        <tr>
            <td>2011-02-11T06:00:00.000Z</td>
            <td>0.0</td>
            <td>0.68</td>
            <td>0.1</td>
       </tr>
        <tr>
            <td>2011-02-13T17:00:00.000Z</td>
            <td>0.3284</td>
            <td>0.28</td>
            <td>0.42</td>
       </tr>
        <tr>
            <td>2011-02-18T11:00:00.000Z</td>
            <td>0.1642</td>
            <td>0.72</td>
            <td>0.44</td>
       </tr>
        <tr>
            <td>2011-02-19T02:00:00.000Z</td>
            <td>0.3881</td>
            <td>0.13</td>
            <td>0.44</td>
       </tr>
        <tr>
            <td>2011-02-19T03:00:00.000Z</td>
            <td>0.2985</td>
            <td>0.14</td>
            <td>0.42</td>
       </tr>
        <tr>
            <td>2012-12-31T23:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.65</td>
            <td>0.26</td>
       </tr>
    </tbody>
</table>

### Output

<table class="table">
    <thead>
        <tr>
            <th>avg_temp</th>
            <th>max_windspeed</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>0.2888888888888889</td>
            <td>0.3881</td>
       </tr>
    </tbody>
</table>
