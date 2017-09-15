---
layout: documentation
displayTitle: Assemble Vector
title: Assemble Vector
description: Assemble Vector
usesMathJax: true
includeOperationsMenu: true
---
Merges multiple columns into a single vector column.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.VectorAssembler">org.apache.spark.ml.feature.VectorAssembler documentation</a>.

**Since**: Seahorse 1.0.0

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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Input DataFrame</td></tr>
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
    <tr><td><code>0</code></td><td><code><a href="../classes/dataframe.html">DataFrame</a></code></td><td>Output DataFrame</td></tr><tr><td><code>1</code></td><td><code><a href="../classes/transformer.html">Transformer</a></code></td><td>Transformer that allows to apply the operation on other DataFrames using <a href="transform.html">Transform</a></td></tr>
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
<td><code>input columns</code></td>
<td><code><a href="../parameters.html#multiple_column_selector">MultipleColumnSelector</a></code></td>
<td>Input columns.</td>
</tr>

<tr>
<td><code>output column</code></td>
<td><code><a href="../parameters.html#string">String</a></code></td>
<td>Name of created output column.</td>
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
<td><code>input columns</code></td>
<td>Selected columns: windspeed, hum, temp</td>
</tr>

<tr>
<td><code>output column</code></td>
<td>assembled</td>
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
            <th>datetime</th>
            <th>windspeed</th>
            <th>hum</th>
            <th>temp</th>
            <th>assembled</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>2011-01-03T20:00:00.000Z</td>
            <td>0.1045</td>
            <td>0.47</td>
            <td>0.2</td>
            <td>[0.1045, 0.47, 0.2]</td>
       </tr>
        <tr>
            <td>2011-01-03T21:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.64</td>
            <td>0.18</td>
            <td>[0.1343, 0.64, 0.18]</td>
       </tr>
        <tr>
            <td>2011-01-03T22:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.69</td>
            <td>0.14</td>
            <td>[0.1343, 0.69, 0.14]</td>
       </tr>
        <tr>
            <td>2011-02-11T06:00:00.000Z</td>
            <td>0.0</td>
            <td>0.68</td>
            <td>0.1</td>
            <td>[0.0, 0.68, 0.1]</td>
       </tr>
        <tr>
            <td>2011-02-13T17:00:00.000Z</td>
            <td>0.3284</td>
            <td>0.28</td>
            <td>0.42</td>
            <td>[0.3284, 0.28, 0.42]</td>
       </tr>
        <tr>
            <td>2011-02-18T11:00:00.000Z</td>
            <td>0.1642</td>
            <td>0.72</td>
            <td>0.44</td>
            <td>[0.1642, 0.72, 0.44]</td>
       </tr>
        <tr>
            <td>2011-02-19T02:00:00.000Z</td>
            <td>0.3881</td>
            <td>0.13</td>
            <td>0.44</td>
            <td>[0.3881, 0.13, 0.44]</td>
       </tr>
        <tr>
            <td>2011-02-19T03:00:00.000Z</td>
            <td>0.2985</td>
            <td>0.14</td>
            <td>0.42</td>
            <td>[0.2985, 0.14, 0.42]</td>
       </tr>
        <tr>
            <td>2012-12-31T23:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.65</td>
            <td>0.26</td>
            <td>[0.1343, 0.65, 0.26]</td>
       </tr>
    </tbody>
</table>
