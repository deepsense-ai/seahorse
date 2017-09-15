---
layout: documentation
displayTitle: Custom Python Column Operation
title: Custom Python Column Operation
description: Custom Python Column Operation
usesMathJax: true
includeOperationsMenu: true
---

Executes Python function provided by the user on a column (columns) of DataFrame connected to its input port.
Returns result of Python function execution as a DataFrame and CustomPythonColumnOperationTransformer.

The function that will be executed has to:

* have name <code>transform_column</code>,

* take exactly one argument of type
<a target="_blank" href="http://spark.apache.org/docs/{{ site.WORKFLOW_EXECUTOR_SPARK_VERSION }}/api/python/pyspark.sql.html#pyspark.sql.Column">Column</a>,

* return Column.

#### Example Python code:

    def transform_column(column):
      return column * 2


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
<tr>
<td><code>0</code></td>
<td><code><a href="../classes/dataframe.html">DataFrame</a></code></td>
<td>DataFrame to be transformed.</td>
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
      <td>Output DataFrame</td>
    </tr>
    <tr>
      <td><code>1</code></td>
      <td><code><a href="../classes/transformer.html">Transformer</a></code></td>
      <td>Transformer that allows to apply the operation on other DataFrames using <a href="transform.html">Transform</a></td>
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
  <td><code>code</code></td>
  <td><code><a href="../parameters.html#code-snippet">Code Snippet</a></code></td>
  <td>The Python code to be executed. It has to contain Python function complying to signature presented in the operation's description.</td>
</tr>

<tr>
  <td><code>target type</code></td>
  <td><code><a href="../parameters.html#single_choice">Choice</a></code></td>
  <td>Target type of the conversion. Possible values are: <code>[String, Boolean, Timestamp, Double, Float, Long, Integer, Vector]</code>.</td>
</tr>

<tr>
  <td><code>operate on</code></td>
  <td><code><a href="../parameters.html#input_output_column_selector">InputOutputColumnSelector</a></code></td>
  <td>Input and output columns for the operation.</td>
</tr>
</tbody>
</table>
