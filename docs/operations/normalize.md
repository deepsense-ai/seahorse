---
layout: documentation
displayTitle: Normalize
title: Normalize
description: Normalize
usesMathJax: true
includeOperationsMenu: true
---
Normalize vector columns using given p-norm.

This operation is ported from Spark ML. For more details, see: <a target="_blank" href="http://spark.apache.org/docs/1.6.0/api/scala/index.html#org.apache.spark.ml.feature.Normalizer">org.apache.spark.ml.feature.Normalizer documentation</a>.

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
<td><code>p</code></td>
<td><code><a href="../parameters.html#numeric">Numeric</a></code></td>
<td>Normalization in L^p space. Must be >= 1.</td>
</tr>

<tr>
<td><code>operate on</code></td>
<td><code><a href="../parameters.html#input_output_column_selector">InputOutputColumnSelector</a></code></td>
<td>Input and output columns for the operation.</td>
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
<td><code>p</code></td>
<td>2.0</td>
</tr>
<tr>
<td><code>operate on</code></td>
<td>one column</td>
</tr>
<tr>
<td><code>input column</code></td>
<td>assembled</td>
</tr>
<tr>
<td><code>output</code></td>
<td>replace input column</td>
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
            <td>[0.200435842852944, 0.9014817812524754, 0.38360926861807465]</td>
       </tr>
        <tr>
            <td>2011-01-03T21:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.64</td>
            <td>0.18</td>
            <td>[0.19800669372366633, 0.9435910944389162, 0.26538499531094517]</td>
       </tr>
        <tr>
            <td>2011-01-03T22:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.69</td>
            <td>0.14</td>
            <td>[0.18737248025109177, 0.9626732045662942, 0.19532499802794379]</td>
       </tr>
        <tr>
            <td>2011-02-11T06:00:00.000Z</td>
            <td>0.0</td>
            <td>0.68</td>
            <td>0.1</td>
            <td>[0.0, 0.9893591353648532, 0.14549399049483136]</td>
       </tr>
        <tr>
            <td>2011-02-13T17:00:00.000Z</td>
            <td>0.3284</td>
            <td>0.28</td>
            <td>0.42</td>
            <td>[0.545332482918055, 0.46496070407142326, 0.6974410561071348]</td>
       </tr>
        <tr>
            <td>2011-02-18T11:00:00.000Z</td>
            <td>0.1642</td>
            <td>0.72</td>
            <td>0.44</td>
            <td>[0.19101268332804697, 0.8375708404153094, 0.511848846920467]</td>
       </tr>
        <tr>
            <td>2011-02-19T02:00:00.000Z</td>
            <td>0.3881</td>
            <td>0.13</td>
            <td>0.44</td>
            <td>[0.6458280501805858, 0.2163299317791192, 0.7321936152524033]</td>
       </tr>
        <tr>
            <td>2011-02-19T03:00:00.000Z</td>
            <td>0.2985</td>
            <td>0.14</td>
            <td>0.42</td>
            <td>[0.5590414543167377, 0.262196996999475, 0.786590990998425]</td>
       </tr>
        <tr>
            <td>2012-12-31T23:00:00.000Z</td>
            <td>0.1343</td>
            <td>0.65</td>
            <td>0.26</td>
            <td>[0.1884021354472996, 0.9118495014202885, 0.3647398005681154]</td>
       </tr>
    </tbody>
</table>
