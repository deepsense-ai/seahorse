---
layout: documentation
displayTitle: String Indexer
title: String Indexer
description: String Indexer
usesMathJax: true
includeOperationsMenu: true
---
Maps a string column of labels to a column of label indices.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#io.deepsense.deeplang.doperables.SingleStringIndexer">io.deepsense.deeplang.doperables.SingleStringIndexer documentation</a>.

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
<td><code>treshold</code></td>
<td>transform one column</td>
</tr>
<tr>
<td><code>one or many</code></td>
<td>transform one column</td>
</tr>
<tr>
<td><code>input column</code></td>
<td>city</td>
</tr>
<tr>
<td><code>transform in place</code></td>
<td>create a new column</td>
</tr>
<tr>
<td><code>output column</code></td>
<td>city_indexed</td>
</tr>
</tbody>
</table>

### Input

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

### Output

<table class="table">
    <thead>
        <tr>
            <th>city</th>
            <th>price</th>
            <th>city_indexed</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>CityA</td>
            <td>695611.0</td>
            <td>0.0</td>
       </tr>
        <tr>
            <td>CityC</td>
            <td>294691.0</td>
            <td>2.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>430784.0</td>
            <td>1.0</td>
       </tr>
        <tr>
            <td>CityB</td>
            <td>336677.0</td>
            <td>1.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>584639.0</td>
            <td>0.0</td>
       </tr>
        <tr>
            <td>CityA</td>
            <td>579560.0</td>
            <td>0.0</td>
       </tr>
    </tbody>
</table>

