---
layout: global
displayTitle: Python Transformation
title: Python Transformation
description: Python Transformation
usesMathJax: true
includeOperationsMenu: true
---

Executes a Python function provided by the user on a [DataFrame](../classes/dataframe.html) connected to its input port.
Returns the results of the execution as a `DataFrame`.

Also returns a [Transformer](../classes/transformer.html) that can be later applied
to another `DataFrame` with a [Transform](transform.html) operation.

The Python function that will be executed must:

* be named <code>transform</code>,

* take exactly one argument of type `DataFrame`,

* return a `DataFrame` (or data which can be automatically converted to Spark `DataFrame`:
pandas.DataFrame, single value, tuple/list of single values, tuple/list of tuples/lists of single values).

{% markdown operations/python_global_scope.md %}

#### Example Python code:
{% highlight python %}
from pyspark.sql.types import Row

def transform(dataframe):
    return spark.createDataFrame(dataframe.rdd.map(lambda row: Row(row.numbers_column*2)))
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
<td>The <code>DataFrame</code> that will be passed to the <code>transform</code> function.</td>
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
<td>The return value of the <code>transform</code> function.</td>
</tr>
<tr>
<td><code>1</code></td><td>
<code><a href="../classes/transformer.html">Transformer</a></code></td>
<td><code>Transformer</code> that allows to apply the operation on another <code>DataFrames</code> using
<a href="transform.html">Transform</a>.</td>
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
<td><code><a href="../parameter_types.html#code-snippet">Code Snippet</a></code></td>
<td>The Python code to be executed. It has to contain a Python function complying to signature
presented in the operation's description.</td>
</tr>

<tr>
<td><code>report type</code></td>
<td><code><a href="../parameter_types.html#report-type">ReportType</a></code></td>
<td>Type of content for generated reports.</td>
</tr>

</tbody>
</table>

{% markdown operations/examples/PythonTransformationMock.md %}
