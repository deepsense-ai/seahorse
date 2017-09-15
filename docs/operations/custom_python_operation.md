---
layout: documentation
displayTitle: Custom Python Operation
title: Custom Python Operation
description: Custom Python Operation
usesMathJax: true
includeOperationsMenu: true
---

Executes Python function provided by the user on a DataFrame connected to its input port.
Returns result of Python function execution as a DataFrame.

The function that will be executed has to:

* have name <code>transform</code>,

* take exactly one argument of type DataFrame,

* return DataFrame.

#### Example Python code:

    from pyspark.sql.types import Row

    def transform(dataframe):
      return sqlContext.createDataFrame(dataframe.map(lambda row: Row(row.numbers_column*2)))


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
<td>A DataFrame that will be passed to <code>transform</code> function.</td>
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
<td>The return value of <code>transform</code> function.</td>
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
<td>The Python code to be executed. It has to contain Python function complying to signature presented in the operation's description.</td>
</tr>
</tbody>
</table>
