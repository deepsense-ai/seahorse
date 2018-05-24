---
layout: global
displayTitle: Python Column Transformation
title: Python Column Transformation
description: Python Column Transformation
usesMathJax: true
includeOperationsMenu: true
---

Executes Python function provided by the user on a column (columns) of [DataFrame](../classes/dataframe.html) connected to its input port.
Returns modified `DataFrame`.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

The function that will be executed has to:

* be named <code>transform_value</code>,

* take exactly two arguments: the value to be transformed and the name of column currently being transformed,

* return the transformed value that conforms to the selected target type (parameter).

The function is applied to the input `DataFrame` in parallel for better performance.

{% markdown operations/python_global_scope.md %}

#### Example Python code:
{% highlight python %}
def transform_value(value, column_name):
    return value
{% endhighlight %}


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
<td>The <code>DataFrame</code> to be transformed.</td>
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
      <td>The output <code>DataFrame</code>.</td>
    </tr>
    <tr>
      <td><code>1</code></td>
      <td><code><a href="../classes/transformer.html">Transformer</a></code></td>
      <td>A <code>Transformer</code> that allows to apply the operation on other
      <code>DataFrames</code> using a <a href="transform.html">Transform</a>.</td>
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
  <td><code>column operation code</code></td>
  <td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
  <td>The Python code to be executed. It has to contain a Python function complying to the signature
  presented in the operation's description.</td>
</tr>

<tr>
  <td><code>target type</code></td>
  <td><code><a href="../parameter_types.html#single-choice">Choice</a></code></td>
  <td>The target type of the conversion. Possible values are:
  <code>[String, Boolean, Timestamp, Double, Float, Long, Integer, Vector]</code>.</td>
</tr>

<tr>
  <td><code>operate on</code></td>
  <td><code><a href="../parameter_types.html#input-output-column-selector">InputOutputColumnSelector</a></code></td>
  <td>Input and output columns for the operation.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/PythonColumnTransformationMock.md %}
